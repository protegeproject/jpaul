// DiGraph.java, created Tue May  6 10:53:15 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.RelationFactory;
import jpaul.DataStructs.RelFacts;
import jpaul.DataStructs.DSUtil;

import jpaul.DataStructs.NonIterableSet;
import jpaul.DataStructs.NonIterableMap;
import jpaul.DataStructs.InterruptTraversalException;

import jpaul.Misc.Action;
import jpaul.Misc.ActionPredicate;
import jpaul.Misc.EqualityPredicate;
import jpaul.Misc.Predicate;

/**
 <code>DiGraph</code> models a directed graph.  A directed graph is
 defined by a set of <i>root</i> vertices and a <i>navigator</i>.
 The navigator is an iterator over the graph: given a vertex
 <code>v</code>, it gives <code>v</code>'s direct successors (i.e.,
 vertices pointed to by arcs that start in <code>v</code>), and
 (optionally), <code>v</code>'s direct predecessors.  The digraph
 contains all (transitive and reflexive) successors of the root
 vertices.

 <p>
 This design allows the use of many graph algorithms (e.g.,
 construction of strongly connected components) even for very general
 graphs where the arcs model only a subtle semantic relation (e.g.,
 caller-callee) that is not explicitly stored in the vertices.

 <p>
 There are two kinds of navigators: {@linkplain ForwardNavigator}s
 (give only successors) and bi-directional {@linkplain BiDiNavigator}s
 (both successors and predecessors).  For a given graph you should
 define at least one of them, i.e., you should override at least one
 of the methods {@link #getBiDiNavigator} and {@link #getForwardNavigator}
 from <code>DiGraph</code>.  The standard implementation of these
 methods is able to construct one navigator starting from the other
 one.  First, a bi-directional navigator is trivially a forward one
 too.  Next, we can use the roots and the forward navigator to build
 the successor relation, and next revert it to build the predecessor
 relation (linear in the size of the graph).

 <p>
 If you decide to implement both navigators (e.g., for efficiency)
 you should make sure the two navigators are consistent.  To see what
 this means, let <code>fnav</code> and <code>nav</code> be the
 forward, respectively the full navigator associated with a digraph.
 For any vertex <code>v</code>, <code>fnav.next(v)</code> should
 return the same vertices as <code>nav.next(v)</code>.  In addition,
 <code>nav</code> itself should be consistent, i.e., for any vertices
 <code>v1</code>, <code>v2</code> from the digraph, if <code>v1</code>
 appears in <code>nav.next(v2)</code>, then <code>v2</code> should
 appear in <code>nav.prev(v1)</code>.
 
 <p>
 To create a <code>DiGraph</code>, you can use of the following
 two static methods: {@link DiGraph#diGraph(Collection,BiDiNavigator)} and
 {@link DiGraph#diGraph(Collection,ForwardNavigator)}.  For fancier
 things, you can also subclass <code>DiGraph</code>.

 <p>
 Most of the algorithms are implemented as instance methods; very few
 are also implemented as static methods.  Here is some sample code:

<pre>
        // set of arcs as a relation vertex -> set of succs
        final jpaul.DataStructs.Relation<Vertex,Vertex> arcs = ...;
        // construct a directed graph based on the relation arcs
	DiGraph<Vertex> diGraph = DiGraph.<Vertex>diGraph
	    (// [ ... ]  some set of roots,
	     new ForwardNavigator<Vertex>() {
		 public List<Vertex> next(Vertex v) {
		     return new ArrayList(arcs.getValues(v));
		 }
	     });
	
	// iterate over the list of strongly connected components
	// in incremental topological order
	for(SCComponent<Vertex> scc : diGraph.getComponentDiGraph().incrOrder()) {
	    System.out.println("SCC vertices: " + scc.vertices());
	    // potentially, do something more interesting with "scc" :)
	}
</pre>

 @see ForwardNavigator
 @see BiDiNavigator

 @author Alexandru Salcianu - salcianu@alum.mit.edu
 @version $Id: DiGraph.java,v 1.36 2006/03/21 17:37:30 adam_kiezun Exp $ */
public abstract class DiGraph<Vertex> {

