// ConstraintSystem.java, created Sat Jun 18 21:30:40 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ArcBasedDiGraph;
import jpaul.Graphs.SCComponent;
import jpaul.Graphs.TopSortedCompDiGraph;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;
import jpaul.DataStructs.UnionFind;
import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.NonIterableSet;
import jpaul.DataStructs.NonIterableMap;

import jpaul.DataStructs.WorkSet;
import jpaul.DataStructs.WorkStack;
import jpaul.DataStructs.WorkPriorityQueue;

import java.io.PrintStream;

/**
   <code>ConstraintSystem</code> is an efficient solver for a system
   of constraints.  Given a collection of constraints, this solver
   constructs an optimized form of them (mainly by unifying
   provably-equal variables) and computes a few internal data
   structures for the fixed-point solver.  Later, we can solve the
   system a few times, obtaining different least fixed-point solutions
   if some of the constraints use external values that change.  Hence,
   the name of this class: it is not just a solver, it is also an
   optimized, "ready-to-solve" constraint system.

   <p>A constraint system may contain variables that take values in
   different lattices: e.g., the values of some variables may be sets,
   while the values of others may be relations etc.  All value
   lattices must be represented by subclasses of the type parameter
   <code>Info</code>, and all variables must be subclasses of the type
   parameter <code>V</code> (that is itself a subtype of
   <code>Var&lt;Info&gt;</code>).  The relevant operations for each value
   lattice are represented as methods of the variables that take
   values in that lattice (see {@link Var#copy copy} and {@link
   Var#join join} in the {@link Var} class).

   This solver performs the following optimizations:

   <ul>
   
   <li><b>Unification of equal variables:</b> The solver treats {@link
   LtConstraint}s specially to detect strongly connected components of
   mutually smaller variables.  Such variable can be unified into a
   single one.  Next, for each variable <code>vd</code> such that the
   only constraint that modifies it has the form
   <code>LtConstraint(vd,vd)</code>, we can unify <code>vs</code> and
   <code>vd</code>: in the least fixed-point solution, <code>vd</code>
   has the same value as <code>vs</code>.

   <p>
   <li><b>Iterating over strongly connected components:</b> The
   fixed-point solver uses the <code>in</code> and <code>out</code>
   sets of each constraint to compute the dependencies between
   variables, compute the strongly connected components, and iterate
   over them, one by one, in topological order.  This is much more
   efficient than chaotic iteration over all the constraints.

   <p>
   <li><b>Cheap constraints first:</b> Inside each strongly
   connected component, the fixed-point solver does not execute an
   expensive constraint before iterating to saturation (i.e., no
   further progress possible) over the cheaper constraints.  This is
   achieved by using a {@link jpaul.DataStructs.WorkPriorityQueue
   WorkPriorityQueue} in the fixed-point solver.

   </ul>

   @see Var
   @see Constraint
 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: ConstraintSystem.java,v 1.26 2006/06/07 02:35:11 salcianu Exp $ */
public class ConstraintSystem<V extends Var<Info>, Info> {

    /** If on, the solver will check that each constraint reads/writes
        only variables declared in its in/out set.  Off by default.
        If the constraints are simple, these safety test may be very
        costly because they involve frequent collection membership
        tests.  */
    public static boolean CHECK_IN_OUT = false;

    /** Turns on several debugging messages and tests.  Off by default. */
    public static boolean DEBUG = false;

    private static boolean DEBUG_VER = false;
    

    /** Creates a <code>ConstraintSystem</code>.  Takes a collection
        of constraints, simplifies them by unifying variables known to
        be equal (e.g., because they are mutually smaller than one
        another), and makes the constraint system ready to be solved.
        Later, we can solve this system a few times, obtaining
        different solutions if some of the constraints use external
        values that change. */
    public ConstraintSystem(Collection<Constraint<V,Info>> cs) {
	findAllVars(cs);

	cs = unifyEquals(cs);

	buildSolverStructs(cs);

	v2version  = new NonIterableMap<V,int[]>(vertexHashCapacity);
	c2versions = new NonIterableMap<Constraint<V,Info>,int[]>((3 * cs.size()) / 2);
    }


