// ImmutableCompoundIterable.java, created Tue Jul 12 06:36:43 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Iterator;
import java.util.NoSuchElementException;

import jpaul.Misc.Function;

/**
   <code>ImmutableCompoundIterable</code> allows the construction of an
   IMMUTABLE <code>Iterable</code> by merging several smaller
   <code>Iterable</code>s.  Given an iterable with elements
   <code>a0</code>, <code>a1</code>, ..., <code>ak</code>, and a
   function <code>f</code> that transforms each <code>a</code>i into
   an iterable, the <code>ImmutableCompoundIterable</code> will contain all
   elements from <code>f(a0)</code>, followed by all the elements from
   <code>f(a1)</code> and so on.

   <p>The elements of a <code>ImmutableCompoundIterable</code> are generated
   lazily, as we iterate over them.  This technique is better than
   eager iteration in the case when (1) <code>f</code> is simple; (2)
   the original <code>Iterable</code> changes dynamically; (3) the
   number of traversals is small; or (3) the number of elements is very
   large.

   <p> <strong>Note on immutability:</code> While it is possible to
   implement a mutable CompoundIterable (i.e., the user is able to
   remove elements), that would be very hard to specify: e.g., what if
   the function always returns a brand-new <code>Iterable</code> that
   ignores the past removals?

   @author  Alexandru Salcianu - salcianu@alum.mit.edu
   @version $Id: ImmutableCompoundIterable.java,v 1.3 2006/03/14 02:29:31 salcianu Exp $ */
public class ImmutableCompoundIterable<A,B> implements Iterable<B> {
    
    /** Creates an <code>ImmutableCompoundIterable</code>. 

	@param collA  An iterable of elements of type <code>A</code>.

	@param a2iter A function that transforms each element from
	<code>collA</code> into an <code>Iterable</code> of
	<code>B</code>s. */
    public ImmutableCompoundIterable(Iterable<A> collA, Function<A,Iterable<B>> a2iter) {
	this.collA = collA;
	this.a2iter = a2iter;
    }

    private final Iterable<A> collA;
    private final Function<A,Iterable<B>> a2iter;

    public Iterator<B> iterator() {
	return new Iterator<B>() {
	    private Iterator<A> itA = collA.iterator();
	    private Iterator<B> itB = null;

	    public boolean hasNext() {
		if(itB == null) {
		    if(!itA.hasNext()) return false;
		    itB = a2iter.f(itA.next()).iterator();
		}
		if(itB.hasNext()) return true;
		while(!itB.hasNext()) {
		    if(!itA.hasNext()) return false;
		    itB = a2iter.f(itA.next()).iterator();
		}
		return true;
	    }

	    public B next() {
		if(!hasNext())
		    throw new NoSuchElementException();
		return itB.next();
	    }

	    public void remove() {
		throw new UnsupportedOperationException("Immutable Compound Iterable");
	    }
	};
    }

    public String toString() {
	return DSUtil.iterable2coll(this).toString();
    }
    
}