    /** Constructs a <code>DiGraph</code> with no caching (the safest
        choice). */
    public DiGraph() {
	this(false);
    }

    /** Constructs a <code>DiGraph</code>.

	@param CACHING If <code>true</code>, the implementation of the
	various methods of this <code>DiGraph</code> assume that the
	graph does not change in time.  Therefore, caching is used for
	performance reasons: e.g., once the set of vertices is
	computed, it is cached and used ever after.  */
    protected DiGraph(boolean CACHING) {
	this.CACHING = CACHING;
    }

    protected final boolean CACHING;

    /** Returns the <i>roots</i> of <code>this</code> directed graph.
        By &quot;roots of a digraph&quot; we mean any set of vertices
        such that one can explore the entire graph by (transitively)
        navigating on their outgoing arcs (using the <code>next</code>
        method of the navigator).  Notice that this set is not
        uniquely defined; also, it is OK to return ALL the vertices
        from the digraph.  The caller is not supposed to mutate the
        returned collection. */
    public abstract Collection<Vertex> getRoots();

    /** Returns the (bi-directional) navigator for <code>this</code>
        digraph.  The default implementation gets the forward
        navigator by calling <code>getForwardNavigator</code>,
        explores the entire digraph and constructs the predecessor
        relation.  Clearly, this is quite costly, and does not
        terminate for infinite digraphs.

	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getBiDiNavigator</code> and
	<code>getForwardNavigator</code>. */
    public BiDiNavigator<Vertex> getBiDiNavigator() {
	if(CACHING && (cachedNavigator != null))
	    return cachedNavigator;

	BiDiNavigator<Vertex> navigator = 
	    constructBiDiNavigator(RelFacts.<Vertex,Vertex>mapSet());

	if(CACHING) {
	    cachedNavigator = navigator;
	}

	return navigator;
    }
    private BiDiNavigator<Vertex> cachedNavigator;

    /** Construct a full navigator, starting from the forward
        navigator, as given by the method
        <code>getForwardNavigator</code>.  To construct the list of
        prdecessors, we reverse the edges by with the help of a
        relation constructed using the forward naviator.

        By default, <code>getBiDiNavigator</code> calls this method using
        the default (hash-map and hash-set based)
        <code>jpaul.DataStructs.MapSetRelationFactory</code>.  For a
        graph with a small degree, a subclass may choose to override
        <code>getBiDiNavigator</code> to use some other
        <code>RelationFactory</code>. */
    protected BiDiNavigator<Vertex> constructBiDiNavigator(RelationFactory<Vertex,Vertex> relFact) {
	final Relation<Vertex,Vertex> prevRel = relFact.create();
	final ForwardNavigator<Vertex> fnav = getForwardNavigator();
	
	for(Vertex vertex : vertices())
	    for (Vertex next : fnav.next(vertex))
		prevRel.add(next, vertex);
	
	final NonIterableMap<Vertex,List<Vertex>> v2preds = 
	    new NonIterableMap<Vertex,List<Vertex>>();
	// agree on some order between the preds
	for(Vertex v : prevRel.keys()) {
	    v2preds.put(v, new ArrayList<Vertex>(prevRel.getValues(v)));
	}
	
	return new BiDiNavigator<Vertex>() {
	    public List<Vertex> next(Vertex vertex) {
		return fnav.next(vertex);
	    }
	    public List<Vertex> prev(Vertex vertex) {
		List<Vertex> prev = v2preds.get(vertex);
		if(prev == null) return Collections.<Vertex>emptyList();
		return prev;
	    }
	};
    }
    
    /** Returns the forward navigator for <code>this</code> digraph.
	The default implementations returns the bi-directional
	navigator (obtained by calling <code>getBiDiNavigator</code>).
	
	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getBiDiNavigator</code> and
	<code>getForwardNavigator</code>. */
    public ForwardNavigator<Vertex> getForwardNavigator() {
	return getBiDiNavigator();
    }


