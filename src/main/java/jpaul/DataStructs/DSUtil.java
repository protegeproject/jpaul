// DSUtil.java, created Mon Jul 11 06:44:34 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.AbstractList;

import jpaul.Misc.Predicate;
import jpaul.Misc.Function;
import jpaul.Misc.IdFunction;

/**
 * <code>DSUtil</code> is a wrapper for commonly used data-structure
 * utilities.  It is a non-instantiatable class with useful
 * static members, similar to <code>java.util.Collections</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: DSUtil.java,v 1.25 2006/03/14 02:29:30 salcianu Exp $ */
public final class DSUtil {

    /** Make sure nobody can instantiate this class. */
    private DSUtil() {
    	// no instances
    }


    /** Checks equality of two objects; deals with the case when
        <code>obj1</code> is null, and next invokes
        <code>equals</code>. */
    public static <E> boolean checkEq(E obj1, E obj2) {
	if(obj1 == null) return obj2 == null;
	// make sure we do this short-circuit, just in case it's not done by equals
	if(obj1 == obj2) return true;
	return obj1.equals(obj2);
    }

    /** Returns <code>"null"</code> if the argument <code>o</code> is
        null, and <code>o.toString()</code> otherwise. */
    public static String toString(Object o) {
	if(o == null) return "null";
	return o.toString();
    }

    /** Transforms a normal <code>Iterator</code> into an unmodifiable
        one.  The resulting iterator does not support
        <code>remove</code>. */
    public static <E> Iterator<E> unmodifiableIterator(final Iterator<E> it) {
	return new Iterator<E>() {
	    public boolean hasNext() { return it.hasNext(); }
	    public E       next()    { return it.next(); }
	    public void    remove()  { throw new UnsupportedOperationException(); }
	};
    }


    /** Transforms a normal <code>Iterable</code> into an unmodifiable
        one.  The iterator over the resulting <code>Iterable</code>
        does not support <code>remove</code>. */
    public static <E> Iterable<E> unmodifiableIterable(final Iterable<E> itrbl) {
	return new Iterable<E>() {
	    public Iterator<E> iterator() {
		return DSUtil.<E>unmodifiableIterator(itrbl.iterator());
	    }
	};
    }

    
    /** Computes, in linear time, the length of an
        <code>Iterable</code>.  This is done by iterating over all
        elements from <code>itrbl</code> and counting their number. */
    public static <E> int iterableSize(Iterable<E> itrbl) {
	int size = 0;
	for (Iterator<E> iter = itrbl.iterator(); iter.hasNext();) {
		iter.next();
		size++;
	}
	return size;
    }


    /** Checks whether the iterable <code>itrbl</code> has exactly
        <code>k</code> elements.  Instead of computing the length of
        <code>itrbl</code> (complexity: linear in the length) and
        comparing it with <code>k</code>, we try to iterate exactly
        <code>k</code> times and next check that <code>itrbl</code> is
        exhausted.

	Complexity: linear in <code>k</code>.  Hence, this test is
	very fast for small <code>k</code>s. */
    public static <E> boolean iterableSizeEq(Iterable<E> itrbl, int k) {
	// 1) try to iterate k times over itrbl;
	Iterator<E> it = itrbl.iterator();
	for(int i = 0; i < k; i++) {
	   if(!it.hasNext()) return false;
	   it.next();
	}
	// 2) next check that there are no more elements in itrbl
	return !it.hasNext();
    }


    /** Checks whether the iterable <code>itrbl</code> has more than
        <code>k</code> elements.  Instead of computing the length of
        <code>itrbl</code> (complexity: linear in the length) and
        comparing it with <code>k</code>, we try to iterate exactly
        <code>k</code> times and next check that <code>itrbl</code> is NOT
        exhausted.

	Complexity: linear in <code>k</code>.  Hence, this test is
	very fast for small <code>k</code>s. */
    public static <E> boolean iterableSizeGt(Iterable<E> itrbl, int k) {
	// 1) try to iterate k times over itrbl;
	Iterator<E> it = itrbl.iterator();
	for(int i = 0; i < k; i++) {
	   if(!it.hasNext()) return false;
	   it.next();
	}
	// 2) next check that there are still some elements in itrbl
	return it.hasNext();
    }


    /** Checks whether <code>itrbl</code> contains the element
        <code>elem</code>. */
    public static <E> boolean iterableContains(Iterable<E> itrbl, E elem) {
	for(E e : itrbl) {
	    if(e.equals(elem)) return true;
	}
	return false;
    }


