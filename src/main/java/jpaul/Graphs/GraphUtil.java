// GraphUtil.java, created Thu Jul  7 11:19:45 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import jpaul.DataStructs.DSUtil;

/**
 * <code>GraphUtil</code> is a wrapper for various graph utilities.
 * It is a non-instantiatable class with useful static members; it is a 
 * graph-equivalent of <code>java.util.Collections</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: GraphUtil.java,v 1.9 2006/01/29 16:05:29 adam_kiezun Exp $ */
public final class GraphUtil {

    /** Make sure nobody can instantiate this class. */
    private GraphUtil() {/*no instances*/}


    /** Unions two <code>BiDiNavigator</code>.  For each vertex
        <code>v</code>, successors/predecessors indicated by the union
        navigator consist of all the successors/predecessors indicated
        by <code>n1</code>, followed by all the
        successors/predecessors indicated by <code>n2</code>.  Note
        that the same successor/predecessor may be indicated twice,
        once by <code>fn1</code> and once by <code>fn2</code>.  */
    public static <V> BiDiNavigator<V> unionNav(final BiDiNavigator<V> n1, final BiDiNavigator<V> n2) {
	return new BiDiNavigator<V>() {
	    public List<V> next(V v) {
		return DSUtil.unionList(n1.next(v), n2.next(v));
	    }
	    public List<V> prev(V v) {
		return DSUtil.unionList(n1.prev(v), n2.prev(v));
	    }	    
	};
    }

    /** Unions two <code>ForwardNavigator</code>.  For each vertex
        <code>v</code>, successors indicated by the union navigator
        consist of all the successors indicated by <code>fn1</code>,
        followed by all the successors indicated by <code>fn2</code>.
        Note that the same successors may be indicated twice, once by
        <code>fn1</code> and once by <code>fn2</code>. */
    public static <V> ForwardNavigator<V> unionFwdNav(final ForwardNavigator<V> fn1,
						      final ForwardNavigator<V> fn2) {
	return new ForwardNavigator<V>() {
	    public List<V> next(V v) {
		return DSUtil.unionList(fn1.next(v), fn2.next(v));
	    }
	};
    }


    /** Returns the reverse of a given navigator.  In the resulting
	reverse navigator, the <code>next<code> method returns the
	same result as <code>nav.prev</code>, and the <code>prev<code>
	method returns the same result as <code>nav.next</code>. */
    public static <Vertex> BiDiNavigator<Vertex> reverseBiDiNavigator(BiDiNavigator<Vertex> nav) {
	return new ReverseBiDiNavigator<Vertex>(nav);
    }

    private static class ReverseBiDiNavigator<Vertex> implements BiDiNavigator<Vertex> {
    
	/** Creates a <code>ReverseNavigator</code> that is based on the
	    existent navigator <code>old_nav</code>, but traverses the
	    graph in reverse direction. */
	public ReverseBiDiNavigator(BiDiNavigator<Vertex> old_nav) {
	    this.old_nav = old_nav;
	}
	private final BiDiNavigator<Vertex> old_nav;
	
	public List<Vertex> next(Vertex node) { return old_nav.prev(node); }
	public List<Vertex> prev(Vertex node) { return old_nav.next(node); }
    }


    public static <Vertex> ForwardNavigator<Vertex> cachedFwdNavigator(final ForwardNavigator<Vertex> fnav) {
	return new ForwardNavigator<Vertex>() {
	    private Map<Vertex,List<Vertex>> cache = new LinkedHashMap<Vertex,List<Vertex>>();
	    public List<Vertex> next(Vertex v) {
		List<Vertex> next = cache.get(v);
		if(next == null) {
		    next = fnav.next(v);
		    cache.put(v, next);
		}
		return next;
	    }
	};
    }

}