    /** Constructs a <code>DiGraph</code> object.

	@param roots Collection of root vertices.  This collection
	will be used directly, without being cloned or copied into
	another collection; none of the graph algoritrhms will mutate
	it.

	@param navigator Bi-directional digraph navigator */
    public static <Vertex> DiGraph<Vertex> diGraph
	(final Collection<Vertex> roots, final BiDiNavigator<Vertex> navigator) {
	return new DiGraph<Vertex>() {
	    public Collection<Vertex> getRoots() { return roots; }
	    public BiDiNavigator<Vertex> getBiDiNavigator() { return navigator; }
	};
    }


    /** Constructs a <code>DiGraph</code> object.

	@param roots Collection of root vertices.  This collection
	will be used directly, without being cloned or copied into
	another collection; none of the graph algorithms will mutate
	it.

	@param fnavigator forward digraph navigator */
    public static <Vertex> DiGraph<Vertex> diGraph
	(final Collection<Vertex> roots, final ForwardNavigator<Vertex> fnavigator) {
	return new DiGraph<Vertex>() {
	    public Collection<Vertex> getRoots() { return roots; }
	    public ForwardNavigator<Vertex> getForwardNavigator() { 
		return fnavigator;
	    }
	};
    }

    /** @return Set of all transitive and reflexive successors of
        <code>vertex</code>.  The caller is free to mutate the
        returned set. */
    public Set<Vertex> transitiveSucc(Vertex vertex) {
    	return transitiveSucc(Collections.singleton(vertex));
    }

    /** @return Set of all transitive and reflexive successors of
        vertices from <code>roots</code>.  The caller is free to
        mutate the returned set. */
    public Set<Vertex> transitiveSucc(Collection<Vertex> roots) {
	return reachableVertices(roots, getForwardNavigator());
    }


    /** @return Set of all transitive and reflexive successors of
        vertices from <code>roots</code>, but not navigating through
        the frontier vertices indicated by the predicate
        <code>frontier</code>.  This is equivalent to the classic
        {@link #transitiveSucc(Collection)} in a directed graph
        identical to this one, except that no arc exits the frontier
        vertices.  The boolean parameter <code>includeFrontier</code>
        indicates whether to include the reachable frontier vertices
        in the returned set. */
    public Set<Vertex> transitiveSuccWithFrontier(Collection<Vertex> roots,
						  final Predicate<Vertex> frontier,
						  boolean includeFrontier) {
	final ForwardNavigator<Vertex> origFwdNav = this.getForwardNavigator();
	Set<Vertex> reachable = 
	    DiGraph.<Vertex>diGraph
	    (roots,
	     new ForwardNavigator<Vertex>() {
		public List<Vertex> next(Vertex node) {
		    if(frontier.check(node)) return Collections.<Vertex>emptyList();
		    return origFwdNav.next(node);
		}
	    }).vertices();

	if(!includeFrontier) {
	    Set<Vertex> reachable2 = new LinkedHashSet<Vertex>();
	    for(Vertex v : reachable) {
		if(!frontier.check(v)) {
		    reachable2.add(v);
		}
	    }
	    reachable = reachable2;
	}
	return reachable;
    }


    /** @return Set of all transitive and reflexive predecessors of
        <code>vertex</code>.  The caller is free to mutate the
        returned set.  */
    public Set<Vertex> transitivePred(Vertex vertex) {
    	return transitivePred(Collections.singleton(vertex));
    }

    /** @return Set of all transitive and reflexive predecessors of
        the vertices from <code>roots</code>.  The caller is free to
        mutate the returned set. */
    public Set<Vertex> transitivePred(Collection<Vertex> roots) {
	return reachableVertices
	    (roots, 
	     GraphUtil.<Vertex>reverseBiDiNavigator(getBiDiNavigator()));
    }


    /** @return Set of all transitive and reflexive predecessors of
        vertices from <code>roots</code>, but not navigating through
        the frontier vertices indicated by the predicate
        <code>frontier</code>.  This is equivalent to the classic
        {@link #transitivePred(Collection)} in a directed graph
        identical to this one, except that no arc enters the frontier
        vertices.  The boolean parameter <code>includeFrontier</code>
        indicates whether to include the reachable frontier vertices
        in the returned set. */
    public Set<Vertex> transitivePredWithFrontier(Collection<Vertex> roots,
						  final Predicate<Vertex> frontier,
						  boolean includeFrontier) {
	return this.reverseDiGraph().transitiveSuccWithFrontier(roots, frontier, includeFrontier);
    }


