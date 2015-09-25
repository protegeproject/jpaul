// TopSortedCompDiGraph.java, created Thu Mar  4 08:10:43 2004 by salcianu
// Copyright (C) 2003 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

import java.util.Map;
import java.util.LinkedHashMap;

import jpaul.DataStructs.ReverseListView;
import jpaul.Misc.Action;

/**
 * <code>TopSortedCompDiGraph</code> is a topologically-sorted
 * component digraph.  Given a digraph, its component digraph is the
 * (acyclic!) digraph having as vertices the strongly-connected
 * components of the original digraph (its arcs are trivially induced
 * by the original arcs).  Being acyclic, the component digraph admits
 * a topological ordering.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TopSortedCompDiGraph.java,v 1.8 2006/03/14 02:29:31 salcianu Exp $
 */
public class TopSortedCompDiGraph<Vertex> extends DiGraph<SCComponent<Vertex>> {

    /** Constructs the topologically sorted component digraph of
        <code>digraph</code>. */
    public TopSortedCompDiGraph(DiGraph<Vertex> graph) {
	sccRoots = SCComponent.buildScc(graph);

	// build list of topologically sorted SCCs
	sccSortedList = new LinkedList<SCComponent<Vertex>>();
	this.dfs
	    (null, // no action on node entry
	     // on dfs termination, add scc to front of sccSortedList
	     new Action<SCComponent<Vertex>>() {
		public void action(SCComponent<Vertex> scc) {
		    sccSortedList.addFirst(scc);
		}
	    });
    }

    // set of top-level SCCs (no incoming arcs + all SCCs are
    // reachable from here)
    private final Collection<SCComponent<Vertex>> sccRoots;
    // list of all SCCs, in decreasing topologic order
    private final LinkedList<SCComponent<Vertex>> sccSortedList;

    public Collection<SCComponent<Vertex>> getRoots() {
	return sccRoots;
    }

    /** @return Bi-directional navigator for the component graph. */
    public BiDiNavigator<SCComponent<Vertex>> getBiDiNavigator() {
	return SCComponent.<Vertex>getSccBiDiNavigator();
    }

    /** @return List of the strongly-connected components of the
        underlying digraph, in <b>decreasing</b> topologic order,
        i.e., starting with the SCCs with no incoming arcs. */
    public List<SCComponent<Vertex>> decrOrder() {
	return sccSortedList;
    }
    
    /** @return List of the strongly-connected components of the
        underlying digraph, in <b>increasing</b> topologic order,
        i.e., starting with the SCCs with no outgoing arcs. */
    public List<SCComponent<Vertex>> incrOrder() {
	return new ReverseListView<SCComponent<Vertex>>(sccSortedList);
    }

    /** 
	@return Strongly-connected component that <code>v</code>
	belongs to; returns <code>null</code> iff <code>v</code> does
	not appear in the original dag.

	<p> For space-savings purposes, we do NOT construct a map from
	vertices to SCCs; instead, each <code>getScc</code> query
	takes time linear in the number of SCCs.  If you do many calls
	to this method, you may want to use {@link
	#getVertex2SccMap()}. */
    public SCComponent<Vertex> getScc(Vertex v) {
	// We don't want to allocate a full LinkedHashMap for each
	// TopSortedCompDiGraph - too memory expensive.
	for(SCComponent<Vertex> scc : this.incrOrder()) {
	    if(scc.contains(v)) return scc;
	}
	return null;
    }

    /**
       @return Map from each vertex from the original dag to its
       corresponding strongly-connected component.  You should use
       this method in case you want to do many
       <code>getScc</code>-like queries (<code>getScc</code> is quite
       expensive).  The returned map has a predictve iteration order
       (it's a <code>LinkedHashMap</code>).

       @see #getScc */
    public Map<Vertex,SCComponent<Vertex>> getVertex2SccMap() {
	Map<Vertex,SCComponent<Vertex>> v2scc = new LinkedHashMap<Vertex,SCComponent<Vertex>>();
	for(SCComponent<Vertex> scc : this.incrOrder()) {
	    for(Vertex v : scc.vertices()) {
		v2scc.put(v, scc);
	    }
	}
	return v2scc;
    }
}
