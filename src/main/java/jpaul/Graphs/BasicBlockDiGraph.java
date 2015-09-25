// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;

import jpaul.DataStructs.DSUtil;

/**
 * A <code>BasicBlockDiGraph</code> is a basic block representation of
 * a directed graph.  The vertices of a <code>BasicBlockDiGraph</code>
 * are (maximal) {@link BasicBlock}s made up of vertices of the
 * original digraph.  The arcs are induced by the arcs of the original
 * digraph.
 *
 * @see BasicBlock
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BasicBlockDiGraph.java,v 1.10 2006/03/14 02:29:31 salcianu Exp $ */
public class BasicBlockDiGraph<Vertex> extends DiGraph<BasicBlock<Vertex>> {
    
    /** Creates a <code>BasicBlockDiGraph</code> representation for
	<code>diGraph</code>.

	Complexity: linear in the size (vertices + arcs) of <code>diGraph</code>*/
    public BasicBlockDiGraph(DiGraph<Vertex> diGraph) {
        (new BuildBBClosure()).doIt(diGraph);
    }

    /** @return BiDiNavigator for <code>this</code> <code>BasicBlockDiGraph</code>.

	Complexity: O(1). */
    public BiDiNavigator<BasicBlock<Vertex>> getBiDiNavigator() {
	return new BBBiDiNavigator<Vertex>();
    }

    private static class BBBiDiNavigator<Vertex> implements BiDiNavigator<BasicBlock<Vertex>> {
	    public List<BasicBlock<Vertex>> next(BasicBlock<Vertex> bb) {
		return bb.next();
	    }
	    public List<BasicBlock<Vertex>> prev(BasicBlock<Vertex> bb) {
		return bb.next();
	    }
    }
    
    public Collection<BasicBlock<Vertex>> getRoots() {
	return rootBBs;
    }


    /** @return The basic block that the vertex <code>v</code> belongs
        to, or <code>null</code> if no such basic block exists.  

	Complexity: O(1). */
    public BasicBlock<Vertex> getBB(Vertex v) {
	return v2bb.get(v);
    }
    
    // map vertex (orig. digraph) -> basic block
    private final Map<Vertex,BasicBlock<Vertex>> v2bb = 
	new LinkedHashMap<Vertex,BasicBlock<Vertex>>();
    // root basic blocks
    private final List<BasicBlock<Vertex>> rootBBs = 
	new LinkedList<BasicBlock<Vertex>>();

    private class BuildBBClosure {
	private BiDiNavigator<Vertex> nav;
	// list of all basic blocks from the BasicBlockDiGraph
	// invariant bbs.equals(v2dd.values());
	private List<BasicBlockImpl> bbs = 
	    new LinkedList<BasicBlockImpl>();
	
	public void doIt(DiGraph<Vertex> digraph) {
	    nav = digraph.getBiDiNavigator();

	    for(Vertex v : digraph.getRoots()) {
		Vertex v2 = walkUp(v);
		BasicBlock<Vertex> bb = special_dfs(v2);
		if(bb != null) {
		    rootBBs.add(bb);
		}
	    }

	    for(BasicBlockImpl bb : bbs) {
		for(Vertex v : nav.prev(bb.elems().getFirst())) {
		    bb.prev.addLast(v2bb.get(v));
		}
		for(Vertex v : nav.next(bb.elems().getLast())) {
		    bb.next.addLast(v2bb.get(v));
		}
	    }
	}

	// "walk up" into the basic block that contains v
	// return the 1st vertex from this basic block
	// (well, in the case of a cycle, 1st is not uniquely defined ...)
	private Vertex walkUp(Vertex v) {
	    Vertex vcurr = v;
	    while(true) {
		List<Vertex> prevs = nav.prev(vcurr);

		// vcurr is a join point, or an initial point (no incoming arcs);
		if(prevs.size() != 1) return vcurr;
		Vertex v2 = DSUtil.getFirst(prevs);

		// v2 is a split point
		if(nav.next(v2).size() != 1)
		    return vcurr;

		// avoid cycles
		if(v2.equals(v)) return vcurr;

		// iterate!
		vcurr = v2;
	    }
	}

	private BasicBlock<Vertex> special_dfs(Vertex v) {
	    // do not re-examine vertices that have already been assigned to a BB
	    if(v2bb.containsKey(v)) return null;

	    BasicBlockImpl cbb = new BasicBlockImpl();
	    bbs.add(cbb);

	    expandCBB(v, cbb);
	    return cbb;
	}

	private void expandCBB(Vertex v, BasicBlockImpl cbb) {
	    cbb.elems.addLast(v);
	    v2bb.put(v, cbb);

	    List<Vertex> succs = nav.next(v);
	    if(succs.size() == 0) return;
	    if(succs.size() > 1) {
		// v has more than one succs -> split point
		terminateCBB(v);
		return;
	    }
	    // succ is the unique successor of v
	    Vertex succ = DSUtil.getFirst(succs);
	    if((nav.prev(succ).size() == 1) &&
	       !v2bb.containsKey(succ)) {
		// v is the unique predecessor of succ, and 
		// has not been distributed into a BB yet
		expandCBB(succ, cbb);
	    }
	    else {
		terminateCBB(v);
	    }
	}

	private void terminateCBB(Vertex v) {
	    for(Vertex succ: nav.next(v)) {
		special_dfs(succ);
	    }
	}
    }

    // simple implementation of the BasicBlock interface
    private class BasicBlockImpl implements BasicBlock<Vertex> {
	protected BasicBlockImpl() { this.id = nbBBs++; }

	private int id;
	LinkedList<BasicBlock<Vertex>> prev = new LinkedList<BasicBlock<Vertex>>();
	LinkedList<BasicBlock<Vertex>> next = new LinkedList<BasicBlock<Vertex>>();
	LinkedList<Vertex> elems = new LinkedList<Vertex>();

	public List<BasicBlock<Vertex>> prev() { return this.prev; }
	public List<BasicBlock<Vertex>> next() { return this.next; }
	public LinkedList<Vertex> elems() { return this.elems; }
	public int getId() { return this.id; }

	public DiGraph<BasicBlock<Vertex>> enclosingBBDiGraph() {
	    // ridiculous: BasicBlockDiGraph<Vertex>.this does not work ...
	    // I have to do this ugly work-around ...
	    return getBBDGThis();
	}
    }
    private int nbBBs = 0;
    // see "ridiculous" note above
    private BasicBlockDiGraph<Vertex> getBBDGThis() { return this; }

}