    /** @return Set of all transitive and reflexive successors of the
        vertices from <code>roots</code>, where the successors of a
        vertex are given by method <code>next</code> of
        <code>fnavigator</code>.  The caller is free to mutate the
        returned set. */
    private static <Vertex> Set<Vertex> reachableVertices
	(Collection<Vertex> roots,
	 ForwardNavigator<Vertex> fnavigator) {
	return 
	    (new ClosureDFS<Vertex>()).doIt(roots, fnavigator, null, null);
    }


    /** @return One shortest path (as number of arcs) from one of the
	vertices from <code>sources</code> to a vertex that satisfies
	the <code>predDest</code> predicate, along arcs given by the
	forward navigator <code>navigator</code>; returns
	<code>null</code> if no such path exists.  The caller is free
	to mutate the returned list.  */
    private static <Vertex> List<Vertex> findPath
	(Collection<Vertex> sources, Predicate<Vertex> predDest, ForwardNavigator<Vertex> navigator) {

	NonIterableMap<Vertex,Vertex> pred = new NonIterableMap<Vertex,Vertex>();
	NonIterableSet<Vertex> reachables  = new NonIterableSet<Vertex>();
	LinkedList<Vertex> w = new LinkedList<Vertex>();

	Vertex foundDest = null;
	
	for(Vertex source : sources) {
	    reachables.add(source);
	    w.addLast(source);
	    if(predDest.check(source)) {
		foundDest = source;
		break;
	    }
	}

	while(!w.isEmpty() && (foundDest == null)) {
	    Vertex vertex = w.removeFirst();
	    // explore the successors
	    for(Vertex succ : navigator.next(vertex)) {
		if(reachables.add(succ)) {
		    // Newly reached node -> do serious processing:
		    // 1. remember how we got to this vertex
		    pred.put(succ, vertex);
		    // 2. check whether we've reached the destination
		    if(predDest.check(succ)) {
			foundDest = succ;
			break;
		    }
		    // 3. add to the worklist for future exploration
		    w.addLast(succ);
		}
	    }
	}

	if(foundDest == null) {
	    // no luck ... 
	    return null;
	}

	// reconstruct discovered path
	LinkedList<Vertex> path = new LinkedList<Vertex>();
	path.addFirst(foundDest);
	Vertex curr = foundDest;
	while(true) {
	    Vertex vertex = pred.get(curr);
	    // sources are the only ones with no predecessors
	    if(vertex == null) break;
	    path.addFirst(vertex);
	    curr = vertex;
	}
	return path;
    }


    /** @return One shortest path of vertices from <code>source</code>
	to <code>dest</code>, along arcs indicated by
	<code>navigator</code>; returns <code>null</code> if no such
	path exists.  The caller is free to mutate the returned
	list. */
    public static <Vertex> List<Vertex> findPath
	(Vertex source, Vertex dest, ForwardNavigator<Vertex> navigator) {
	return 
	    findPath(Collections.<Vertex>singleton(source),
		     new EqualityPredicate<Vertex>(dest),
		     navigator);
    }


    /** @return One shortest path (as number of arcs) from
	<code>source</code> to <code>dest</code>, along arcs from
	<code>this</code> graph; returns <code>null</code> if no such
	path exists.  The caller is free to mutate the returned list.  */
    public List<Vertex> findPath(Vertex source, Vertex dest) {
	return findPath(source, dest, getForwardNavigator());
    }


    /** @return One shortest path (as number of arcs) from one of the
	vertices from <code>sources</code> to a vertex that satisfies
	the <code>predDest</code> predicate, along arcs from
	<code>this</code> graph; returns <code>null</code> if no such
	path exists.  The caller is free to mutate the returned list.  */
    public List<Vertex> findPath(Collection<Vertex> sources, Predicate<Vertex> predDest) {
	return findPath(sources, predDest, getForwardNavigator());
    }




