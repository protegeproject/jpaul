// LabeledDiGraph.java, created Fri Aug 19 13:28:40 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;

import jpaul.Misc.Function;
import jpaul.Misc.Action;

/**
 * <code>LabeledDiGraph</code> models a labeled directed graph.  This is a
 * <code>DiGraph</code> where each arc has a label.  Similar to a
 * {@link DiGraph}, a <code>LabeledDiGraph</code> is defined by giving a set
 * of roots and a navigator to iterate over the (labeled) arcs.  The
 * vertices from the <code>LabeledDiGraph</code> are those vertices that are
 * reachable from the roots by following the forward arcs given by the
 * navigator.
 *
 * <p>An <code>LabeledDiGraph</code> is trivially a <code>DiGraph</code>: it
 * is enough to strip the labels off the arcs.  All the algorithms for
 * a <code>DiGraph</code> can be used for a <code>LabeledDiGraph</code>.
 * 
 * @see DiGraph
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: LabeledDiGraph.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class LabeledDiGraph<Vertex,Label> extends DiGraph<Vertex> {

    /** Forward iterator into a labeled graph.  Each labeled navigator
        is also a normal navigator: we just strip the labels off the
        arcs. */
    public abstract static class LabeledForwardNavigator<Vertex,Label> implements ForwardNavigator<Vertex> {
	/** Returns the list of the arcs that exit vertex
            <code>v</code>.  Each arc is modeled as a pair of the
            destination vertex and the arc label.  */
	public abstract List<Pair<Vertex,Label>> lnext(Vertex v);
	public List<Vertex> next(Vertex v) { return LabeledDiGraph.<Vertex,Label>convert(lnext(v)); }
    }


    /** Bidirectional iterator into a labeled graph.  Each labeled
        navigator is also a normal navigator: we just strip the labels
        off the arcs.  */
    public abstract static class LabeledBiDiNavigator<Vertex,Label> 
	  extends LabeledForwardNavigator<Vertex,Label> implements BiDiNavigator<Vertex> {
	/** Returns the list of the arcs that enter into the vertex
            <code>v</code>.  Each arc is modeled as a pair of the
            source vertex and the arc label.  */
	public abstract List<Pair<Vertex,Label>> lprev(Vertex v);
	public List<Vertex> prev(Vertex v) { return LabeledDiGraph.<Vertex,Label>convert(lprev(v)); }
    }

    /** Creates a <code>LabeledDiGraph</code>. */
    public LabeledDiGraph() {
	this(false);
    }

    /** Creates a <code>LabeledDiGraph</code>. */
    public LabeledDiGraph(boolean CACHING) {
	super(CACHING);
    }

    
    public abstract Collection<Vertex> getRoots();


    /** Returns a bi-directional labeled navigator through
        <code>LabeledDiGraph</code>.  The default implementation uses the
        forward navigator returned by <code>LabeledForwardNavigator</code>
        to traverse the entire graph and construct the list of
        backward arcs.

	<b>Note:</b> You MUST override at least one of
	<code>getLabeledBiDiNavigator</code> and
	<code>getLabeledForwardNavigator</code>.  */
    public LabeledBiDiNavigator<Vertex,Label> getLabeledBiDiNavigator() {
	if(CACHING && lnav == null) {
	    return lnav;
	}
	LabeledBiDiNavigator<Vertex,Label> lnav = _getLabeledBiDiNavigator();
	if(CACHING) {
	    this.lnav = lnav;
	}
	return lnav;
    }
    private LabeledBiDiNavigator<Vertex,Label> lnav = null;

    private LabeledBiDiNavigator<Vertex,Label> _getLabeledBiDiNavigator() {
	Relation<Vertex,Pair<Vertex,Label>> rel =
	    new MapSetRelation<Vertex,Pair<Vertex,Label>>();
	for(Vertex src : vertices()) {
	    for(Pair<Vertex,Label> p : getLabeledForwardNavigator().lnext(src)) {
		Vertex dst = p.left;
		Label  lab = p.right;
		rel.add(dst, new Pair<Vertex,Label>(src,lab));
	    }
	}
	final Map<Vertex,List<Pair<Vertex,Label>>> dst2src = 
	    new LinkedHashMap<Vertex,List<Pair<Vertex,Label>>>();
	for(Vertex dst : rel.keys()) {
	    dst2src.put(dst, new LinkedList<Pair<Vertex,Label>>());
	}
	return new LabeledBiDiNavigator<Vertex,Label>() {
	    public List<Pair<Vertex,Label>> lnext(Vertex v) {
		return getLabeledForwardNavigator().lnext(v);
	    }
	    public List<Pair<Vertex,Label>> lprev(Vertex v) {
		return dst2src.get(v);
	    }
	};
    }


    /** Returns a forward labeled navigator through this
	<code>LabeledDiGraph</code>.  The default implementation returns the
	full navigator produced by {@link #getLabeledBiDiNavigator()}.

	<b>Note:</b> You MUST override at least one of
	<code>getLabeledBiDiNavigator</code> and
	<code>getLabeledForwardNavigator</code>.  */
    public LabeledForwardNavigator<Vertex,Label> getLabeledForwardNavigator() {
	return getLabeledBiDiNavigator();
    }


    /** Returns the (bi-directional) navigator for this digraph.

	Default implementation for <code>LabeledDiGraph</code>s: returns the
        same object as {@link #getLabeledBiDiNavigator()}. */
    public BiDiNavigator<Vertex> getNavigator() {
	return getLabeledBiDiNavigator();
    }


    /** Returns the forward navigator for this digraph.

	Default implementation for <code>LabeledDiGraph</code>s: returns the
        same object as {@link #getLabeledForwardNavigator()}. */
    public ForwardNavigator<Vertex> getForwardNavigator() {
	return getLabeledForwardNavigator();
    }
    

    public String toString() {
	final StringBuffer buff = new StringBuffer("{");
	this.dfs
	    (new Action<Vertex>() {
		public void action(Vertex v) {
		    List<Pair<Vertex,Label>> edges = getLabeledForwardNavigator().lnext(v);
		    if(edges.isEmpty()) return;
		    buff.append("\n  " + v + " --> ");
		    for(Pair<Vertex,Label> edge : edges) {
			buff.append("\n    --" + edge.right + "--> " + edge.left);
		    }
		}
	    },
	    null);
	buff.append("\n}");
	return buff.toString();
    }


    /* unfinished implementation: compute smallest weight path between two vertices.
    public List<Pair<Vertex,Label>> findMinPath(Vertex src, Vertex dst, final WeightOp<Label> wop) {
	final Map<Vertex,Label> v2cost = new LinkedHashMap<Vertex,Label>();
	WorkSet<Vertex> ws = new WorkPriorityQueue<Vertex>(new Comparator<Vertex>() {
	    public int compare(Vertex v1, Vertex v2) {
		Label l1 = v2cost.get(v1);
		Label l2 = v2cost.get(v2);
		if(l1 == null) {
		    return l2 == null ? 0 : -1;
		}
		if(l2 == null) {
		    return l1 == null ? 0 : +1;
		}
		if(wop.lt(l1, l2)) return -1;
		if(wop.lt(l2, l1)) return +1;
		return 0;
	    }
	});

	// TODO: CONTINUE	
    }

    public static interface WeightOp<Label> {
	Label add(Label l1, Label l2);
	boolean lt(Label l1, Label l2);
    }

    */


    private static <Vertex,Label> List<Vertex> convert(final List<Pair<Vertex,Label>> list) {
	return 
	    DSUtil.<Pair<Vertex,Label>,Vertex>mapList
	    (list, 
	     new Function<Pair<Vertex,Label>,Vertex>() {
		public Vertex f(Pair<Vertex,Label> p) {
		    return p.left;
		}
	    });
    }

    /*
    private static <Vertex,Label> List<Pair<Vertex,Label>> convert(final List<Vertex> list, final Label defLabel) {
	return 
	    DSUtil.<Vertex, Pair<Vertex,Label>>mapList
	    (list,
	     new Function<Vertex,Pair<Vertex,Label>>() {
		public Pair<Vertex,Label> f(Vertex v) { 
		    return new Pair<Vertex,Label>(v, defLabel);
		}
	    });
    }
    */
    
}