    private void findAllVars(Collection<Constraint<V,Info>> cs) {
	vars = new LinkedHashSet<V>();
	for(Constraint<V,Info> c : cs) {
	    vars.addAll(c.in());
	    vars.addAll(c.out());
	}
    }
    private Set<V> vars;

    /** Returns the set of all variables from this constraint system.
        The reason this method is public is that we want external
        entities to be able to estimate the effect of the various
        variable-unification optimizations by comparing the set of all
        variables with the set of variables that remain after
        unification (see {@link #debugUniqueVars}). */
    public Set<V> vars() { return vars; }


    private Collection<Constraint<V,Info>> unifyEquals(Collection<Constraint<V,Info>> cs) {
	cs = new LinkedList<Constraint<V,Info>>(cs);
	uf = new UnionFind<V>();
	unifyMutuallySmaller(cs);
	boolean changed = true;
	while(changed) {
	    changed = unifySingleLt(cs, false);
	    if(DEBUG) unifySingleLt(cs, true);
	    if(eliminateEmpty(cs)) {
		changed = true;
	    }
	    if(changed) {
		cs = updateConstraints(cs);
	    }
	}

	int nbDisjointVars = 0;
	for(V v : vars()) {
	    V parent = uf.find(v);
	    if(v == parent) nbDisjointVars++;
	}
	vertexHashCapacity = (3 * nbDisjointVars) / 2;

	return cs;
    }
    // Union-find structure to maintain the sets of equal variables
    private UnionFind<V> uf;
    // the initial capacity for all maps/sets with fdisjoint (i.e., non-unified) vertices
    private int vertexHashCapacity = 16;


    // unify groups of mutually smaller variables
    private void unifyMutuallySmaller(Collection<Constraint<V,Info>> cs) {
	// construct digraph generated by "less than" constraints
	List<Pair<V,V>> edges = new LinkedList<Pair<V,V>>();
	for(Constraint<V,Info> c : cs) {
	    if(c instanceof LtConstraint/*<Info>*/) {
		LtConstraint<V,Info> ltc = (LtConstraint<V,Info>) c;
		edges.add(new Pair<V,V>(ltc.vs, ltc.vd));
	    }
	}
	DiGraph<V> dg = new ArcBasedDiGraph<V>(edges);
	// for each SCC of mutually "smaller" vars,
	for(SCComponent<V> scc : (new TopSortedCompDiGraph<V>(dg)).incrOrder()) {
	    // unify all the vars from the SCC
	    V major = null; 
	    for(V v : scc.vertices()) {
		if(major == null) {
		    major = v;
		}
		else {
		    uf.union(v, major);
		}
	    }
	}

	// remove superfluous inclusions of the form "v <= v"
	for(Iterator<Constraint<V,Info>> it = cs.iterator(); it.hasNext(); ) {
	    Constraint<V,Info> c = it.next();
	    if(c instanceof LtConstraint/*<V,Info>*/) {
		LtConstraint<V,Info> ltc = (LtConstraint<V,Info>) c;
		V vs = uf.find(ltc.vs);
		V vd = uf.find(ltc.vd);
		if(vs.equals(vd)) {
		    if(DEBUG_VER) System.out.println("Removing " + c);
		    it.remove();
		}
	    }
	}

    }