    /** DFS traversal of <code>this</code> digraph.  The traversal may
	be stoped at any point by throwing an {@link
	jpaul.DataStructs.InterruptTraversalException
	InterruptTraversalException} from one of the visitors (the
	arguments of this method); the exception is caught internally
	by the implementation of this method.

	@param onEntry action executed when a node is first time
	visited by the DFS traversal

	@param onExit action executed after the DFS traversal of a
	node finished 

	@return Set of visited vertices.  The caller is free to mutate
	the returned set.  */
    public Set<Vertex> dfs(Action<Vertex> onEntry,
			   Action<Vertex> onExit) {
	return (new ClosureDFS<Vertex>()).doIt(getRoots(),
					       getForwardNavigator(),
					       onEntry,
					       onExit);
    }


    /** More customizable dfs traversal than that performed by the
	method {@link #dfs dfs}.  On entry to a specific
	(yet-unvisited) vertex <code>v</code>, the traversal adds the
	node to the set of visited vertices and performs the
	<code>onEntry</code> {@link jpaul.Misc.ActionPredicate
	ActionPredicate}.  If the value returned by
	<code>onEntry</code> is <code>true</code>, the visit of the
	vertex continues as in the case of the simple traversal {@link
	#dfs} with the (recursive) visit of the children and the
	execution of the action <code>onExit</code>.  OTHERWISE, the
	visit of the current vertex <code>v</code> stops immediately:
	no recursive visit of its children, no execution of the exit
	action.

	<p>Notice that the <code>onEntry</code> parameter is a
	combination of a predicate and an action.  Initially, we
	thought about having two parameters: a predicate and an
	action.  Still, that solution would left unspecified the
	problem of the order between the predicate and the action.
	Worse still, one can imagine the action being split into two
	parts: one executed (unconditionally) before the predicate,
	and the second one executed (conditionally) after the
	predicate.  To simplify things, we decided to allow the client
	to specify the desired combination of an action and a
	predicate.
	
	<p>As in the case of the "simple" {#dfs dfs}, the traversal
	may be stoped at any point by throwing an {@link
	jpaul.DataStructs.InterruptTraversalException
	InterruptTraversalException} from one of the visitors (the
	arguments of this method); the exception is caught internally
	by the implementation of this method.  Notice the difference
	between the case when <code>onEntry</code> returns
	<code>false</code> and the case when <code>onEntry</code> or
	<code>onExit</code> throws an {@link
	jpaul.DataStructs.InterruptTraversalException
	InterruptTraversalException}: in the first case, we skip only
	the (recursive) visit of the current vertex's children; in the
	second case, we skip the visit of all the remaining vertices.

	@param onEntry action executed when a node is first time
	visited by the DFS traversal

	@param onExit action executed after the DFS traversal of a
	node finished 

	@return Set of visited vertices.  The caller is free to mutate
	the returned set.  */
    public Set<Vertex> dfs2(ActionPredicate<Vertex> onEntry,
			    Action<Vertex> onExit) {
	return (new ClosureDFS2<Vertex>()).doIt(getRoots(),
						getForwardNavigator(),
						onEntry,
						onExit);
    }

    private static class ClosureDFS<Vertex> {
	public  Set<Vertex> visited;
	private Action<Vertex> onEntry;
	private Action<Vertex> onExit;
	private ForwardNavigator<Vertex> fnav;
	
	public Set<Vertex> doIt(Collection<Vertex> roots,
				ForwardNavigator<Vertex> fnav,
				Action<Vertex> onEntry,
				Action<Vertex> onExit) {

	    this.fnav    = fnav;
	    this.onEntry = onEntry;
	    this.onExit  = onExit;
	    this.visited = new LinkedHashSet<Vertex>();

	    try {
		for(Vertex root : roots) {
		    dfs_visit(root);
		}
	    }
	    catch(InterruptTraversalException itex) {
		// Do nothing; InterruptTraversalException is only a
		// way to terminate the traversal prematurely.
	    }

	    return visited;
	}


	private void dfs_visit(Vertex v) {
	    // skip already visited nodes
	    if(!visited.add(v)) return;

	    if(onEntry != null)
		onEntry.action(v);

	    for(Vertex v2 : fnav.next(v))
		dfs_visit(v2);

	    if(onExit != null)
		onExit.action(v);
	}
    }