    /** Returns an immutable <code>Collection</code> view of an
        <code>Iterable</code>. */
    public static <E> Collection<E> iterable2coll(final Iterable<E> itrbl) {
	return new AbstractCollection<E>() {
	    public Iterator<E> iterator() {
		return itrbl.iterator();
	    }
	    public int size() {
		return DSUtil.iterableSize(itrbl);
	    }
	};
    }

    /** Returns an immutable <code>List</code> that is the union of
        two lists: it contains first the elements from
        <code>l1</code>, and next the elements from
        <code>l2</code>. */
    public static <E> List<E> unionList(List<E> l1, List<E> l2) {
	return new UList<E>(l1, l2);
    }

    private static class UList<E> extends AbstractList<E> {
	public UList(List<E> l1, List<E> l2) {
	    this.l1 = l1;
	    this.l2 = l2;
	}
	private final List<E> l1;
	private final List<E> l2;
	
	public Iterator<E> iterator() {
	    return new UIter<E>(l1.iterator(), l2.iterator());
	}
	
	public int size() {
	    return l1.size() + l2.size();
	}
	
	public E get(int index) {
	    if(index < l1.size())
		return l1.get(index);
	    return l2.get(index - l1.size());
	}
    }

    /** Returns the immutable union of two collections. */
    public static <E> Collection<E> unionColl(Collection<E> c1, Collection<E> c2) {
	return new UColl<E>(c1, c2);
    }

    /** Returns the immutable union of three collections. */
    public static <E> Collection<E> unionColl(Collection<E> c1, Collection<E> c2, Collection<E> c3) {
	return new UColl<E>(c1, c2, c3);
    }

    private static class UColl<E> extends AbstractCollection<E> {
    
	/** Creates a <code>UColl</code>. */
	public UColl(Collection<E> c1, Collection<E> c2) {
	    this.c1 = c1;
	    this.c2 = c2;
	}
	
	public UColl(Collection<E> c1, Collection<E> c2, Collection<E> c3) {
	    this(c1, new UColl<E>(c2, c3));
	}
	
	private final Collection<E> c1;
	private final Collection<E> c2;
	
	public Iterator<E> iterator() {
	    return new UIter<E>(c1.iterator(), c2.iterator());
	}
	
	public int size() {
	    return c1.size() + c2.size();
	}
    }

    
    /** Returns an immutable <code>Iterable</code> that is the union
        of two <code>Iterable</code>s.  The resulting
        <code>Iterable</code> contains first all elements from
        <code>it1</code>, and next all elements from <code>it2</code>.  */
    public static <E> Iterable<E> unionIterable(Iterable<E> it1, Iterable<E> it2) {
	return unionIterable(Arrays.<Iterable<E>>asList(it1, it2));
    }

    /** Returns an immutable <code>Iterable</code> that is the union
        of several <code>Iterable</code>s (in the order thet are given
        in <code>its</code>). */
    public static <E> Iterable<E> unionIterable(Iterable<Iterable<E>> its) {
	return new ImmutableCompoundIterable<Iterable<E>,E>
	    (its,
	     new IdFunction<Iterable<E>>());
    }

    /** Put in <code>newColl</code> all elements of <code>coll</code>
        that satisfy the predicate <code>pred</code>.

	@return newColl, the collection with the filtered elements. */
    public static <E> Collection<E> filterColl(Iterable<E> coll,
					       Predicate<E> pred,
					       Collection<E> newColl) {
	for(E elem : coll) {
	    if(pred.check(elem)) {
		newColl.add(elem);
	    }
	}
	return newColl;
    }

    /** Maps collection <code>coll</code> into a new collection
        (stored in <code>newColl</code>), according to the function
        <code>func</code>. */
    public static <E1,E2> Collection<E2> mapColl(Iterable<E1> coll, Function<E1,E2> func, Collection<E2> newColl) {
	for(E1 elem : coll) {
	    newColl.add(func.f(elem));
	}
	return newColl;
    }

    /** Similar to <code>mapColl</code>, but filters out all
        <code>null</code> elements produced by <code>func</code>. 

	@see #mapColl(Iterable, Function, Collection) */
    public static <E1,E2> Collection<E2> mapColl2(Iterable<E1> coll, Function<E1,E2> func, Collection<E2> newColl) {
	for(E1 elem : coll) {
	    E2 img = func.f(elem);
	    if(img != null)
		newColl.add(img);
	}
	return newColl;
    }