    // Search variables vd such that the only constraint that has vd
    // on the right side has the form "vs <= vd".  For each such
    // finding, unify vs and vd.
    //
    // NOTE: all this processing is done modulo the already-found unification 
    // (all constraints have to be projected through uf).
    //
    // NOTE2: if CHECK is true, the procedure will just check that no
    // further unifications of this form are posible, and throw an
    // error if this is not true.  This is useful for debugging, in
    // order to make sure that the fixed point alg. really finds all
    // unifications.
    private boolean unifySingleLt(Collection<Constraint<V,Info>> cs, boolean CHECK) {
	// collect in bad all variables that are sources of non-inclusion constraints
	NonIterableSet<V> bad = new NonIterableSet<V>();
	// construct successor relation (s2d) and predecessor relation (d2s)
	Relation<V,V> s2d = new MapSetRelation<V,V>();
	Relation<V,V> d2s = new MapSetRelation<V,V>();
	fillInSD(cs, s2d, d2s, bad);

	WorkSet<V> wToUnify = new WorkStack<V>();

	for(V vd : d2s.keys()) {
	    if(!bad.contains(vd) && (d2s.getValues(vd).size() == 1)) {
		if(CHECK)
		    throw new Error("Additional unification possibilities!");
		wToUnify.add(vd);
	    }
	}
	if(CHECK) return false;

	boolean modif = !wToUnify.isEmpty();

	while(!wToUnify.isEmpty()) {
	    V vd = wToUnify.extract();
	    Collection<V> sources = d2s.getValues(vd);
	    if(sources.size() != 1) {
		assert sources.size() == 0;
		continue;
	    }
	    V vs = DSUtil.<V>getFirst(sources);
	    
	    V vNew = uf.union(vs, vd);
	    if(DEBUG_VER) System.out.println("unify " + vs + " " + vd + " -> " + vNew);

	    // unify the constraints for vs and vd
	    if(bad.contains(vs)) {
		bad.remove(vs);
		bad.add(vNew);
	    }

	    changeOut(vs, vNew, d2s, s2d); // no typo!
	    s2d.remove(vs, vd);
	    d2s.remove(vd, vs);
	    changeOut(vs, vNew, s2d, d2s);
	    changeOut(vd, vNew, s2d, d2s);

	    s2d.remove(vNew, vNew);
	    d2s.remove(vNew, vNew);

	    if(!vs.equals(vNew) && wToUnify.contains(vs)) {
		wToUnify.add(vNew);
	    }

	    for(V v : s2d.getValues(vNew)) {
		if(!bad.contains(v) && (d2s.getValues(v).size() == 1)) {
		    wToUnify.add(v);
		}
	    }
	}
	
	return modif;
    }

    
    private void fillInSD(Collection<Constraint<V,Info>> cs, Relation<V,V> s2d, Relation<V,V> d2s, 
			  NonIterableSet<V> bad) {
	for(Constraint<V,Info> c : cs) {
	    if(c instanceof LtConstraint/*<Info>*/) {
		LtConstraint<V,Info> ltc = (LtConstraint<V,Info>) c;
		V src = uf.find(ltc.vs);
		V dst = uf.find(ltc.vd);
		if(!src.equals(dst)) {
		    s2d.add(src, dst);
		    d2s.add(dst, src);
		}
	    }
	    else {
		for(V v : c.out()) {
		    bad.add(uf.find(v));
		}
	    }
	}	
    }
    

    private void changeOut(V vOld, V vNew, Relation<V,V> s2d, Relation<V,V> d2s) {
	if(!vNew.equals(vOld)) {
	    for(V v : new LinkedHashSet<V>(s2d.getValues(vOld))) {
		s2d.remove(vOld, v);
		d2s.remove(v, vOld);
		s2d.add(vNew, v);
		d2s.add(v, vNew);
	    }
	}
    }

    private Collection<Constraint<V,Info>> updateConstraints(Collection<Constraint<V,Info>> cs) {
	Collection<Constraint<V,Info>> cs2 = new LinkedHashSet<Constraint<V,Info>>();
	for(Constraint<V,Info> c : cs) {
	    Constraint<V,Info> c2 = c.rewrite(uf);
	    if(c2 != null)
		cs2.add(c2);
	}
	return cs2;
    }


