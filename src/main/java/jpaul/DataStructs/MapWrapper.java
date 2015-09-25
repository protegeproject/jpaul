// MapWrapper.java, created Wed Aug 10 08:07:33 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * <code>MapWrapper</code> is a map backed by another map.  All
 * operations on <code>this</code> map are forwarded to the underlying
 * map.  Subclassing this class makes it easier to write map wrappers
 * that change the behaviour of only a few <code>Map</code> methods.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapWrapper.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class MapWrapper<K,V> implements Map<K,V> {
    
    /** Creates a <code>MapWrapper</code>. 

	@param map The underlying, real map */
    public MapWrapper(Map<K,V> map) {
        this.map = map;
    }

    /** Underlying map. */
    protected final Map<K,V> map;

    public void clear() {
	map.clear(); 
    }

    public boolean containsKey(Object key) {
	return map.containsKey(key); 
    }

    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    public Set<Map.Entry<K,V>> entrySet() {
	return map.entrySet();
    }
    
    public boolean equals(Object obj) {
	return map.equals(obj);
    }

    public V get(Object key) {
	return map.get(key);
    }

    public int hashCode() {
	return map.hashCode();
    }
    
    public boolean isEmpty() {
	return map.isEmpty();
    }

    public Set<K> keySet() {
	return map.keySet();
    }

    public V put(K key, V value) {
	return map.put(key, value);
    }

    public void putAll(Map<? extends K,? extends V> t) {
	map.putAll(t);
    }

    public V remove(Object key) {
	return map.remove(key);
    }

    public int size() {
	return map.size();
    }

    public Collection<V> values() {
	return map.values();
    }
}
