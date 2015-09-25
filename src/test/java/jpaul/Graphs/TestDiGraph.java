// TestDiGraph.java, created Mon Feb 14 20:17:03 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import java.util.Random;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.InterruptTraversalException;

import jpaul.Misc.Action;
import jpaul.Misc.ActionPredicate;
import jpaul.Misc.IntMCell;
import jpaul.Misc.BoolMCell;

/**
 * <code>TestDiGraph</code> tests the digraph reachability, SCC
 * construction, topological sorting, and basic block construction.
 * Testing is done on several small digraphs and a much bigger set of
 * large (pseudo-)randomly-generated directed graphs.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TestDiGraph.java,v 1.10 2006/03/14 02:54:51 salcianu Exp $ */
public class TestDiGraph extends TestCase {
    
    public static Test suite() {
	return new TestSuite(TestDiGraph.class);
    }

    
    private int[][][] tests = new int[][][] {
	{
	    {1, 2}, {2, 3}, {3, 4}, {3, 5}
	},
	{
	    {1, 2}, {2, 1}, {3, 1}
	},
	{
	    {1, 2}, {2, 3}, {3, 1}
	},
	{
	    {1, 2}, {2, 3},
	    {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 5}, {7, 8},
	    {3, 9}, {9, 8}, {8, 10}, {10, 3}
	}
    };

    
    public void testDFSWithInterrupt() {
	System.out.println("Testing DiGraph.dfs/dfs2 with total/partial abort features.");

	DiGraph<Integer> diGraph = getDiGraphFromArray
	    (new int[][]{
		{1, -2},
		{1, 3},
		{-2, 4},
		{-2, 5},
		{5, 1}
	    });
	// make sure our digraph has only 1 as root
	// (the dfs2 traversal will start from 1, and only from 1)
	diGraph = DiGraph.diGraph(Collections.singleton(new Integer(1)),
				  diGraph.getBiDiNavigator());
	final IntMCell accum = new IntMCell(0);
	Set<Integer> visited = 
	    diGraph.dfs2
	    (new ActionPredicate<Integer>() {
		public boolean actionPredicate(Integer v) {
		    int i = v.intValue();
		    accum.value += i;
		    // do not recurse the traversal from negative vertices
		    return i >= 0;
		}
	    }, null);
	Assert.assertTrue("why was 4 visited?", !visited.contains(new Integer(4)));
	Assert.assertTrue("accum.value = " + accum.value, accum.value == 2);
	
	final BoolMCell foundNeg = new BoolMCell(false);
	Action<Integer> searchNeg = new Action<Integer>() {
	    public void action(Integer v) {
		if(v.intValue() < 0) {
		    foundNeg.value = true;
		    throw new InterruptTraversalException();
		}
	    }
	};
	diGraph.dfs(searchNeg, null);
	Assert.assertTrue("-2 was not found by DiGraph.dfs with total abort !", foundNeg.value);

	foundNeg.value = false;
	diGraph.dfs2(ActionPredicate.fromAction(searchNeg, true), null);
	Assert.assertTrue("-2 was not found by DiGraph.dfs2 with total abort !", foundNeg.value);
    }
	

    public void test() {
	System.out.println("Regression tests for DiGraph");

	System.out.println("SMALL TESTS\n");
	for(int i = 0; i < tests.length; i++) {
	    System.out.println("Small Test " + (i+1));
	    DiGraph<Integer> diGraph = getDiGraphFromArray_time(tests[i]);
	    testDiGraph(diGraph);
	    System.out.println();
	}

	System.out.println("LARGE TESTS\n");
	for(int step = 0; step < 2; step++) {
	    if(step == 0) { 
		System.out.println("REPRODUCIBLE TESTS\n");
		random = new Random(0);
	    }
	    else {
		System.out.println("TRULY RANDOM TESTS\n");
		random = new Random(System.currentTimeMillis());
	    }
	    
	    for(int i = 0; i < NB_TESTS; i++) {
		int nbVertices = 10*(i+20);
		// The "3" factor was chosen such that the resulting
		// graphs are reasonably sparse, and have an
		// interesting number of SCCs.
		int nbArcs = nbVertices * 3;
		System.out.println("Test " + (i+1) + " - random diGraph");
		DiGraph<Integer> diGraph = getRandomDiGraph_time(nbVertices, nbArcs);
		testDiGraph(diGraph);
		System.out.println();
	    }
	}
    }
    private static int NB_TESTS = 20;
    // we do not run all tests for graphs with more than SMALL_DIGRAPH_SIZE vertices
    private static long SMALL_DIGRAPH_SIZE = 250;


    private void testDiGraph(DiGraph<Integer> diGraph) {
	System.out.println("  Graph size: " + 
			   diGraph.numVertices() + " vertices; " + 
			   diGraph.numArcs() + " arcs");

	// test basic block construction
	TestBasicBlockDiGraph.test(diGraph);
	
	// test the SCC construction
	testSCC(diGraph);
    }