    private boolean eliminateEmpty(Collection<Constraint<V,Info>> cs) {
	// map variable -> count of incoming constraints (constraints
	// that may update that variable).  For speed, instead of
	// immutable Integers (that need to be created again and
	// again), we use mutable int arrays with 1 element.
	NonIterableMap<V,int[]> v2incCount = new NonIterableMap<V,int[]>();
	for(Constraint<V,Info> c : cs) {
	    for(V v : c.out()) {
		V v2 = uf.find(v);
		// count[0] is the counter for v2
		int[] count = v2incCount.get(v2);
		if(count == null) {
		    // no counter yet for v2; create one and initialize it to 1
		    v2incCount.put(v2, new int[]{1});
		}
		else {
		    // increment the counter for v2
		    count[0]++;
		}
	    }
	}

	if(DEBUG_VER) {
	    debugPrintV2incCount(v2incCount);
	}

	NonIterableSet<V> emptyVars = new NonIterableSet<V>();
	for(V v : vars()) {
	    V v2 = uf.find(v);
	    int[] count = v2incCount.get(v2);
	    if((count == null) || (count[0] == 0)) {
		emptyVars.add(v2);
	    }
	}
	
	if(DEBUG_VER) System.out.println("emptyVars = " + emptyVars);

	boolean modif = false;

	while(!emptyVars.isEmpty()) {

	    NonIterableSet<V> newEmptyVars = new NonIterableSet<V>();

	    for(Iterator<Constraint<V,Info>> it = cs.iterator(); it.hasNext(); ) {
		Constraint<V,Info> c = it.next();
		if(c instanceof LtConstraint/*<V,Info>*/) {
		    LtConstraint<V,Info> ltc = ((LtConstraint<V,Info>) c);
		    V vs = uf.find(ltc.vs);
		    if(emptyVars.contains(vs)) {
			it.remove();
			modif = true;
			V vd = uf.find(ltc.vd);
			int[] count = v2incCount.get(vd);
			count[0]--;
			if(count[0] == 0) {
			    newEmptyVars.add(vd);
			}
		    }
		}
	    }

	    emptyVars = newEmptyVars;
	}

	return modif;
    }


    // debug stuff
    private void debugPrintV2incCount(NonIterableMap<V,int[]> v2incCount) {
	System.out.print("v2incCount = [ ");
	for(V v : vars()) {
	    int count = v2incCount.get(v)[0];
	    if(count != 0) {
		System.out.print(", " + v + " -> " + count);
	    }
	}
	System.out.println( "]");
    }


    private void buildSolverStructs(Collection<Constraint<V,Info>> cs) {
	// 1. compute dependency graph between variables; an arc from
	// v to w signifies that the value of variable v influences
	// the value of variable w.
	DiGraph<V> deps = new ArcBasedDiGraph<V>(dependencies(cs));
	// 2. compute sets of mutually dependent variables, and sort
	// them topologically
	ts_deps = new TopSortedCompDiGraph<V>(deps);

	// map from each variable to the SCC it belongs to
	Map<V,SCComponent<V>> v2scc = ts_deps.getVertex2SccMap();

	for(Constraint<V,Info> c : cs) {
	    for(V w : c.out()) {
		SCComponent<V> scc = v2scc.get(uf.find(w));
		assert scc != null;
		// mark that constraint c updates at least one
		// variable from scc
		scc2rules.add(scc, c);
	    }

	    for(V v : c.in()) {
		V v2 = uf.find(v);
		SCComponent<V> scc_v = v2scc.get(v2);
		assert scc_v != null;
		iterate_outs: for(V w : c.out()) {
		    SCComponent<V> scc_w = v2scc.get(uf.find(w));
		    assert scc_w != null;
		    if(scc_v.equals(scc_w)) {
			// mark that constraint c reads v and updates
			// at least one variable from the same scc as v
			v2rules.add(v2, c);
			break iterate_outs;
		    }
		}
	    }
	}
    }

    ///////////////////////////////////////////////////
    // KEY DATA-STRUCTURES USED BY THE SOLVER
    // top sorted component graph of the dependencies between constraint variables
    private TopSortedCompDiGraph<V> ts_deps;

    // map scc -> constraints that write at least one var from scc
    private Relation<SCComponent<V>,Constraint<V,Info>> scc2rules =
	new MapSetRelation<SCComponent<V>,Constraint<V,Info>>();

    // map var -> constraints that read var and write at least one var from the same scc
    private Relation<V,Constraint<V,Info>> v2rules = 
	new MapSetRelation<V,Constraint<V,Info>>();
    ///////////////////////////////////////////////////

    private void debugPrintUnifiedVars(PrintStream ps) {
	Relation<V,V> maj2vars = new MapSetRelation<V,V>();
	for(V v : vars) {
	    maj2vars.add(uf.find(v), v);
	}
	ps.println("Unified vars: ");
	for(V major : maj2vars.keys()) {
	    ps.println("  " + major + " <- " + maj2vars.getValues(major));
	}
    }


