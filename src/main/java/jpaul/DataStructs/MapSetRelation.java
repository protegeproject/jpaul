// MapSetRelation.java, created Tue Jan 11 14:52:48 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedList;

import jpaul.Misc.Predicate;
import jpaul.Misc.Function;

/**
 * <code>MapSetRelation</code> is an implementation of the
 * <code>Relation</code> interface based on a <code>Map</code> from
 * keys to <code>Set</code>s of values.
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapSetRelation.java,v 1.19 2006/03/23 15:51:37 adam_kiezun Exp $ */
public class MapSetRelation<K,V> extends Relation<K,V> implements Serializable, Cloneable {

    private static final long serialVersionUID = 937979529035501744L;

	/** Constructs a <code>Relation</code> represented using a
        <code>LinkedHashMap</code> from keys to
        <code>LinkedHashSet</code>s of values.  Consumes a lot of
        memory but fast for large relation.  */
    public MapSetRelation() {
	this(MapFacts.<K,Set<V>>hash(), SetFacts.<V>hash());
    }


    /** Constructs a <code>Relation</code> represented by a
	<code>Map</code> from keys to <code>Set</code>s of values.
	The map is created by <code>mapFact</code> and the sets by
	<code>setFact</code>.

	@see MapFacts
	@see SetFacts */
    public MapSetRelation(MapFactory<K,Set<V>> mapFact, SetFactory<V> setFact) {
	this.mapFact = mapFact;
	this.setFact = setFact;

	this.map = mapFact.create();
    }

    private final MapFactory<K,Set<V>> mapFact;
    private final SetFactory<V> setFact;


    /** The top-level <code>Hashtable</code>. */
    private Map<K,Set<V>> map;

    public boolean add(K key, V value) {
	Set<V> set = map.get(key);
	if(set == null) {
	    set = setFact.create();
	    map.put(key, set);
	}
	return set.add(value);
    }


    public boolean addAll(K key, Collection<V> values) {
	if(values.isEmpty()) return false;
	Set<V> set = map.get(key);
	if(set == null) {
	    // allow setFact to clone "values", if this improves the
	    // performances; a particularly sweet case is when
	    // "values" is already a copy-on-write set.
	    set = setFact.newColl(values);
	    map.put(key, set);
	    return true;
	}
	return set.addAll(values);
    }


    public boolean addAll2(K key, Collection<? extends V> values) {
	if(values.isEmpty()) return false;
	Set<V> set = map.get(key);
	if(set == null) {
	    // create a brand-new set
	    set = setFact.create();
	    map.put(key, set);
	}
	return set.addAll(values);
    }


    public void clear() {
	map.clear();
    }


    public boolean remove(K key, V value) {
	Set<V> set = map.get(key);
	if(set == null) return false;
	boolean changed = set.remove(value);
	if(set.isEmpty())
	    map.remove(key);
	return changed;
    }


    public boolean removeAll(K key, Collection<V> values) {
	Set<V> set = map.get(key);
	if(set == null) return false;
	boolean changed = set.removeAll(values);
	if(set.isEmpty())
	    map.remove(key);
	return changed;
    }


    public boolean removeKey(K key) {
	// key was mapped to at least one value
	//  iff hash maps key to a non-empty set
	//  iff hash maps key to a non-null value
	// (we maintain the invariant that hash never maps a key to an empty set)
	return (map.remove(key) != null);
    }


    public boolean removeKeys(Predicate<K> predicate) {
	boolean changed = false;
	for(Iterator<K> it = map.keySet().iterator(); it.hasNext(); ) {
	    K key = it.next();
	    if(predicate.check(key)) {
		it.remove();
		changed = true;
	    }
	}
	return changed;
    }


    private boolean removeValues(K key, Predicate<V> predicate) {
	boolean changed = false;
	Set<V> values = map.get(key);
	for(Iterator<V> it = values.iterator(); it.hasNext(); ) {
	    V value = it.next();
	    if(predicate.check(value)) {
		it.remove();
		changed = true;
	    }
	}
	if(values.isEmpty()) {
	    map.remove(key);
	}
	return changed;
    }


