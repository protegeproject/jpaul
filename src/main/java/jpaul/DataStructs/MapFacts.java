// MapFacts.java, created Thu Aug 18 09:16:00 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Comparator;

/**
 * <code>MapFacts</code> contains several common map factories.  For
 * each kind of map factory, we have a corresponding static method.
 * 
 * <b>Note:</b> some old map factories that used to exist as separate
 * classes are now static inner classes of this class.  They are
 * provided mostly to simplify porting old code (programmers only have
 * to change a few <code>import</code> statements).
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapFacts.java,v 1.6 2006/03/14 02:29:31 salcianu Exp $
 */
public abstract class MapFacts {

    /** Returns a map factory that generates
	<code>LinkedHashMap</code>s.  <code>LinkedHashMap</code>s are
	great for applications that use a few large maps.  They also
	offer predictable iteration order (identical to the
	<i>insertion</i> order).  A <code>LinkedHashMap</code>s is
	only slightly slower than a <code>HashMap</code> (iteration is
	actually faster: linear in the actual size, independent of the
	capacity).  Therefore, in the interest of simplicity, instead
	of a factory for <code>HashMap</code>s and a factory for
	<code>LinkedHashMap</code>s, <code>jpaul</code> offers only
	the latter. */
    public static <K,V> MapFactory<K,V> hash() {
	return new HashMapFactory<K,V>();
    }


    /** <code>HashMapFactory</code> is a map factory that generates
	<code>LinkedHashMap</code>s.  <code>LinkedHashMap</code>s are
	great for applications that use a few large maps.  They also
	offer predictable iteration order.
    
	@deprecated  As of jpaul 2.2, use {@link #hash()} instead. */
    public static class HashMapFactory<K,V> extends MapFactory<K,V> {
    private static final long serialVersionUID = -4752157389672136828L;

	public Map<K,V> create() {
	    return new LinkedHashMap<K,V>();
	}
	
	public Map<K,V> create(Map<K,V> m) {
	    if(m instanceof LinkedHashMap/*<K,V>*/) {
		@SuppressWarnings("unchecked")
		Map<K, V> result = (Map<K,V>) ((LinkedHashMap<K,V>) m).clone();
		return result;
	    }
	    else {
		return new LinkedHashMap<K,V>(m);
	    }
	}
    }



    /** Returns a map factory that generates <code>TreeMap</code>s.
	<code>TreeMap</code>s are great for applications that use many
	small maps. 

	@param comp Comparator used internally by the generated
	<code>TreeMap</code>s. */
    public static <K,V> MapFactory<K,V> tree(Comparator<K> comp) {
	return new TreeMapFactory<K,V>(comp);
    }
    

    /** <code>TreeMapFactory</code> is a map factory that generates
	<code>TreeMap</code>s.  <code>TreeMap</code> are great for
	applications that use many small maps. 

	@deprecated  As of jpaul 2.2, use {@link #tree(Comparator)} instead. */
    public static class TreeMapFactory<K,V> extends MapFactory<K,V> {
    
	private static final long serialVersionUID = 5487287370814696757L;

    /** Creates a <code>TreeMapFactory</code>. 
	    
	    @param comp Comparator used internally by the generated
	    <code>TreeMap</code>s. */
	public TreeMapFactory(Comparator<K> comp) {
	    this.comp = comp;
	}
	private final Comparator<K> comp;
	
	public Map<K,V> create() { return new TreeMap<K,V>(comp); }
	
	public Map<K,V> create(Map<K,V> m) {
	    if(m instanceof TreeMap/*<K,V>*/) {
		@SuppressWarnings("unchecked")
		TreeMap<K, V> result = (TreeMap<K,V>) (((TreeMap<K,V>) m).clone());
		return result;
	    }
	    return super.create(m);
	}
    }


    /** Copy-on-write maps.  UNIMPLEMENTED YET. */
    public static <K,V> MapFactory<K,V> cow(MapFactory<K,V> underMapFact) {
	throw new UnsupportedOperationException("Not implemented yet");
    }


    /** Returns a map factory that generates
        <code>NoCompTreeMap</code>.  {@link
        jpaul.DataStructs.NoCompTreeMap NoCompTreeMap} is a binary
        tree-backed map that does not require a user-defined {@link
        java.util.Comparator Comparator} between keys. */
    public static <K,V> MapFactory<K,V> noCompTree() {
	return new MapFactory<K,V>() {
    	    
            private static final long serialVersionUID = 5687187370812696757L;

	    public Map<K,V> create() { return new NoCompTreeMap<K,V>(); }
	    
	    public Map<K,V> create(Map<K,V> m) {
		if(m instanceof NoCompTreeMap/*<K,V>*/) {
		    return ((NoCompTreeMap<K,V>) m).clone();
		}
		return super.create(m);
	    }
	};
    }

}