    // YES, this is a horrible case of code duplication.  Still, I
    // felt that performance may be important for dfs traversals, and
    // I didn't want to have an extra wrapper to convert Actions into
    // tautologic ActionPredicates.
    private static class ClosureDFS2<Vertex> {
	public  Set<Vertex> visited;
	private ActionPredicate<Vertex> onEntry;
	private Action<Vertex> onExit;
	private ForwardNavigator<Vertex> fnav;
	
	public Set<Vertex> doIt(Collection<Vertex> roots,
				ForwardNavigator<Vertex> fnav,
				ActionPredicate<Vertex> onEntry,
				Action<Vertex> onExit) {

	    this.fnav    = fnav;
	    this.onEntry = onEntry;
	    this.onExit  = onExit;
	    this.visited = new LinkedHashSet<Vertex>();

	    try {
		for(Vertex root : roots) {
		    dfs_visit(root);
		}
	    }
	    catch(InterruptTraversalException itex) {
		// Do nothing; InterruptTraversalException is only a
		// way to terminate the traversal prematurely.
	    }

	    return visited;
	}


	private void dfs_visit(Vertex v) {
	    // skip already visited nodes
	    if(!visited.add(v)) return;

	    if((onEntry != null) && !onEntry.actionPredicate(v)) {
		// The client wants to skip the traversal of the
		// vertex v (including the traversal of v's children,
		// and the onExit action).
		return;
	    }

	    for(Vertex v2 : fnav.next(v))
		dfs_visit(v2);

	    if(onExit != null)
		onExit.action(v);
	}
    }


    /** Executes an action at most once for each vertex from
        <code>this</code> directed graph.  The traversal may be
        terminated at each moment by throwing an {@link
        jpaul.DataStructs.InterruptTraversalException
        InterruptTraversalException}.  If no such exception is thrown,
        then <code>action</code> is applied exactly once to each
        vertex from this digraph.  The
        <code>InterruptTraversalException</code> (if any) is caught
        inside this method.

	@see #vertices() */
    public void forAllVertices(Action<Vertex> action) {
	dfs(action, null);
    }
    

    /** Returns the component graph for <code>this</code> graph.  The
        &quot;component graph&quot; of a graph <code>G</code> is the
        directed, acyclic graph consisting of the strongly connected
        components of <code>G</code>.  As it doesn't affect the
        asymptotic complexity, we also sort it topologically. */
    public TopSortedCompDiGraph<Vertex> getComponentDiGraph() {
	return new TopSortedCompDiGraph<Vertex>(this);
    }

    
    /** Returns the set of all vertices from <code>this</code>
	digraph: vertices that are (trasitively and reflexively)
	reachable from the root vertices by following the forward arcs
	provided by the navigator.

	<p>The returned set is unmodifiable and has a deterministic
	iteration order (it's based on a <code>LinkedHashSet</code>).
	Moreover, if the following conditions are met, different calls
	to <code>vertices()</code> return equal sets of vertices, with
	the same deterministic iteration order; here are the
	conditions: (1) the navigator and the set of graph roots do
	not change, (2) for each node, the forward graph navigator
	always returns the same list of neighbors (instead of
	re-arragements of them), (3) the set of roots has a
	deterministic iteration order. */
    public Set<Vertex> vertices() {
	if(CACHING && (cachedVertices != null)) 
	    return cachedVertices;
	
	Set<Vertex> vertices = Collections.unmodifiableSet(transitiveSucc(getRoots()));

	if(CACHING) {
	    cachedVertices = vertices;
	}

	return vertices;
    }
    private Set<Vertex> cachedVertices;
    
    /** Returns a reverse form of <code>this</code> directed graph: a
        directed graph with the same set of vertices, that contains an
        arc from <code>v1</code> to <code>v2</code> iff
        <code>this</code> graph contains an arc from <code>v2</code>
        to <code>v1</code>. */
    public DiGraph<Vertex> reverseDiGraph() {
	final DiGraph<Vertex> origDiGraph = this;
	return new DiGraph<Vertex>() {
	    public Set<Vertex> getRoots() {
		return origDiGraph.vertices();
	    }
	    public BiDiNavigator<Vertex> getBiDiNavigator() {
		return
		    GraphUtil.<Vertex>reverseBiDiNavigator(origDiGraph.getBiDiNavigator());
	    }
	};
    }