    /* Computes the inter-variable dependencies.  Variable v
       influences w (i.e., w depends on v) if there exists at least
       one constraint c that reads v and updates w (i.e., w belongs to
       c.in() and v belongs to c.out()); in that case the pair <v,w>
       appears in the relation returned by this method.

       As a technicality, the dependencies relation also contains a
       self-dependency for each variable that is mentioned by at least
       one constraint.  This is just to make sure that each variable
       appears in the dependencies graph.

       NOTE: the last paragraph seems to describe bad engineering.
       Hoping to fix this some day and make the code clearer. */
    private Relation<V,V> dependencies(Collection<Constraint<V,Info>> cs) {
	Relation<V,V> succs = new MapSetRelation<V,V>();
	Set<V> vertices = new LinkedHashSet<V>();

	for(Constraint<V,Info> c : cs) {
	    for(V v : c.in()) {
		for(V w : c.out()) {
		    succs.add(uf.find(v), uf.find(w));
		}
	    }
	    for(V v : c.in()) {
		vertices.add(uf.find(v));
	    }
	    for(V w : c.out()) {
		vertices.add(uf.find(w));
	    }
	}

	// tricky: makes sure the digraph sees all relevant vars, even
	// those that are not involved in some dependency arc.
	for(V v : vertices) {
	    succs.add(v, v);
	}

	return succs;
    }


    /** Solves <code>this</code> system of constraints.  Returns the
        least solution of the constraints.  Solving the same system
        twice may return different results, if the constraints use
        some external values that change in between the two calls to
        <code>solve()</code>; still, it is very important that these
        external values (if any) do NOT change during an execution of
        <code>solve()</code>. 

	<p>Note: this method is synchronized such that no two threads
	can execute it simultaneously: there are a few data structures
	that we create once and reuse in every call; also, there seems
	to be no reason why two threads would need to solve the same
	system twice. */
    public synchronized SolReader<V,Info> solve() {
	sa = new MySolAccessor();
	workset.clear();
	initVersions();

	// Algorithm: explore the sets of mutually dependent variables
	// (the SCCs of the dependency relation) in reverse
	// topological order (i.e., starting with those variables that
	// do not depend on any variable outside their scc).
	for(SCComponent<V> scc : ts_deps.decrOrder()) {
	    // Please read the comments inside solveSCC below
	    solveSCC(scc);
	}
	SolReader<V,Info> solReader = sa;
	sa = null;
	return solReader;
    }


    ////// BEGIN - VERSIONING SUPPORT 

    // TODO: for speed, we can place the version id(s) in the variable
    // and the constraint objects themselves.  Restriction: no
    // variable / constraint appears in two systems simultaneously
    // solved by two threads - pretty reasonable.  Advantage: no need
    // for maps.

    // init the version of each variable to -1; init the version of
    // last-used-inputs to -2 for each constraint
    private void initVersions() {
	v2version.clear();
	c2versions.clear();
    }
    // gives the version of variable v
    private int getVersion(V v) {
	int[] i = v2version.get(v);
	if(i == null) return -1;
	return i[0];
    }
    // increments the version of variable v
    private void incrVersion(V v) {
	int[] i = v2version.get(v);
	if(i == null) {
	    i = new int[]{-1};
	    v2version.put(v, i);
	}
	i[0]++;
    }
    // Checks whether constraint c was already evaluated for the
    // current versions of its input variables.  Also stores the
    // current versions for c's inputs.
    private boolean sameInputs(Constraint<V,Info> c) {
	boolean same = true;

	Collection<V> ins = c.in();
	int[] inVers = c2versions.get(c);
	if(inVers == null) {
	    inVers = new int[ins.size()];
	    c2versions.put(c, inVers);
	    same = false;
	}
	assert inVers.length == ins.size();

	int k = 0;
	for(V v_in : ins) {
	    int currVer = getVersion(uf.find(v_in));
	    if(same) {
		if(inVers[k] != currVer) {
		    inVers[k] = currVer;
		    same = false;
		}
	    } else {
		inVers[k] = currVer;
	    }
	    k++;
	}

	if(DEBUG_VER) {
	    System.out.print("  inVers: ");
	    for(int i = 0; i < inVers.length; i++) {
		System.out.print(inVers[i] + " ");
	    }
	    System.out.println();
	}

	return same;
    }


