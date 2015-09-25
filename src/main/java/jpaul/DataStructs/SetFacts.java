// SetFacts.java, created Thu Aug 18 07:35:17 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * <code>SetFacts</code> contains common set factories.  For
 * each kind of set factory, we have a corresponding static method.
 *
 * <b>Note:</b> some old set factories that used to exist as separate
 * classes are now static inner classes of this class.  They are
 * provided mostly to simplify porting old code (programmers only have
 * to change a few <code>import</code> statements).
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SetFacts.java,v 1.11 2006/03/14 02:29:31 salcianu Exp $ */
public final class SetFacts {

    /** Make sure nobody can instantiate this class. */
    private SetFacts() {/* no instances */ }

    /** Returns a set factory that generates
	<code>LinkedHashSet</code>s.  <code>LinkedHashSet</code>s are
	great for applications that use a few large sets; in addition,
	they provide predictable iteration order (identical to the
	<i>insertion<i> order).  A <code>LinkedHashSet</code> is only
	slightly slower than a <code>HashSet</code> (iteration is
	actually faster: linear in the actual size, independent of the
	capacity).  Therefore, in the interest o simplicity, instead
	of offering a factory for plain <code>HashSet</code>s, and a
	factory for <code>LinkedHashSet</code>s, <code>jpaul</code>
	offers only the latter. */
    public static <E> SetFactory<E> hash() {
	return new HashSetFactory<E>();
    }


    /** <code>HashSetFactory</code> is a set factory that generates
	<code>LinkedHashSet</code>s.  <code>LinkedHashSet</code>s are
	great for applications that use a few large sets.  They also
	offer predictable iteration order.

	@deprecated As of jpaul 2.2, use {@link #hash()} instead. */
    public static class HashSetFactory<E> extends SetFactory<E> {
        private static final long serialVersionUID = -931644816758004864L;
	
        public Set<E> create() {
	    return new LinkedHashSet<E>();
	}
	
	public Set<E> newColl(Collection<E> c) {
	    if(c instanceof LinkedHashSet/*<E>*/) {
		@SuppressWarnings("unchecked")
		Set<E> result = (Set<E>) ((LinkedHashSet<E>) c).clone();
		return result;
	    }
	    else {
		return super.newColl(c);
	    }
	}
    }
    
    

    /** Returns a set factory that generates <code>TreeSet</code>s.
	<code>TreeSet</code>s are great for applications that use many
	small sets.*/
    public static <E> SetFactory<E> tree(Comparator<E> comp) {
	return new TreeSetFactory<E>(comp);
    }


    /** <code>TreeSetFactory</code> is a set factory that generates
	<code>TreeSet</code>s.  <code>TreeSet</code>s are great for
	applications that use many small sets.

	@deprecated As of jpaul 2.2, use {@link #tree(Comparator)} instead. */
    public static class TreeSetFactory<E> extends SetFactory<E> {

        private static final long serialVersionUID = -4493140903394022661L;
	
        /** Creates a <code>TreeSetFactory</code>. 
	    
	    @param comp Comparator used internally by the generated
	    <code>TreeSet</code>s. */
	public TreeSetFactory(Comparator<E> comp) {
	    this.comp = comp;
	}
	private final Comparator<E> comp;
	
	public Set<E> create() {
	    return new TreeSet<E>(comp);
	}
	
	public Set<E> newColl(Collection<E> c) {
	    if(c instanceof TreeSet/*<E>*/) {
		@SuppressWarnings("unchecked")
		TreeSet<E> result = (TreeSet<E>) ((TreeSet<E>) c).clone();
		return result;
	    }
	    return super.newColl(c);
	}
    }


    /** Returns a set factory that generates "copy-on-write" (COW)
	sets.  A COW set shares its representation (also a set) with
	other COW sets, until a mutation occurs.  At that moment, the
	COW set makes a private, exclusive copy of its underlying
	representation, and mutates that copy.

	<p>The internal representation of a COW set maintains a
	"sharing" counter to identify cases when the representation is
	not shared with anyone (and hence, no cloning is necessary
	before a mutation).

	<p>Cloning a COW set is a constant time operation.  COW sets
	are good when it is hard to determine statically whether a
	clone of a set will be mutated: they delay the real cloning
	until the first mutation (if any). 

	@param underSetFact Set factory for generating the sets used
	in the representation of the COW sets. */
    public static <E> SetFactory<E> cow(SetFactory<E> underSetFact) {
	return new jpaul.DataStructs.COWSetFactory<E>(underSetFact);
    }


    /** <code>COWSetFactory</code> generates "copy-on-write" (COW) sets.

	@deprecated  As of jpaul 2.2, use {@link #cow(SetFactory)} instead. */
    @Deprecated
	public static class COWSetFactory<E> extends jpaul.DataStructs.COWSetFactory<E> {
	    private static final long serialVersionUID = 2353743286940510499L;

    /** Creates a <code>COWSetFactory</code>.  
	    
	    @param underSetFact Set factory for generating the sets used
	    in the representation of the COW sets generated by this
	    <code>COWSetFactory</code>. */
	public COWSetFactory(SetFactory<E> underSetFact) {
	    super(underSetFact);
	}
    }


    /** Returns a set factory that generates map-backed sets.  The
        elements from the set are the keys of the map; the values they
        are mapped to do not matter; in practice, the implementation
        will never use <code>null</code> keys, so virtually any map
        factory is good as argument. 

	@param mapFact Factory for the maps used in the representation
	of the generated sets. */
    public static <E> SetFactory<E> mapBased(final MapFactory<E,Object> mapFact) {
	return new SetFactory<E>() {
	    private static final long serialVersionUID = 2323753186840412499L;
	    
	    public Set<E> create() { return new MapBasedSet<E>(mapFact); }
	    
	    public Set<E> newColl(Collection<E> c) {
		if(c instanceof MapBasedSet/*<E>*/) {
		    return ((MapBasedSet<E>) c).clone();
		}
		return super.newColl(c);
	    }
	};
    }


    /** Returns a set factory that generates sets backed by
        <code>NoCompTreeMap</code>s.  {@link
        jpaul.DataStructs.NoCompTreeMap NoCompTreeMap} is a binary
        tree-backed map that does not require a user-defined {@link
        java.util.Comparator Comparator} between keys.  This set
        factory is good for applications that use many small trees and
        when a total order <code>Comparator</code> is hard to
        write. 

	@see #mapBased */
    public static <E> SetFactory<E> noCompTree() {
	return mapBased(MapFacts.<E,Object>noCompTree());
    }

}
