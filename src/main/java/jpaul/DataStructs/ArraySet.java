// ArraySet.java, created Wed Dec  7 13:48:14 2005 by salcianu
// Copyright (C) 2005 Alex Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

/**
 * <code>ArraySet</code> is an immutable, array-backed set.  It
 * consumes minimal memory; very good for small sets.
 * 
 * @author  Alex Salcianu - salcianu@alum.mit.edu
 * @version $Id: ArraySet.java,v 1.9 2006/03/14 02:55:23 salcianu Exp $ */
public class ArraySet<T> extends AbstractSet<T> implements Serializable {
    
    private static final long serialVersionUID = 8759323214346590319L;

    /** Creates an <code>ArraySet</code> containing the elements
        present in the set <code>set</code>.  The created
        <code>ArraySet</code> contains the elements of
        <code>set</code> at the point this constructor is invoked;
        future changes to <code>set</code> are not reflected by the
        <code>ArraySet</code>. */
    public ArraySet(Set<T> set) {
	this(set, true);
    }


    /** Creates an <code>ArraySet</code> containing the distinct
        elements from the colection <code>coll</code>.
        <code>coll</code> may contain duplicates, but only distinct
        elements will appear in the created <code>ArraySet</code>.
        Future changes to <code>coll</code> are not reflected by this
        <code>ArraySet</code>.

	<p><strong>Note:</strong> This constructor is more general
	than {@link #ArraySet(Set)}, which may lead to a little bit of
	confusion: a set is also a collection, so both constructors
	apply in certain cases.  Semantically, whether you invoke one
	constructor or the other, it is the same thing.  Still, the
	constructor that takes a set knows that the elements of the
	set are unique (by the contract of a set); the constructor
	that takes a collection has no such guarantee, so it needs to
	avoid inserting equal elements to the constructed
	<code>ArraySet</code>.  So, the constructor that takes a set
	is faster; it's a classic example of using the type system to
	speed-up the program execution.  */
    public ArraySet(Collection<T> coll) {
	this(coll, false);
    }


    /** Creates an <code>ArraySet</code> with the elements given as a
	variable-length list of arguments (instead of a collection).
	E.g., <code>new ArraySet&lt;T&gt;(e1, e2, e3, e4)</code> is
	equivalent to <code>new ArraySet&lt;T&gt;(Arrays.asList(e1,
	e2, e3, e4))</code>. */
    public ArraySet(T... ts) {
	this(Arrays.<T>asList(ts));
    }


    // the underlying array of elements
    private final T[] elemArray;


    /** Use only if you suspect that someone is invoking the full
        {@link #ArraySet(Collection,boolean) constructor} with an
        incorrect <code>collHasDistinctElements</code> argument.  If
        <code>true</code>, everytime that argument is
        <code>trues</code>, the code checks that the collection really
        contains distinct elements.  */
    static boolean DEBUG_DISTINCT_ELEMS = false;


    /** Powerful and unsafe constructor: creates an
	<code>ArraySet</code> containing the distinct elements present
	in the colection <code>coll</code>.  If the second parameter
	<code>collHasDistinctElements</code> is <code>true</code>,
	then this constructor assumes that <code>coll</code> contains
	only distinct elements and skips the costly step of
	determining the unique elements from
	<code>coll</code>. <strong>This feature is unsafe and should
	be used with maximal care!</strong>.

	<p>The created <code>ArraySet</code> contains the elements of
        <code>coll</code> at the point this constructor is invoked;
        future changes to <code>coll</code> are not reflected by the
        <code>ArraySet</code>. */
    public ArraySet(Collection<T> coll, boolean collHasDistinctElements) {
	if(DEBUG_DISTINCT_ELEMS && collHasDistinctElements) {
	    checkHasDistinctElements(coll);
	}

	Collection<T> collDistinctElements =
	    collHasDistinctElements ? 
	    ((Collection<T>) coll) :
	    ((Collection<T>) new LinkedHashSet<T>(coll));

	// this cast is very ugly, but what more can we do here?
    @SuppressWarnings("unchecked")
	T[] elems = (T[]) collDistinctElements.toArray();
    elemArray = elems;
    }


    // Debug code: check that "coll" does not have any duplicates
    private void checkHasDistinctElements(Collection<T> coll) {
	// keep track of the already seen elements
	NonIterableSet<T> seenElems = new NonIterableSet<T>(coll.size());
	for(T elem : coll) {
	    if(!seenElems.add(elem)) {
		throw new IllegalArgumentException("coll has duplicate element:" + elem);
	    }
	}
    }


    // Re-implement the contains method from AbstractSet, for speed reasons
    public boolean contains(Object o) {
	for(T e : elemArray) {
	    if((o == e) || ((o != null) && o.equals(e))) {
		return true;
	    }
	}
	return false;
    }
    

    // The implementations of the following two methods is required in
    // order to implement a real Set on top of an AbstractSet

    public Iterator<T> iterator() {
	return new ArrayIterator<T>(elemArray);
    }

    public int size() {
	return elemArray.length;
    }

}
