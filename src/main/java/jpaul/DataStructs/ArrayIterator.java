// ArrayIterator.java, created Fri Dec  2 12:47:28 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.NoSuchElementException;
import java.util.ListIterator;

/**
 * <code>ArrayIterator</code> is a read-only iterator over an array of
 * elements.
 *
 * @see java.util.ListIterator
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: ArrayIterator.java,v 1.4 2006/01/30 22:49:42 adam_kiezun Exp $ */
public class ArrayIterator<T> implements ListIterator<T> {
    
    /** Creates a <code>ArrayIterator</code>. */
    public ArrayIterator(T... a) {//use varargs to allow flexibility for callers
        if (a == null) throw new IllegalArgumentException("argument cannot be null");
        this.a = a;
    }

    // the array we iterate over
    private final T[] a;

    // the index of the element about to be returned by the next call
    // to next()
    private int index = 0;


    public void add(T o) {
	throw new UnsupportedOperationException("ArrayIterators are read-only!");
    }
    
    public boolean hasNext() {
	return index < a.length;
    }

    public boolean hasPrevious() {
	return index > 0;
    }

    public T next() {
	if(index < a.length) {
	    return a[index++];
	}
	else {
	    throw new NoSuchElementException("ArrayIterator Overflow");
	}
    }

    public int nextIndex() {
	return index;
    }

    public T previous() {
	if(index > 0) {
	    return a[--index];
	}
	else {
	    throw new NoSuchElementException("ArrayIterator Underflow");
	}
    }

    public int previousIndex() {
	return index-1;
    }

    public void remove() {
	throw new UnsupportedOperationException("ArrayIterators are read-only!");
    }

    public void set(T o) {
	throw new UnsupportedOperationException("ArrayIterators are read-only!");
    }
    
}