    public boolean removeValues(Predicate<V> predicate) {
	boolean changed = false;
	for(K key : new LinkedList<K>(keys())) {
	    if(removeValues(key, predicate)) {
		changed = true;
	    }
	}
	return changed;
    }
    

    public boolean containsKey(K key) {
	return map.containsKey(key);
    }


    public boolean isEmpty() {
	return map.isEmpty();
    }


    protected final Set<V> _getValues(K key) {
	Set<V> res = map.get(key);
	if(res == null) {
	    return Collections.emptySet();
	}
	return res;
    }


    public Set<K> keys() {
	return Collections.<K>unmodifiableSet(map.keySet());
    }


    public Iterable<V> values() {
	// We rely on the fact that CompoundIterable is immutable, in
	// order to respect our contract of returning an immutable.
	// We have to change this if CompoundIterable becomes
	// immutable.
	return 
	    new ImmutableCompoundIterable<K,V>
	    (keys(),
	     new Function<K,Iterable<V>>() {
		public Iterable<V> f(K key) {
		    return getValues(key);
		}
	    });
    }


    public boolean union(Relation<K,V> rel) {
	if(rel == null) return false;
	boolean changed = false;
	for(K key : rel.keys()) {
	    if(this.addAll(key, rel._getValues(key))) {
		changed = true;
	    }
	}
	return changed;
    }


    /** Complexity: linear in the number of (key,value) pairs from the
	relation.

	We could maintain the hashCode incrementally, keeping the
	amortized cost of all operations to O(1).  However, that
	complicates the code significantly, and I'm not sure it's that
	useful: how many times does one use a set of relations?  The
	Java collections do not compute their hashcode incrementally
	either. */
    public int hashCode() {
	hashCode = 0;
	forAllEntries(new EntryVisitor<K,V>() {
	    public void visit(K key, V value) {
		hashCode += key.hashCode() + value.hashCode();
	    }
	});
	return hashCode;
    }
    // We could declare this as a local variable of hashCode().
    // Still, in order to allow it to be accessed from the visitor, we
    // would need to make it final, and in that case the visitor
    // cannot modify it ...
    private int hashCode;


    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
    if(! (o instanceof Relation)) return false;
    
	// In Java there is no way to do a REAL dynamic
	// cast to Relation<K,V>. Because of type erasure, the line
	// below is not executed as such at runtime ...
	@SuppressWarnings("unchecked")
	Relation<K,V> r2 = (Relation<K,V>) o;

	// 1. check the relations have the same keys
	Set<K> set1 = this.keys();
	Set<K> set2 = r2.keys();
	if(!set1.equals(set2)) return false;

	// 2. check each key is mapped to the same values
	for(K key : set1) {
	    Set<V> set_a = this._getValues(key);
	    Set<V> set_b = r2._getValues(key);
	    if(!set_a.equals(set_b)) return false;
	}
	
	return true;
    }


    /** Creates a new, independent relation (independent = the
	operations on the new relation won't affect the old one). */
    public MapSetRelation<K,V> clone() {
	MapSetRelation<K,V> newRel = (MapSetRelation<K,V>) super.clone();

	newRel.map = mapFact.create();
	for(K key : this.keys()) {
	    newRel.map.put(key, setFact.create(this._getValues(key)));
	}

	return newRel;
    }


    /*
    public void removeObjects(Predicate predicate) {
	removeKeys(predicate);
	removeValues(predicate);
    }

    public Relation select(Collection selected_keys) {
	Relation rel2 = new MapSetRelation();
	for(Iterator it = keys().iterator(); it.hasNext(); ) {
	    Object key = it.next();
	    if(!selected_keys.contains(key)) continue;
	    rel2.addAll(key, getValues(key));
	}
	return rel2;
    }
    */

    /*
    public Relation convert(final Map map, final Relation result) {
	forAllEntries(new EntryVisitor() {
		public void visit(Object key, Object value) {
		    Object keyp = map.get(key);
		    if(keyp == null) keyp = key;
		    Object valuep = map.get(value);
		    if(valuep == null) valuep = value;
		    result.add(keyp, valuep);
		}
	    });
	return result;
    }
    */

}
