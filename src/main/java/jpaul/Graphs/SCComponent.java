// SCComponent.java, created Mon Jan 24 19:26:30 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.AbstractSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Collection;

import java.io.Serializable;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;
import jpaul.DataStructs.ArraySet;

/**
 * <code>SCComponent</code> models a <i>strongly-connected
 * component</i> of a directed graph: a set of vertices such that
 * there is a path between any two of them.
 *
 * The main way of spliting a digraph into <code>SCComponent</code>s
 * is using the method <code>buildScc</code>.  Alternatively, given a
 * <code>DiGraph</code>, one may construct a
 * <code>TopSortedCompDiGraph</code> (using its constructor).
 *
 * @see DiGraph
 * @see TopSortedCompDiGraph
 * @see SCComponent#buildScc
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SCComponent.java,v 1.13 2006/03/14 02:55:23 salcianu Exp $
 */
public final class SCComponent<Vertex>
    implements Comparable<SCComponent<Vertex>>, Serializable {

	private static final long serialVersionUID = 6087863634983845607L;

	/** Default navigator through a component graph (a diGraph of
     * strongly-connected components).  */
    public static <Vertex> BiDiNavigator<SCComponent<Vertex>> getSccBiDiNavigator() {
	return
	    new BiDiNavigator<SCComponent<Vertex>>() {
	       public List<SCComponent<Vertex>> next(SCComponent<Vertex> scc) {
		  return scc.next();
	       }
	       public List<SCComponent<Vertex>> prev(SCComponent<Vertex> scc) {
		  return scc.prev();
	       }
	    };
    }


    // THE FIRST PART CONTAINS JUST SOME STATIC METHODS & FIELDS

    /** Splits a directed graph into the set of its strongly-connected
	components.  The complexity is linear in the size of the
	original digraph (nb. of vertices + nb. of arcs).  We use the
	algorithm from the DFS-based algorithm from Cormen, Leiserson,
	Rivest, MIT Press, 19th Edition, 1997, Section 23.5, an
	efficient and simple algorithm.

	@param diGraph Directed graph

	@return Set of top-level strongly-connected components (ie,
	SCCs that are not pointed to by anybody).  The client may
	mutate this set, with no effect on the original digraph
	<code>diGraph</code>. */
    public static final <Vertex> Set<SCComponent<Vertex>>
	buildScc(final DiGraph<Vertex> diGraph) {
	return
	    (new BuildSCCClosure<Vertex>()).doIt(diGraph.getRoots(),
						 diGraph.getBiDiNavigator());
    }


    // OO programming is great, especially when one encodes functional
    // programming into it :)
    // BuildSCCClosure.doIt computes the strongly-connected
    // components, using a DFS-based algorithm from CLR (page 488).
    private static class BuildSCCClosure<Vertex> {
	// set of vertices reachable from root
	private Set<Vertex> reachable_vertices;
	// set of reached vertices (to avoid reanalyzing them "ad infinitum")
	private Set<Vertex> visited_vertices;
	// Mapping vertex -> SCComponent
	private Map<Vertex,SCComponent<Vertex>> v2scc;
	// The vector of the reached vertices, in the order DFS finished them
	private Vector<Vertex> vertices_vector;
	// vector to put the generated SCCs in.
	private Vector<SCComponent<Vertex>> scc_vector;

	// currently generated strongly connected component
	private SCComponent<Vertex> current_scc = null;
	// the navigator used in the DFS algorithm
	private BiDiNavigator<Vertex> nav = null;

	// does the real work behind SCComponent.buildScc
	public final Set<SCComponent<Vertex>> doIt
	    (final Collection<Vertex> roots,
	     final BiDiNavigator<Vertex> navigator) {
	    scc_vector     = new Vector<SCComponent<Vertex>>();
	    visited_vertices  = new LinkedHashSet<Vertex>();
	    vertices_vector   = new Vector<Vertex>();
	    v2scc          = new LinkedHashMap<Vertex,SCComponent<Vertex>>();

	    // STEP 1: DFS exploration; add all reached vertices in
	    // "vertices_vector", in the order of their "finished" time.
	    direct_dfs(roots, navigator);

	    // STEP 2. build the SCCs by doing a DFS in the reverse graph.
	    reverse_dfs(navigator);

	    // produce the final formal SCCs
	    build_final_sccs(navigator);
	    return get_root_sccs(roots);
	}


	private final void direct_dfs(Collection<Vertex> roots, BiDiNavigator<Vertex> navigator) {
	    nav = navigator;
	    for(Vertex vertex : roots)
		dfs_first(vertex);
	}

	// DFS for the first step: the "forward" navigation
	private final void dfs_first(Vertex node) {
	    // skip already visited vertices
	    if(visited_vertices.contains(node)) return;

	    visited_vertices.add(node);
	    
	    for(Vertex next : nav.next(node)) {
		dfs_first(next);
	    }
	    
	    vertices_vector.add(node);
	}


	private final void reverse_dfs(BiDiNavigator<Vertex> navigator) {
	    // "reverse" navigator
	    nav = GraphUtil.<Vertex>reverseBiDiNavigator(navigator);

	    // Explore the vertices in the decreasing order of their
	    // "finished" time.  For each unvisited vertex, grab all
	    // vertices reachable on the reverse navigator.  No
	    // inter-SCC arcs yet.  Put SCCs in scc_vector.
	    reachable_vertices = visited_vertices;
	    visited_vertices = new LinkedHashSet<Vertex>();
	    for(int i = vertices_vector.size() - 1; i >= 0; i--) {
		Vertex node = vertices_vector.elementAt(i);
		// explore vertices that are still unanalyzed
		if(!visited_vertices.contains(node)) {
		    current_scc = new SCComponent<Vertex>();
		    scc_vector.add(current_scc);
		    dfs_second(node);
		}
	    }
	}

	// DFS for the second step: the "backward" navigation.
	private final void dfs_second(Vertex node) {
	    // only vertices reachable from root vertices count: we make
	    // sure that navigator.prev does not take us to strange
	    // places!
	    if(visited_vertices.contains(node) ||
	       !reachable_vertices.contains(node)) return;
	    
	    visited_vertices.add(node);

	    v2scc.put(node, current_scc);
	    current_scc.vertices.add(node);
	    
	    for(Vertex next : nav.next(node)) {
		dfs_second(next);
	    }
	}
    

	// Build the final SCC.  This requires converting some sets to
	// arrays (and sorting them in the deterministic case).
	private final void build_final_sccs(BiDiNavigator<Vertex> navigator) {
	    nextRel        = new MapSetRelation<SCComponent<Vertex>,SCComponent<Vertex>>();
	    prevRel        = new MapSetRelation<SCComponent<Vertex>,SCComponent<Vertex>>();
	    scc2exits      = new MapSetRelation<SCComponent<Vertex>,Vertex>();
	    scc2entries    = new MapSetRelation<SCComponent<Vertex>,Vertex>();

	    // Put inter-SCCs arcs.
	    collect_arcs(navigator);
	    
	    for(SCComponent<Vertex> scc : scc_vector) {
		// We make a compromise between speed of membership
		// testing, and memory consumption (having too many
		// LinkedHashSets is BAD ...)
		if(scc.vertices.size() > 10) {
		    // big set of vertices: linear membership checking may be too expensive ->
		    // create a read HashSet (Linked to guarantee deterministic iterations) +
		    // immutability wrapper.
		    scc.vertices = Collections.unmodifiableSet(new LinkedHashSet<Vertex>(scc.vertices));
		}

		// add the arcs
		scc.next = Collections.unmodifiableList
		    (new LinkedList<SCComponent<Vertex>>(nextRel.getValues(scc)));
		scc.prev = Collections.unmodifiableList
		    (new LinkedList<SCComponent<Vertex>>(prevRel.getValues(scc)));

		// add the entries / exits
		scc.entries = new ArraySet<Vertex>(scc2entries.getValues(scc));
		scc.exits   = new ArraySet<Vertex>(scc2exits.getValues(scc));

		// record that scc is now fully initialized; it cannot be changed past this point
		scc.fullyInitialized = true;
	    }
	}

	// relation vertex -> successor vertices
	private Relation<SCComponent<Vertex>,SCComponent<Vertex>> nextRel;
	// scc -> set of exit vertices
	private Relation<SCComponent<Vertex>,Vertex> scc2exits;
	// relation vertex -> predecessor vertices
	private Relation<SCComponent<Vertex>,SCComponent<Vertex>> prevRel;
	// scc -> set of entry vertices
	private Relation<SCComponent<Vertex>,Vertex> scc2entries;


	// Collect inter-SCCs arcs in nextRel/prevRel: there is an arc
	// from scc1 to scc2 iff there is at least one pair of vertices
	// n1 in scc1 and n2 in scc2 such that there exists an arc
	// from n1 to n2.
	private final void collect_arcs(final BiDiNavigator<Vertex> navigator) {
	    for(SCComponent<Vertex> scc1 : scc_vector) {
		for(Vertex v1 : scc1.vertices) {
		    for(Vertex v2 : navigator.next(v1)) {
			SCComponent<Vertex> scc2 = v2scc.get(v2);
			if(scc1 == scc2) {
			    scc1.loop = true;
			}
			else {
			    nextRel.add(scc1, scc2);
			    prevRel.add(scc2, scc1);
			    scc2exits.add(scc1, v1);
			    scc2entries.add(scc2, v2);
			}
		    }
		}
	    }
	}

	// Compute set of root SCCs.
	private final Set<SCComponent<Vertex>> get_root_sccs(Collection<Vertex> roots) {
	    Set<SCComponent<Vertex>> root_sccs = new LinkedHashSet<SCComponent<Vertex>>();
	    for(Vertex root_vertex : roots) {
		SCComponent<Vertex> scc = v2scc.get(root_vertex);
		if(scc.prev().isEmpty())
		    root_sccs.add(scc);
	    }
	    return root_sccs;
	}
    }


    // HERE STARTS THE REAL (i.e. NON STATIC) CLASS

    // The only way to produce SCCs is through SCComponent.buildSSC !
    private SCComponent() { id = count++; }
    private static int count = 0;

    /** Returns the numeric ID of <code>this</code> <code>SCComponent</code>.
	Just for debug purposes ... */
    public int getId() { return id; }
    private int id;

    // The vertices of this SCC (Strongly Connected Component):
    // initially a list that only the SCC construction algorithm may
    // write to.  After the construction is done, big sets are
    // converted into unmodifiable hashsets.
    private Set<Vertex> vertices = new ListAsSet<Vertex>();

    // successors / predecessors
    private List<SCComponent<Vertex>> next;
    private List<SCComponent<Vertex>> prev;

    // entries/exists into this SCC
    private Set<Vertex> entries;
    private Set<Vertex> exits;

    /** Checks whether <code>this</code> strongly connected component
	corresponds to a loop, <i>ie</i> it has at least one arc to
	itself. */
    public final boolean isLoop() { return loop; }
    // is there any arc to itself?
    private boolean loop;


    public int compareTo(SCComponent<Vertex> scc2) {
	int id2 = scc2.id;
	if(id  < id2) return -1;
	if(id == id2) return 0;
	return 1;
    }
    
    /** Returns the SCCs that <code>this</code> SCC points to in the
        component digraph.  Returns an unmodifiable list.  Returns a
        list (instead of a set), in order to be consistent with the
        {@link jpaul.Graphs.BiDiNavigator navigator} methods. */
    public final List<SCComponent<Vertex>> next() { return next; }

    /** Returns the SCCs that point to <code>this</code> SCC in the
        component digraph.  Returns an unmodifiable list.  Returns a
        list (instead of a set), in order to be consistent with the
        {@link jpaul.Graphs.BiDiNavigator navigator} methods.  */
    public final List<SCComponent<Vertex>> prev() { return prev; }

    /** Returns the vertices of <code>this</code> strongly connected
        component.  Returns an umodifiable set. */
    public final Set<Vertex> vertices() { return vertices; }

    /** Returns the number of vertices in <code>this</code> strongly connected
	component. */
    public final int size() { return vertices.size(); }

    /** Checks whether <code>node</code> belongs to <code>this</code> \
	strongly connected component. */
    public final boolean contains(Vertex node) { return vertices.contains(node); }

    /** Returns the entry vertices of <code>this</code> strongly
        connected component.  These are the vertices that are
        reachable from outside the component.  Returns an unmodifiable
        set. */
    public final Set<Vertex> entryVertices() { return entries; }

    /** Returns the exit vertices of <code>this</code> strongly
        connected component.  These are the vertices that have arcs
        toward vertices outside the component.  Returns an
        unmodifiable set. */
    public final Set<Vertex> exitVertices()   { return exits; }


    /** Pretty-print method for debugging. */
    public final String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("SCC" + id + " (size " + size() + ") {\n");
	for(Vertex v : vertices()) {
	    buffer.append(v);
	    buffer.append("\n");
	}
	buffer.append("}\n");
	// string representation of the "prev" links.
	if(!prev().isEmpty()) {
	    buffer.append("Prev:");
	    for(SCComponent<Vertex> prev_scc : prev()) {
		buffer.append(" SCC" + prev_scc.getId());
	    }
	    buffer.append("\n");
	}

	// string representation of the "next" links.
	if(!next().isEmpty()) {
	    buffer.append("Next:");
	    for(SCComponent<Vertex> next_scc : next()) {
		buffer.append(" SCC" + next_scc.getId());
	    }
	    buffer.append("\n");
	}
	return buffer.toString();
    }

    // once this becomes true, it cannot be changed again
    private boolean fullyInitialized = false;

    // Set backed by a list of unique elements, customized for our
    // usage.  It's used to represent the set of vertices from an SCC.
    // During SCC construction, we add vertices to a ListAsSet, BUT we
    // make sure (in our alg.) NOT to add the same element twice: for
    // speed purposes, this class does not check element uniqueness by
    // itself.
    //
    // After the SCC this set is part of is fully initialized, the set
    // become unmodifiable (see method add() below).  This is faster
    // than Collections.unmodifiableSet which adds an extra level of
    // indirection.
    //
    // [ Note: the usage conditions on this class are so severe, we
    // decided it's better not to have it as a public class in
    // jpaul.DataStructs ]
    private class ListAsSet<T> extends AbstractSet<T> {
	// underlying list
	private final List<T> list = new ArrayList<T>();

	// NOTE: the client of this "set" must make sure no element is added twice
	public boolean add(T elem) {
	    if(fullyInitialized) {
		throw new UnsupportedOperationException("trying to change a fully initialized SCC");
	    }
	    list.add(elem);
	    return true;
	}
	
	public Iterator<T> iterator() {
	    return list.iterator();
	}

	public int size() {
	    return list.size();
	}
    }

}