    private <Vertex> void testSCC(DiGraph<Vertex> diGraph) {	
	int nbPaths = 0;
	long pathLength = 0;

	long start = System.currentTimeMillis();
	TopSortedCompDiGraph<Vertex> cDiGraph = new TopSortedCompDiGraph<Vertex>(diGraph);
	System.out.println("  TopSortedCompDiGraph constr " + 
			   (System.currentTimeMillis() - start) + " ms; " + 
			   cDiGraph.incrOrder().size() + " scc(s)");

	if(diGraph.numVertices() > SMALL_DIGRAPH_SIZE) {
	    System.out.println("  DiGraph too big; skip the rest of the tests ;(");
	    return;
	}

	System.out.print("  Check intra-SCC strong connectivity ... ");
	for(SCComponent<Vertex> scc : cDiGraph.incrOrder()) {
	    for(Vertex v1 : scc.vertices()) {
		for(Vertex v2 : scc.vertices()) {
		    if(v1.equals(v2)) continue;
		    // check that there is a path from v1 to v2
		    pathLength += checkPath(diGraph, v1, v2);
		    pathLength += checkPath(diGraph, v2, v1);
		    nbPaths += 2;
		}
	    }
	}
	System.out.println("ok");
	System.out.println("    " + nbPaths + " paths checked; " + 
			   "total length: " + pathLength + " arcs");

	System.out.print("  Check SCC maximality ... ");
	Collection<Vertex> verts = diGraph.vertices();
	for(Vertex v1 : verts) {
	    for(Vertex v2 : verts) {
		SCComponent<Vertex> scc1 = cDiGraph.getScc(v1);
		Assert.assertTrue("Inconsistent getSCC", scc1.contains(v1));
		SCComponent<Vertex> scc2 = cDiGraph.getScc(v2);
		Assert.assertTrue("Inconsistent getSCC", scc2.contains(v2));
		
		if(scc1 == scc2) continue;
		Assert.assertTrue
		    ("SCCs are not maximal",
		     (diGraph.findPath(v1, v2) == null) || (diGraph.findPath(v2, v1) == null));
	    }
	}
	System.out.println("ok");

	System.out.print("  Check topological ordering ... ");
	SCComponent<Vertex> prev = null;
	for(SCComponent<Vertex> curr : cDiGraph.incrOrder()) {
	    if(prev != null) {
		Assert.assertTrue
		    ("Incorrect topological ordering",
		     cDiGraph.findPath(prev, curr) == null);
	    }
	    prev = curr;
	}
	System.out.println("ok");
    }

    // Check that there is a valid path between two vertices from the same SCC.
    // Returns the size of the path (if valid)
    private <Vertex> int checkPath(DiGraph<Vertex> diGraph, Vertex v1, Vertex v2) {
	List<Vertex> path = diGraph.findPath(v1, v2);
	Assert.assertTrue
	    ("Disconnected vertices in the same SCC",
	     path != null);

	Vertex prev = null;
	for(Vertex curr : path) {
	    if(prev == null) {
		prev = curr;
		continue;
	    }
	    Assert.assertTrue
		("Path with inexistent arc",
		 DSUtil.iterableContains(diGraph.getForwardNavigator().next(prev),
					 curr));
	    prev = curr;
	}

	return path.size();
    }    


    // trying to have a "real" pseudo-random generator
    private Random random = new Random(System.currentTimeMillis());

    /** Generates a random <code>DiGraph</code> with at most
     * <code>maxNumVerts</code> vertices. */
    private DiGraph<Integer> getRandomDiGraph(int maxNumVerts, int numArcs) {
	int iarcs[][] = new int[numArcs][2];
	for(int i = 0; i < numArcs; i++) {
	    iarcs[i][0] = random.nextInt(maxNumVerts);
	    iarcs[i][1] = random.nextInt(maxNumVerts);
	}
	return getDiGraphFromArray(iarcs);
    }

    private DiGraph<Integer> getRandomDiGraph_time(int maxNumVerts, int numArcs) {
	long start = System.currentTimeMillis();
	DiGraph<Integer> diGraph = getRandomDiGraph(maxNumVerts, numArcs);
	System.out.println("  DiGraph construction time " + 
			   (System.currentTimeMillis() - start) + " ms");
	return diGraph;
    }

    private DiGraph<Integer> getDiGraphFromArray(int iarcs[][]) {
	Set<Integer> vertices = new HashSet<Integer>();
	Collection<Pair<Integer,Integer>> arcs = new LinkedHashSet<Pair<Integer,Integer>>();
	for(int i = 0; i < iarcs.length; i++) {
	    Integer vStart = new Integer(iarcs[i][0]);
	    Integer vEnd   = new Integer(iarcs[i][1]);
	    arcs.add(new Pair<Integer,Integer>(vStart, vEnd));
	    vertices.add(vStart);
	    vertices.add(vEnd);
	}
	DiGraph<Integer> dg = new ArcBasedDiGraph<Integer>(arcs);
	
	System.out.print("Testing DiGraph.vertices()/numVertices()/numArcs() ... ");
	Set<Integer> verts = dg.vertices();
	// to test the determinism of teh set returned by
	// DiGraph.vertices(), we build two lists based on vertices()
	// and check their equality.  We also perform some operations
	// (some other tests) between the creation of the two lists,
	// to bring in more non-determinism :)
	List<Integer> l1 = new LinkedList<Integer>(verts);
	// we need to do the following three tests here, because they
	// require independent knowledge of the sets of vertices/arcs
	Assert.assertTrue("wrong number of vertices", vertices.size() == dg.numVertices());
	Assert.assertTrue("wrong set of vertices", vertices.equals(verts));
	Assert.assertTrue("wrong number of arcs", arcs.size() == dg.numArcs());
	List<Integer> l2 = new LinkedList<Integer>(verts);
	Assert.assertTrue("inconsistent iteration order for DiGraph.vertices()", l1.equals(l2));
	System.out.println("ok");
	return dg;
    }

    private DiGraph<Integer> getDiGraphFromArray_time(int[][] iarcs) {
	long start = System.currentTimeMillis();
	DiGraph<Integer> diGraph = getDiGraphFromArray(iarcs);
	System.out.println("  DiGraph construction time " + 
			   (System.currentTimeMillis() - start) + " ms");
	return diGraph;
    }
}