    // Map variable -> version of its value (the version, an int, is
    // stored as the element index 0 of an array - it's faster than an
    // immutable Integer that will need to be recreated again and
    // again, each time we increment the version)
    private NonIterableMap<V,int[]> v2version;
    // Map constraint -> versions of the values of its input
    // variables, as recorded the last time the constraint was
    // executed.  We re-execute the constraint only if at least one of
    // the input variables have a bigger version.
    private NonIterableMap<Constraint<V,Info>,int[]> c2versions;

    ////// END - VERSIONING SUPPORT 


    private class MySolAccessor implements SolAccessor<V,Info> {
	private final NonIterableMap<V,Info> sol = new NonIterableMap<V,Info>(vertexHashCapacity);

	// If CHECK_IN_OUT is true, the sol. accessor needs to know
	// the currently executed constraint, such that it can check
	// that c.in() contains each read variable appears, and
	// c.out() contains each variable c joins to.
	Constraint<V,Info> c;

	// join will add here the variables whose value has been
	// changed by the currently executed constraint.
	final Set<V> changedVars = new LinkedHashSet<V>(vertexHashCapacity);

	public Info get(V v) {
	    if(CHECK_IN_OUT && (c != null) && !c.in().contains(v)) {
		throw new Error(" Constraint " + c + " reads illegal data: " + v + " not in " + c.in());
	    }

	    v = uf.find(v);
	    return sol.get(v);
	}
	public void join(V v, Info delta) {
	    if(CHECK_IN_OUT && (c != null) && !c.out().contains(v)) {
		throw new Error(" Constraint " + c + " writes illegal data " + v + " not in " + c.out());
	    }
	    
	    // null represents bottom, so we can already return :)
	    if(delta == null) return;

	    v = uf.find(v);
	    Info old = sol.get(v);
	    // special case: no previous value for v
	    if(old == null) {
		// for mutation style
		sol.put(v, v.copy(delta));
		changedVars.add(v);
	    }
	    else {
		// normal case: join to a non-null variable
		if(v.join(old, delta)) {
		    changedVars.add(v);
		}
	    }
	}

	public String toString() {
	    StringBuffer buff = new StringBuffer();
	    for(V v : vars) {
		buff.append("  " + v + "(" + uf.find(v) + ") -> ");
		buff.append(get(v));
		buff.append("\n");
	    }
	    return buff.toString();
	}
    }
    

    private MySolAccessor sa;


    private void solveSCC(SCComponent<V> scc) {
	// 1. Execute once all constraints that write at least one var
	// from scc.  After this step, the influences from the outer
	// sccs will be propagated to this scc (although the
	// fixed-point from 2 is required in order to complete this
	// propagation inside scc).
	//
	// NOTE: initially, I thought about executing only those
	// constraints that also read one variable from outside scc
	// (or do not read any arg. at all).  Still, we cannot be
	// sure that a constraint that reads only vars from inside scc
	// does not use some constant to produce new values, so this
	// tentative "optimization" would be incorrect.
	if(DEBUG) System.out.println("scc-init " + scc.vertices());
	for(Constraint<V, Info> c : scc2rules.getValues(scc)) {
	    execute(c);
	}

	// 2. Iterate over constraints that may read (at least) one
	// var from scc and may write one or more variable(s) from scc
	if(DEBUG) System.out.println("scc-fixed-point");
	while(!workset.isEmpty()) {
	    Constraint<V,Info> c = workset.extract();
	    execute(c);
	}
    }
    // workset of the constraints that still need to be evaluated
    private final WorkSet<Constraint<V,Info>> workset = 
	new WorkPriorityQueue<Constraint<V,Info>>
	(new Constraint.CostComparator<V,Info>());


