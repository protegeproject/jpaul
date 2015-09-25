// NonIterableSet.java, created Wed Nov 30 22:38:44 2005 by salcianu
// Copyright (C) 2005 Alex Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 * <code>NonIterableSet</code> is a very simple set that CANNOT be
 * traversed.  As such, it avoids the non-determinism problems of the
 * <code>HashSet</code>s, without the cost of building a linked list
 * (as <code>LinkedHashSet</code> does).  The idea of having such a
 * class occured to me while trying to find which
 * <code>HashSet</code>s do not affect the externally-visible
 * determinism of a piece of code.  Clearly, all sets that are used
 * only for membership testing (without ever being iterated upon) can
 * be left to be <code>HashSet</code>.  One can use a
 * <code>NonIterableSet</code> instead, and get the type system check
 * the lack of iterations.
 * 
 * @author  Alex Salcianu - salcianu@alum.mit.edu
 * @version $Id: NonIterableSet.java,v 1.4 2006/02/22 05:04:30 salcianu Exp $ */
public class NonIterableSet<T> implements Serializable {
    
    private static final long serialVersionUID = 4925180938716224828L;

    /** Creates a <code>NonIterableSet</code> backed by a private
        <code>HashSet</code> with the default initial capacity. */
    public NonIterableSet() {
        set = new HashSet<T>();
    }

    /** Creates a <code>NonIterableSet</code> backed by a private
        <code>HashSet</code> of a certain initial capacity. */
    public NonIterableSet(int initialCapacity) {
        set = new HashSet<T>(initialCapacity);
    }

    private final Set<T> set;

    /** Adds element <code>elem</code> to <code>this</code> set.
        Returns <code>true</code> iff <code>elem</code> is a new
        element (i.e., it was not in the set before the call to this
        method). */
    public boolean add(T elem) {
	return set.add(elem);
    }

    /** Adds all elements of collection <code>coll</code> to
        <code>this</code> set.  Returns <code>true</code> iff we added
        at least one new element to this non-iterable set. */
    public boolean addAll(Collection<T> coll) {
	boolean newData = false;
	for(T elem : coll) {
	    if(set.add(elem)) {
		newData = true;
	    }
	}
	return newData;
    }


    /** Checks whether the element <code>elem</code> belongs to
        <code>this</code> set.  */
    public boolean contains(T elem) {
	return set.contains(elem);
    }

    /** Removes the element <code>elem</code> from <code>this</code>
        set. Returns <code>true</code> if the set contained
        <code>elem</code>. */
    public boolean remove(T elem) {
	return set.remove(elem);
    }


    /** Checks whether <code>this</code> set is empty. */
    public boolean isEmpty() {
	return set.isEmpty();
    }

    /** Removes all elements from <code>this</code> set. */
    public void clear() {
	set.clear();
    }

}
