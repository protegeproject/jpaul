// TestBasicBlockDiGraph.java, created Wed Feb 23 15:55:51 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

/**
 * <code>TestBasicBlockDiGraph</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TestBasicBlockDiGraph.java,v 1.3 2006/01/29 16:05:30 adam_kiezun Exp $
 */
abstract class TestBasicBlockDiGraph {
    
    static <Vertex> void test(DiGraph<Vertex> origDig) {
	long start = System.currentTimeMillis();
	System.out.print("  BasicBlockDiGraph construction time ... ");
	System.out.flush();

	BasicBlockDiGraph<Vertex> bbDig = new BasicBlockDiGraph<Vertex>(origDig);

	System.out.println((System.currentTimeMillis() - start) + " ms; " +
			   bbDig.vertices().size() + " bb(s)");
	

	System.out.print("  BasicBlockDiGraph testing time ... ");
	System.out.flush();
	start = System.currentTimeMillis();

	checkCorrectness(origDig, bbDig);

	System.out.println((System.currentTimeMillis() - start) + " ms");
    }


    static <Vertex> void checkCorrectness(DiGraph<Vertex> origDig,
					  BasicBlockDiGraph<Vertex> bbDig) {

	checkSameVertices(origDig, bbDig);
	checkSameArcs(origDig, bbDig);
	for(BasicBlock<Vertex> bb : bbDig.vertices()) {
	    checkBasicBlock(bb, origDig.getBiDiNavigator());
	}
    }

    // check same vertices
    static <Vertex> void checkSameVertices(DiGraph<Vertex> origDig,
					   BasicBlockDiGraph<Vertex> bbDig) {
	Set<Vertex> verts = new HashSet<Vertex>();
	for(BasicBlock<Vertex> bb : bbDig.vertices()) {
	    for(Vertex v : bb.elems()) {
		boolean isNew = verts.add(v);
		Assert.assertTrue("Vertex " + v + " appears in two basic blocks",
				  isNew);
	    }
	}
	Assert.assertTrue("different set of vertices:\n\t orig=" + origDig.vertices() +
			  "\n\t   bb=" + verts,
			  verts.equals(origDig.vertices()));
    }

    // check same vertices
    static <Vertex> void checkSameArcs(DiGraph<Vertex> origDig,
				       BasicBlockDiGraph<Vertex> bbDig) {
	// collect the arcs of the original digraph
	Set<Pair<Vertex,Vertex>> origArcs = new HashSet<Pair<Vertex,Vertex>>();
	for(Vertex v : origDig.vertices()) {
	    for(Vertex v2 : origDig.getForwardNavigator().next(v)) {
		origArcs.add(new Pair<Vertex,Vertex>(v,v2));
	    }
	}

	// collect the original arcs as shown by the basic bloc digraph
	Set<Pair<Vertex,Vertex>> bbArcs = new HashSet<Pair<Vertex,Vertex>>();
	for(BasicBlock<Vertex> bb : bbDig.vertices()) {
	    Vertex prev = null;
	    for(Vertex v : bb.elems()) {
		if(prev != null) {
		    bbArcs.add(new Pair<Vertex,Vertex>(prev,v));
		}
		prev = v;
	    }
	    for(BasicBlock<Vertex> bbNext : bb.next()) {
		bbArcs.add(new Pair<Vertex,Vertex>
			   (bb.elems().getLast(), bbNext.elems().getFirst()));
	    }
	}

	Assert.assertTrue("the basic block models different arcs",
			  origArcs.equals(bbArcs));
    }
    

    static <Vertex> void checkBasicBlock(BasicBlock<Vertex> bb,
					 BiDiNavigator<Vertex> nav) {

	// each basic block contains "straight-line" sequences
	{
	    Vertex prev = null;
	    for(Vertex v : bb.elems()) {
		if(prev != null) {
		    Assert.assertTrue("split",
				      nav.next(prev).size() < 2);
		    Assert.assertTrue("wrong link / disconnected",
				      nav.next(prev).contains(v));
		    Assert.assertTrue("join",
				      nav.prev(v).size() < 2);
		    Assert.assertTrue("wrong link / disconnected",
				      nav.prev(v).contains(prev));
		}
		prev = v;
	    }
	}

	// the following two tests check that each basic block is
	// maximal, i.e., cannot be extended up or down.
	{
	    Vertex first = bb.elems().getFirst();
	    Vertex last  = bb.elems().getLast();

	    // Check the basic block cannot be extended upward:
	    // - first node is a join (in-degree >= 2), OR
	    // - first node has no predecessor (in-degree == 0), OR
	    // - the only pred. of first node is a split, OR
	    // - the only pred. is the last node from this bb 
	    // (pathological case: the entire original graph is a cycle)
	    List<Vertex> prevs = nav.prev(first);
	    if(prevs.size() == 1) {
		Vertex prev = DSUtil.getFirst(prevs);
		Assert.assertTrue("1st vertex from bb is suspicious " + first,
				  prev.equals(last) ||
				  (nav.next(prev).size() > 1));
	    }
	    // Check the basic block cannot be extended downward:
	    // - last node is a split (out-degree >= 2), OR
	    // - last node has no successor (out-degree == 0), OR
	    // - the only succ. of last node is a join, OR
	    // - the only succ. is the first node from this bb
	    // (pathological case: the entire original graph is a cycle)
	    List<Vertex> nexts = nav.next(last);
	    if(nexts.size() == 1) {
		Vertex next = nexts.iterator().next();
		Assert.assertTrue("last vertex from bb is suspicious " + last,
				  next.equals(first) ||
				  (nav.prev(next).size() > 1));
	    }
	}
    }
}