    // Executes one constraint; possibly add into the workset a few
    // more constraints that need to be re-evaluated.
    private final void execute(Constraint<V,Info> c) {
	// If we have already executed the constraint for the current
	// versions of the input vars, then no need to execute it
	// again -> return
	if(sameInputs(c)) {
	    if(DEBUG_VER) System.out.println("Constraint " + c + " has already been seen.");
	    return;
	}

	if(DEBUG) System.out.println("  Execute " + c);

	// initially, no changed variables
	sa.changedVars.clear();

	// This store is done such that sa can check that all
	// variables that are read / updated by c have been declared
	// in c.in() / c.out() (in case CHECK_IN_OUT is true).
	sa.c = c;
	c.action(sa);
	sa.c = null; // enable some gc

	if(DEBUG && !sa.changedVars.isEmpty()) {
	    System.out.println("    changed vars: " + sa.changedVars);
	}
	// For each changed variable, put into the workset all
	// constraints that read the variable and update at least one
	// variable from the same scc.  These constraints will need to
	// be (re)evaluated, as they may produce new values.
	for(V v : sa.changedVars) {
	    incrVersion(v);
	    if(DEBUG_VER) System.out.println("    Version(" + v + ") = " + getVersion(v));
	    workset.addAll(v2rules.getValues(v));
	}
    }
 

    ///////////////////////////////////////////////////////////////

    /** Pretty-printer of solver internals. */
    public void debugPrintSolverStructs(PrintStream ps) {
	ps.println("--- CONSTRAINT SOLVER STRUCTS --");

	// 1. Print groups of unified vars
	debugPrintUnifiedVars(ps);

	// 2. Print constraints
	// give a unique id to each constraint
	Map<Constraint<V,Info>,Integer> rule2id = new LinkedHashMap<Constraint<V,Info>,Integer>();
	int counter = 0;
	for(SCComponent<V> scc : scc2rules.keys()) {
	    for(Constraint<V,Info> rule : scc2rules.getValues(scc)) {
		if(rule2id.get(rule) == null) {
		    rule2id.put(rule, new Integer(counter++));
		}
	    }
	}
	ps.println("Simplified constraints:");
	for(Constraint<V,Info> c : rule2id.keySet()) {
	    ps.println("  C" + rule2id.get(c) + ":\t" + c);
	}

	// 3. Print dependency SCCs
	ps.println("Dependency SCC(s):");
	int sccCounter = 0;
	for(SCComponent<V> scc : ts_deps.decrOrder()) {
	    ps.println("SCC" + (sccCounter++) + " " + scc.vertices());
	    Collection<Constraint<V,Info>> inRules = scc2rules.getValues(scc);
	    if(inRules.size() != 0) {
		ps.print("  scc-in rules: [");
		for(Constraint<V,Info> c : inRules) 
		    ps.print(" C" + rule2id.get(c));
		ps.println(" ]");
	    }
	    for(V v : scc.vertices()) {
		Collection<Constraint<V,Info>> vRules = v2rules.getValues(v);
		if(vRules.size() != 0) {
		    ps.print("  Rules for " + v + ": [");
		    for(Constraint<V,Info> c : vRules) 
			ps.print(" C" + rule2id.get(c));
		    ps.println(" ]");
		}
	    }
	}

	ps.println("--------------------------------");
    }

    /** Pretty-printer of solver internals using the standard output
        (<code>System.out</code>). */
    public void debugPrintSolverStructs() {
	debugPrintSolverStructs(System.out);
    }


    /** Returns a map from each original variable to the
        representative of its corresponding variable equivalence
        class. */
    public Map<V,V> debugGetVarUnification() {
	Map<V,V> map = new LinkedHashMap<V,V>();
	for(Set<V> equivClass : uf.allNonTrivialEquivalenceClasses()) {
	    V repr = uf.find(DSUtil.getFirst(equivClass));
	    for(V var : equivClass) {
		map.put(var, repr);
	    }
	}
	return map;
    }

    /** Returns the yet-ununified variables.  These are the
        representatives of the equivalence classes of variables. */
    public Collection<V> debugUniqueVars() {
	Collection<V> res = new LinkedHashSet<V>();
	for(V v : vars) {
	    res.add(uf.find(v));
	}
	return res;
    }

}