    /** Similar to teh other <code>mapColl</code> method, but the
        function is given as a map.  This map is expected to map all
        elements from the entry collection <code>coll</code>.

	@see #mapColl(Iterable, Function, Collection) */
    public static <E1,E2> Collection<E2> mapColl(Iterable<E1> coll, Map<E1,E2> map, Collection<E2> newColl) {
	return mapColl(coll, map2fun(map), newColl);
    }


    /** Construct a <code>Function</code> based on a map.  The
        resulting function will throw an exception if invoked on an
        element that is unassigned in <code>map</code>. */
    public static <K,V> Function<K,V> map2fun(final Map<K,V> map) {
	return new Function<K,V>() {
	    public V f(K key) {
		V value = map.get(key);
		if(value == null) {
		    throw new Error("No value for " + key);
		}
		return value;
	    }
	};
    }

    /** @return Returns the first element from <code>iterable</code>.
        Returns <code>null</code> if <code>iterable</code> is empty. */
    public static <E> E getFirst(Iterable<E> iterable) {
	try {
	    return iterable.iterator().next();
	}
	catch(NoSuchElementException excp) {
	    return null;
	}
    }


    public static <A,B> Iterator<B> mapIterator(final Iterator<A> it, final Function<A,B> f) {
	return new Iterator<B>() {
	    public boolean hasNext() { return it.hasNext(); }
	    public B next() { return f.f(it.next()); }
	    public void remove() { it.remove(); }
	};
    }

    public static <A,B> Iterable<B> mapIterable(final Iterable<A> itrblA, final Function<A,B> f) {
	return new Iterable<B>() {
	    public Iterator<B> iterator() {
		return mapIterator(itrblA.iterator(), f);
	    }
	};
    }

    public static <A,B> List<B> mapList(final List<A> list, final Function<A,B> f) {
	return new AbstractList<B>() {
	    public B get(int i) {
		return f.f(list.get(i));
	    }
	    public int size() {
		return list.size();
	    }
	    public Iterator<B> iterator() {
		return DSUtil.<A,B>mapIterator(list.iterator(), f);
	    }
	};
    }


    /** Checks whether two collections are disjoint.  The two
        collections must contain elements with a common superclass
        <code>S</code>.  The worst-case complexity is proportional to
        the product of the lengths of the two collections.  The
        implementation checks that none of the elements of the
        smallest collection is present in the second one.  If
        membership testing is O(1), the complexity of the disjointness
        test is linear in the length of the smallest of the two
        collections.  */
    public static <S, A extends S, B extends S> boolean disjoint(Collection<A> c1, Collection<B> c2) {
	if(c1.size() > c2.size()) return DSUtil.<S,B,A>disjoint(c2, c1);
	for(A a : c1) {
	    if(c2.contains(a)) {
		return false;
	    }
	}
	return true;
    }


    public static <E> List<E> select(List<E> list, List<Integer> indices, List<E> sel) {
	Iterator<Integer> itIndex = indices.iterator();
	if(!itIndex.hasNext()) {
	    return sel;
	}
	int desiredIndex = itIndex.next().intValue();

	// Resulting list (to be filled in with selected elements).
	int currIndex = 0;
	for(E elem : list) {
	    if(currIndex == desiredIndex) {
		// It is possible to select the same element multiple times;
		// indices may contain duplicates, e.g., [2, 4, 4, 10] .
		while(currIndex == desiredIndex) {
		    sel.add(elem);
		    if(!itIndex.hasNext()) return sel;
		    desiredIndex = itIndex.next().intValue();
		}
		if(desiredIndex < currIndex)
		    throw new Error("List of indices should be non-decreasing; instead, " + indices);
	    }
	    currIndex++;
	}
	// if we arrive here, it must be that there are still unused indices
	throw new Error("Not enough elements in the 1st list arg; list = " + 
			list + "\n\tindices = " + indices);
    }


    public static <E> String iterableToString(Iterable<E> itrbl) {
	StringBuffer buff = new StringBuffer();
	buff.append("[ ");
	boolean first = true;
	for(E elem : itrbl) {
	    if(!first) {
		buff.append(", ");
	    }
	    first = false;
	    buff.append(elem);
	}
	buff.append(" ]");
	return buff.toString();
    }

}
