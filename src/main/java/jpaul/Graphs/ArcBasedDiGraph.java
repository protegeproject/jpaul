// ArcBasedDiGraph.java, created Tue May  6 10:53:15 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;
import jpaul.DataStructs.Pair;

/**
 * Digraph based on a list of arcs.
 * 
 * Created: Sun Feb 7 20:00:00 2005
 *
 * @author Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: ArcBasedDiGraph.java,v 1.11 2006/03/14 02:29:31 salcianu Exp $ */
public class ArcBasedDiGraph<Vertex> extends DiGraph<Vertex> {

    /** Constructs a digraph based on a collection of arcs between vertices.
        Each arc is given as a pair &lt;source,target&gt;. */
    public ArcBasedDiGraph(Collection<Pair<Vertex,Vertex>> arcs) {
	this(ArcBasedDiGraph.<Vertex>edgeColl2Relation(arcs));
    }

    /** Constructs a digraph based on a set of arcs given as a successor relation.
	
	@param succs Relation between arc sources and arc targets. */
    public ArcBasedDiGraph(Relation<Vertex,Vertex> succs) {
	// agree on some order between the succs
	for(Vertex v : succs.keys()) {
	    v2succs.put(v, new ArrayList<Vertex>(succs.getValues(v)));
	}	
    }

    private static <Vertex> Relation<Vertex,Vertex> edgeColl2Relation(Collection<Pair<Vertex,Vertex>> arcs) {
	// adjacency relation: vertex -> successors
	Relation<Vertex,Vertex> succs = new MapSetRelation<Vertex,Vertex>();
	for(Pair<Vertex,Vertex> arc : arcs) {
	    succs.add(arc.left, arc.right);
	}
	return succs;
    }

    private final Map<Vertex,List<Vertex>> v2succs = new LinkedHashMap<Vertex,List<Vertex>>();

    public Set<Vertex> getRoots() { return v2succs.keySet(); }

    public ForwardNavigator<Vertex> getForwardNavigator() {
	return new ForwardNavigator<Vertex>() {
	    public List<Vertex> next(Vertex v) {
		List<Vertex> next = v2succs.get(v);
		if(next == null) return Collections.<Vertex>emptyList();
		return next;
	    }
	};
    }
}
