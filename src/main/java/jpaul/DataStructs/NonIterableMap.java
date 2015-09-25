// NonIterableMap.java, created Wed Nov 30 22:45:15 2005 by salcianu
// Copyright (C) 2005 Alex Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * <code>NonIterableMap</code> is a very simple association map that
 * CANNOT be traversed.  As such, it avoids the non-determinism
 * problems of the <code>HashMap</code>s, without the cost of building
 * a linked list (as <code>LinkedHashMap</code> does).  The idea of
 * having such a class occured to me while trying to find which
 * <code>HashMap</code>s do not affect the externally-visible
 * determinism of a piece of code.  Clearly, all maps that are used
 * only as asssociation maps (without ever being iterated upon) can be
 * made HashMap.  One can use a NonIterableMap instead, and get the
 * type system check the lack of iterations.
 * 
 * @author  Alex Salcianu - salcianu@alum.mit.edu
 * @version $Id: NonIterableMap.java,v 1.4 2006/02/15 17:59:16 salcianu Exp $ */
public class NonIterableMap<K,V> implements Serializable {

    private static final long serialVersionUID = -3026270662223147684L;

    /** Creates a <code>NonIterableMap</code> backed by a private
        <code>HashMap</code> with the default initial capacity. */
    public NonIterableMap() {
        map = new HashMap<K,V>();
    }

    /** Creates a <code>NonIterableMap</code> backed by a private
        <code>HashMap</code> of a certain initial capacity. */
    public NonIterableMap(int initialCapacity) {
	map = new HashMap<K,V>(initialCapacity);
    }

    private final Map<K,V> map;


    /** Associates the key <code>key</code> with the value
        <code>value</code> in <code>this</code> association map.
        Returns the previous value attached to <code>key</code> if
        any, or <code>null</code> oterwise. */
    public V put(K key, V value) {
	return map.put(key, value);
    }

    /** Returns the value associated with <code>key</code> in
        <code>this</code> association map, or <code>null</code> if no
        such value exists. */
    public V get(K key) {
	return map.get(key);
    }
    
    /** Remove any association for the key <code>key</code>.  Returns
        the value that <code>key</code> was mapped to, or
        <code>null</code> if no such value exists. */
    public V remove(K key) {
	return map.remove(key);
    }


    /** Checks whether <code>this</code> association map contains any
        association for the key <code>key</code>. */
    public boolean containsKey(K key) {
	return map.containsKey(key);
    }

    /** Checks whether <code>this</code> association map is empty. */
    public boolean isEmpty() {
	return map.isEmpty();
    }

    /** Removes all associations from <code>this</code> association
        map. */
    public void clear() {
	map.clear();
    }

}
