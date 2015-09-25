// WorkSetAbstr.java, created Tue Feb 22 09:24:12 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;

/**
 * <code>WorkSetAbstr</code> implements a <code>WorkSet</code> using a
 * set and an ordered collection (usually a list).  This allows
 * constant-time membership check.  <code>WorkSetAbstr</code> is an
 * abstract class that factors out common implementation code for
 * other classes, e.g., <code>WorkList</code> and
 * <code>WorkStack</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkSetAbstr.java,v 1.10 2006/03/14 02:29:31 salcianu Exp $ */
abstract class WorkSetAbstr<T> implements WorkSet<T> {
    
    // invariant: set and list contain the same set of elements
    private final NonIterableSet<T> set = new NonIterableSet<T>();

    // number of elements in this workset
    // [ I'm not sure what the complexity of Set.size() and List.size()
    // is, so I've decided to keep track of the size on my own. ]
    private int size = 0;

    /** Adds <code>elem</code> to the underlying list.  Subclasses
        will have to choose whether they want to perform the addition
        at the head, respectively at the tail of the list.

	Precondition: <code>elem</code> is not already in the list. */
    protected abstract void addToOrder(T elem);

    /** Returns the first element from the underlying ordered
        collection.  The returned element is removed from the
        underlying ordered collection. */
    protected abstract T extractInOrder();

    /** Returns the underlying ordered collection.  Useful for
        factoring out the code for <code>clear</code> and
        <code>toString</code>. */
    protected abstract Collection<T> underlyingOrder();

    public boolean add(T elem) {
	if(!set.add(elem)) return false;
	addToOrder(elem);
	size++;
	return true;
    }

    public boolean addAll(Collection<T> elems) {
	boolean newInfo = false;
	for(T elem : elems) {
	    if(add(elem))
		newInfo = true;
	}
	return newInfo;
    }

    public T extract() {
	T res = extractInOrder();
	set.remove(res);
	size--;
	return res;
    }

    public void clear() {
	underlyingOrder().clear();
	set.clear();
    }

    public boolean isEmpty() {
	return size == 0;
    }

    public boolean contains(T e) {
	return set.contains(e);
    }

    public int size() {
	return size;
    }

    public String toString() {
	return underlyingOrder().toString();
    }
    
}