    /** Returns a subgraph of <code>this</code> directed graph
	containing only the vertices from <code>verts</code>. */
    public DiGraph<Vertex> subDiGraph(final Collection<Vertex> verts) {
	final BiDiNavigator<Vertex> origNav = this.getBiDiNavigator();
	return DiGraph.diGraph
	    (verts,
	     new BiDiNavigator<Vertex>() {
		public List<Vertex> next(Vertex v) {
		    return
			(List<Vertex>)
			diff(origNav.next(v), verts, new LinkedList<Vertex>());
		}
		public List<Vertex> prev(Vertex v) {
		    return
			(List<Vertex>)
			diff(origNav.prev(v), verts, new LinkedList<Vertex>());
		}
		
		private <T> Collection<T> diff(Iterable<T> a,
					       Collection<T> b,
					       Collection<T> res) {
		    for(T t : a) {
			if(b.contains(t)) {
			    res.add(t);
			}
		    }
		    return res;
		}
	    });
    }


    /** Returns the number of vertices in <code>this</code> directed
        graph.

	Complexity: linear in the size of the graph.  Cached if the
	<code>CACHING</code> argument to the constructor was true. */
    public int numVertices() {
	if(CACHING && (cachedNumVertices != -1)) {
	    return cachedNumVertices;
	}	
	int numVertices = vertices().size();       
	if(CACHING) {
	    cachedNumVertices = numVertices;
	}
	return numVertices;
    }
    // cached number of vertices; meaning ful only if CACHING is true
    private int cachedNumVertices = -1;


    /** Returns the number of arcs in <code>this</code> directed
        graph.

	Complexity: linear in the size of the graph.  Cached if the
	<code>CACHING</code> argument to the constructor was true. */
    public long numArcs() {
	if(CACHING && (cachedNumArcs != -1)) {
	    return cachedNumArcs;
	}
	long numArcs = 0;
	for(Vertex v : vertices()) {
	    numArcs += getForwardNavigator().next(v).size();
	}
	if(CACHING) {
	    cachedNumArcs = numArcs;
	}
	return numArcs;
    }
    // cached number of vertices; meaningful only if CACHING is true
    private long cachedNumArcs = -1;


    /** Returns a string representation of <code>this</code> DiGraph.
        This string representation is adequate for debugging small
        graphs.  For more complex graphs, try to output a dot
        representation of the graph and use an external tool to
        visualize it. */
    public String toString() {
	StringBuffer buff = new StringBuffer();
	ForwardNavigator<Vertex> fwdNav = getForwardNavigator();
	Collection<Vertex> roots = getRoots();
	buff.append("{\n");
	for(Vertex v : vertices()) {
	    List<Vertex> succs = fwdNav.next(v);
	    if(succs.isEmpty()) continue;
	    buff.append("\t");
	    buff.append(v.toString());
	    if(roots.contains(v)) {
		buff.append(" (root) ");
	    }
	    buff.append(" ->");
	    for(Vertex succ : succs) {
		buff.append(" ");
		buff.append(succ.toString());
	    }
	    buff.append("\n");
	}
	buff.append("}");
	return buff.toString();
    }


    /** Computes the union of two directed graphs.  The resulting
        directed graph contains all the arcs of the two original
        directed arcs.  The union <code>DiGraph</code> is not computed
        explicitly; instead, the lists of predecessors/successors are
        generated on-demand. */
    public static <V> DiGraph<V> union(final DiGraph<V> dg1, final DiGraph<V> dg2) {
	return new DiGraph<V>() {
	    public Collection<V> getRoots() {
		return DSUtil.unionColl(dg1.getRoots(), dg2.getRoots());
	    }
	    public BiDiNavigator<V> getBiDiNavigator() {
		return GraphUtil.<V>unionNav(dg1.getBiDiNavigator(),
					     dg2.getBiDiNavigator());
	    }
	    public ForwardNavigator<V> getForwardNavigator() {
		return GraphUtil.<V>unionFwdNav(dg1.getForwardNavigator(),
						dg2.getForwardNavigator());
	    }
	};
    }

}
