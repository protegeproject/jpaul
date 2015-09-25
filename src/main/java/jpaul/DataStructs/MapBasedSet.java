// MapBasedSet.java, created Thu Aug 18 07:49:19 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.Iterator;

/**
 * <code>MapBasedSet</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapBasedSet.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $
 */
class MapBasedSet<E> implements Set<E>, Cloneable, Serializable {
    
    private static final long serialVersionUID = -5972006737936348285L;

    /** Creates a <code>MapBasedSet</code>. */
    public MapBasedSet(MapFactory<E,Object> mapFact) {
	this.mapFact = mapFact;
        this.map = mapFact.create();
    }
    
    private Map<E,Object> map;
    private final MapFactory<E,Object> mapFact;

    private static final Object VALUE = new Object();

    public boolean add(E elem) {
	if(map.put(elem, VALUE) == null) {
	    hashCode += (elem == null) ? 0 : elem.hashCode();
	    return true;
	}
	return false;
    }

    public boolean addAll(Collection<? extends E> c) {
	boolean changed = false;
	for(E elem : c) {
	    if(add(elem)) {
		changed = true;
	    }
	}
	return changed;
    }

    public void clear() {
	map.clear();
	hashCode = 0;
    }
    
    public boolean contains(Object o) { 
	return map.containsKey(o);
    }
    
    public boolean containsAll(Collection<?> c) {
	for(Object o : c) {
	    if(!contains(o)) {
		return false;
	    }
	}
	return true;
    }

    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof Set/*<E>*/)) return false;
	if(this.hashCode() != o.hashCode()) return false;

	return map.keySet().equals(o);
    }
    
    public int hashCode()  { return hashCode; }
    private int hashCode = 0;

    public boolean isEmpty() { return map.isEmpty(); }
    
    public boolean remove(Object o) {
	if(map.remove(o) != null) {
	    hashCode -= (o == null) ? 0 : o.hashCode();
	    return true;
	}
	return false;
    }

    public boolean removeAll(Collection<?> c) {
	boolean changed = false;
	for(Object o : c) {
	    if(remove(o)) {
		changed = true;
	    }
	}
	return changed;
    }
    
    public boolean retainAll(Collection<?> c) {
	boolean changed = false;
	for(Iterator<E> it = map.keySet().iterator(); it.hasNext(); ) {
	    E elem = it.next();
	    if(!c.contains(elem)) {
		it.remove();
		hashCode -= (elem == null) ? 0 : elem.hashCode();
		changed = true;
	    }
	}
	return changed;
    }
    
    public int size() { return map.size(); }

    public Object[] toArray()      { return map.keySet().toArray(); }
    public <T> T[]  toArray(T[] a) { return map.keySet().toArray(a); }
    
    public MapBasedSet<E> clone() {
	try {
        @SuppressWarnings("unchecked")
	    MapBasedSet<E> cloneSet = (MapBasedSet<E>) super.clone();
	    cloneSet.map = mapFact.create(this.map);
	    return cloneSet;
	}
	catch(CloneNotSupportedException cex) {
	    // should not happen
	    throw new Error(cex);
	}
    }
    
    public Iterator<E> iterator() {
	return new Iterator<E>() {
	    private final Iterator<E> itKeys = map.keySet().iterator();
	    public boolean hasNext() {
		return itKeys.hasNext();
	    }
	    public E next() { 
		lastNext = itKeys.next();
		return lastNext;
	    }
	    private E lastNext = null;
	    public void remove() {
		hashCode -= (lastNext == null) ? 0 : lastNext.hashCode();
	    }
	};
    }
    
    public String toString() {
	return map.keySet().toString();
    }

}
