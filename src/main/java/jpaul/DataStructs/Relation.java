// Relation.java, created Tue Jan 11 14:52:48 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import jpaul.Misc.Debug;
import jpaul.Misc.Predicate;

/**
 * <code>Relation</code> is a binary relation, accepting one to many
 * and many to one mappings.
 *
 * <p>Unless otherwise specified a relation is modifiable and
 * thread-UNsafe.  Similar to the Collection framework, we provide
 * unmodifiable and thread-safe wrappers:
 *
 * <ul>
 * <li>{@link  Relation#unmodifiableRelation(Relation)  Relation.unmodifiableRelation(Relation)}
 * <li>{@link  Relation#synchronizedRelation(Relation)  Relation.synchronizedRelation(Relation)}
 * </ul>
 * 
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Relation.java,v 1.25 2006/02/26 16:34:03 salcianu Exp $ */
public abstract class Relation<K,V> implements Cloneable, Serializable {

    private static final long serialVersionUID = 1092295047324324L;

    /** <code>Relation.EntryVisitor</code> is a wrapper for a function
	that is called on a relation entry of the form
	<code>&lt;key,value&gt;</code>.  Used by the method {@link
	Relation#forAllEntries(jpaul.DataStructs.Relation.EntryVisitor)}.  */
    public static interface EntryVisitor<Key,Value> {
	/** Visits a <code>&lt;key,value&gt;</code> entry of a
	    relation.  May throw an
	    <code>InterruptTraversalException</code> in order to
	    indicate the desire to terminate the enclosing relation
	    traversal (the method {@link Relation#forAllEntries(jpaul.DataStructs.Relation.EntryVisitor)}). */
	public void visit(Key key, Value value) throws InterruptTraversalException;
    }
    
    /** Adds the pair <code>&lt;key, value&gt;</code> to the relation.
	Returns <code>true</code> if the new relation is bigger. */
    public abstract boolean add(K key, V value);

    /** Puts <code>key</code> in relation to each element of the
	collection <code>values</code>.  Returns <code>true</code> if
	the relation becomes bigger. */
    public abstract boolean addAll(K key, Collection<V> values);


    /** Similar to {@link #addAll} except that the type of the
        collection <code>values</code> allows more flexibility.  Puts
        <code>key</code> in relation to each element from the
        collection <code>values</code>.  Returns <code>true</code> if
        the relation becomes bigger.

	<p><strong>Only for the very curious users:</strong> A
	legitimate question is &quot;why two almost identical versions
	of <code>addAll</code>?&quot; The answer has to do with speed
	optimizations and historic reasons.  In program analysis, it
	is frequent to propagate large relations, with only the
	mappings for a few keys being changed.  Consequently,
	<code>jpaul</code> offers special <i>Copy-On-Write</code>
	(COW) sets and other datastructures (see {@link
	jpaul.DataStructs.COWSetFactory}).  For the rest of this
	discussion, let's assume that <code>key</code> is not in
	relation with any value yet.  The historically first version,
	{@link #addAll}, allows the possible underlying set factory to
	"clone" the COW set <code>values</code> (if
	<code>values</code> is indeed a COW set).  "Cloning" a COW
	datastructure is very fast, and consumes almost no memory at
	all, leading to enormous speedups (e.g., in Alex Salcianu's
	pointer analysis prototype).  Unfortunately, cloning in the
	presence of wildcards would violate type safety: cloning a
	<code>Set&lt;? extends V&gt;</code> does not produce a
	<code>Set&lt;V&gt;</code>.  Instead, the usual implementations
	of {@link #addAll2} work by creating a brand-new
	<code>Set&lt;V&gt;</code> and adding the new values to it,
	one-by-one.  Retrospectively, the design could have been
	better, but we didn't want to do anything that would break the
	performances of the applications that already use
	<code>jpaul</code>.  */
    public abstract boolean addAll2(K key, Collection<? extends V> values);


    /** Removes all mappings stored in <code>this</code> relation. */
    public abstract void clear();

    /** Removes the relation between <code>key</code> and 
	<code>value</code>.
	@return <code>true</code> iff the relation changed */ 
    public abstract boolean remove(K key, V value);

    /** Removes the relation between <code>key</code> and 
	any element from <code>values</code>.
	@return <code>true</code> iff the relation changed */ 
    public abstract boolean removeAll(K key, Collection<V> values);

    /** Removes all the relations attached to <code>key</code>.
	@return <code>true</code> iff the relation changed */    
    public abstract boolean removeKey(K key);

    /** Removes all the keys that satisfy <code>predicate.check()</code>.
	@return <code>true</code> iff the relation changed */
    public abstract boolean removeKeys(Predicate<K> predicate);

    /** Removes all the values that satisfy <code>predicate.check()</code>.
	@return <code>true</code> iff the relation changed */
    public abstract boolean removeValues(Predicate<V> predicate);


    /** Checks the existence of the relation <code>&lt;key,value&gt;</code>. */
    public boolean contains(K key, V value) {
	return _getValues(key).contains(value);
    }

    /** Checks the existence of the relation between <code>key</code>
        and every element from <code>values</code>. */
    public boolean containsAll(K key, Collection<? extends V> values) {
	return _getValues(key).containsAll(values);
    }

    /** Checks the existence of the <code>key</code> key in this relation. */
    public abstract boolean containsKey(K key);


    /** Tests if this relation is empty or not. */
    public abstract boolean isEmpty();


    /** Returns the image of <code>key</code> through this relation.
	The returned collection IS IMMUTABLE.
	Returns <code>Collections.emptySet()</code> if no value is attached to <code>key</code>. */
    public final Set<V> getValues(K key) {
	Set<V> set = _getValues(key);
	if(set == null)
	    return Collections.emptySet();
	else 
	    return Collections.unmodifiableSet(set);
    }

    /** Method used by the internal implementation of the
        <code>Relation</code> or its subclasses.  Similar to the
        user-level {@link #getValues} but the returned set IS MUTABLE.
        Mutating this set affects the set of values that are
        associated with a given key; therefore, this method cannot be
        used by <code>Relation</code> clients directly. */
    protected abstract Set<V> _getValues(K key);


    /** Returns an IMMUTABLE view of all the keys appearing in
	<code>this</code> relation.  If you want to delete a key, then
	use {@link #removeKey}.  */
    public abstract Set<K> keys();


    /** Returns an IMMUTABLE view of all the values appearing in
        <code>this</code> relation.  The view may contain the same
        value twice, if it is associated with two distinct keys.  */
    public abstract Iterable<V> values();


    /** Combines <code>this</code> relation with relation
	<code>rel</code>.  A <code>null</code> parameter is considered
	to be an empty relation.

	@return <code>true</code> iff <code>this</code> relation has
	changed. */
    public abstract boolean union(Relation<K,V> rel);


    /** Checks the equality of two relations */
    public abstract boolean equals(Object o);


    /** Visits all the entries <code>&lt;key,value&gt;</code> of
	<code>this</code> relation and calls
	<code>visitor.visit</code> on each of them.  This traversal of
	the relation entries can be stopped at any point by throwing
	an {@link jpaul.DataStructs.InterruptTraversalException
	InterruptTraversalException} from the visitor; the exception
	is caught internally by the implementation of this method. */
    public void forAllEntries(EntryVisitor<K,V> visitor) {
	try {
	    for(K key : keys()) {
		for(V value : _getValues(key)) {
		    visitor.visit(key, value);
		}
	    }
	}
	catch(InterruptTraversalException itex) {
	    // Do nothing; InterruptTraversalException is only a way
	    // to terminate the traversal prematurely.
	}
    }


    /** Return a relation that is the reverse of <code>this</code>
	relation.  The reverse relation contains a pair &lt;a,b&gt;
	iff <code>this</code> relation contains the pair &lt;b,a&gt;.
	Once created, the reverse relation is independent of this
	relation: mutation on one of them will not affect the other
	one.  The reverse relation is represented as a
	hashset/hashmap-based {@link jpaul.DataStructs.MapSetRelation
	MapSetRelation}.

	<p><strong>Warning:</strong> This default representation may
	be very inefficient for small relations.  If speed is an
	issue, please use {@link #revert(Relation)}: it allows you to
	create the revert relation, using your favorite Relation
	implementation.  */
    public Relation<V,K> revert() {
	return revert(new MapSetRelation<V,K>());
    }


    /** Revert <code>this</code> relation and store the result into
	the relation <code>result</code>. &lt;a,b&gt; appears in the
	reverse relation iff &lt;b,a&gt; appears in <code>this</code>
	relation.  Returns the new relation (ie,
	<code>result</code>). */
    public Relation<V,K> revert(final Relation<V,K> result) {
	forAllEntries(new EntryVisitor<K,V>() {
		public void visit(K key, V value) {
		    result.add(value, key);
		}
	    });
	return result;
    }


    /** Returns the number of &lt;key,value&gt; pairs in
        <code>this</code> relation.  Linear in the size of the
        relation.  This may be also implemented in O(1) by
        incrementally updating a <code>size</code> field, but that may
        complicate the code in the presence of subclassing, etc.  Will
        think about it if it becomes a problem. */
    public int size() {
	int size = 0;
	for(K key : keys()) {
	    size += getValues(key).size();
	}
	return size;
    }


    /** Checks whether <code>this</code> relation maps each key to a
        single value.  Mathematically, checks whether this relation is
        a (partial) function. */
    public boolean isFunction() {
	for(K key : this.keys()) {
	    if(this.getValues(key).size() != 1)
		return false;
	}
	return true;
    }


    public Relation<K,V> clone() {
	try {
        @SuppressWarnings("unchecked")
	    Relation<K, V> result = (Relation<K,V>) super.clone();
        return result;
	}
	catch(CloneNotSupportedException e) {
	    // should not happen ...
	    throw new Error(e);
	}
    }


    /** Pretty-print function for debug.
	<code>rel1.equals(rel2) <==> rel1.toString().equals(rel2.toString())</code> */
    public String toString() {
	StringBuffer buffer = new StringBuffer();

	buffer.append("{");
	
	for(K key : Debug.sortedCollection(keys())) {
	    buffer.append("\n  ");		
	    buffer.append(key);
	    buffer.append(" -> ");
	    buffer.append(Debug.stringImg(_getValues(key)));
	}
	
	buffer.append("\n }\n");
	
	return buffer.toString();
    }

    // THE FOLLOWING FEATURES WOULD BE NICE TO ADD IN THE FUTURE

    /* Removes all the relations involving at least one object that
	satisfy <code>predicate.check()</code>. */
    //public void removeObjects(Predicate predicate);


    /* Returns the subrelation of this relation that contains
	only the keys that appear in <code>selected_keys</code>. */
    //public Relation<K,Value> select(Collection<K> selected_keys);


    /* Convert <code>this</code> relation through the mapping
	<code>map</code>.  The converted mapping contains all pairs
	<code>(a,b)</code> such that there exists <code>c,d</code>
	such that <code>(c,d)</code> appears in <code>this</code>
	mapping, and map maps c to a and d to b.  If an object is not
	mapped to anything by <code>map</code>, it will be mapped to
	itself by default.  The result is stored in
	<code>result</code>.  Returns the converted mapping (ie,
	result).  */
    //public<K2,V2> Relation<K2,V2> convert(Map<K,K2> keyMap, Map<V,V2> valueMap, Relation<K2,V2> result);


    /** Returns an unmodifiable wrapper backed by the given relation
	<code>rel</code>.  This allows "read-only" access, although
	changes in the backing collection show up in this view.
	Attempts to modify the relation will fail with {@link
	UnsupportedOperationException}. */
    public static <K,V> Relation<K,V> unmodifiableRelation(final Relation<K,V> rel) {
	return new UnmodifiableRelation<K,V>(rel);
    }


    private static class UnmodifiableRelation<K,V> extends Relation<K,V> implements Serializable {

	private static final long serialVersionUID = 23409453709234L;

	public UnmodifiableRelation(Relation<K,V> rel) {
	    this.rel = rel;
	}

	// underlying relation
	private final Relation<K,V> rel;

	public boolean add(K key, V value) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean addAll(K key, Collection<V> values) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean addAll2(K key, Collection<? extends V> values) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public void clear() {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean remove(K key, V value) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean removeAll(K key, Collection<V> values) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean removeKey(K key) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean removeKeys(Predicate<K> predicate) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean removeValues(Predicate<V> predicate) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean contains(K key, V value) {
	    return rel.contains(key, value);
	}
	public boolean containsKey(K key) {
	    return rel.containsKey(key);
	}
	public boolean isEmpty() { return rel.isEmpty(); }
	// No need to put an immutable wrapper around the returned
	// set.  This method is used only internally; moreover, it
	// can be mutated only by methods that mutate the overall
	// relation, and these methods are already forbidden.
	protected Set<V> _getValues(K key) { return rel._getValues(key); }
	// we don't wrap rel.keys() in an immutable wrapper:
	// rel.keys() is supposed to be immutable anyway.
	public Set<K> keys() { return rel.keys(); }
	// same immutability considerations as for keys()
	public Iterable<V> values() { return rel.values(); }
	public boolean union(Relation<K,V> rel) {
	    throw new UnsupportedOperationException("unmodifiable Relation");
	}
	public boolean equals(Object o) { return rel.equals(o); }
	public void forAllEntries(EntryVisitor<K,V> visitor) {
	    rel.forAllEntries(visitor);
	}
	public Relation<K,V> clone() { return super.clone(); }
    }


    /** Returns a synchronized (thread-safe) relation wrapper backed by
	the given relation <code>rel</code>.  Each operation
	synchronizes on <code>rel</code>.

	<p>For operations that return a collection (e.g., {@link
	#keys()}, {@link #values()}), it is strongly recommended to
	synchronize the whole block that (1) invokes the
	collection-returning relation operation, and (2) uses that
	collection.  This is similar to the recommended usage pattern
	for the JDK synchronized collections (e.g.,
	<code>Collections.synchronizedSet</code>.  Here is an example:

	<pre>
	Relation&lt;K,V&gt; syncRel = Relation.synchronizedRelation(someRel);
	...
	synchronized(syncRel) {
          Iterator&lt;K&gt; it = syncRel.keys().iterator();
	  while(it.hasNext()) {
	    K key = it.next();
	    ...
	  }
        }
        </pre>

        This pattern prevents non-deterministic, hard-to-reproduce
        behavior due to modifications from other threads on the
        relation you iterate over.  */
    public static <K,V> Relation<K,V> synchronizedRelation(final Relation<K,V> rel) {
	return new SynchronizedRelation<K,V>(rel); 
    }


    private static class SynchronizedRelation<K,V> extends Relation<K,V> implements Serializable {

	private static final long serialVersionUID = 823471587244858L;

	public SynchronizedRelation(Relation<K,V> rel) {
	    this.rel = rel;
	}

	// underlying relation
	private final Relation<K,V> rel;

	public synchronized boolean add(K key, V value) {
	    return rel.add(key, value);
	}
	public synchronized boolean addAll(K key, Collection<V> values) {
	    return rel.addAll(key, values);
	}
	public synchronized boolean addAll2(K key, Collection<? extends V> values) {
	    return rel.addAll2(key, values);
	}
	public synchronized void clear() {
	    rel.clear();
	}
	public synchronized boolean remove(K key, V value) {
	    return rel.remove(key, value);
	}
	public synchronized boolean removeAll(K key, Collection<V> values) {
	    return rel.removeAll(key, values);
	}
	public synchronized boolean removeKey(K key) {
	    return rel.removeKey(key);
	}
	public synchronized boolean removeKeys(Predicate<K> predicate) {
	    return rel.removeKeys(predicate);
	}
	public synchronized boolean removeValues(Predicate<V> predicate) {
	    return rel.removeValues(predicate);
	}
	public synchronized boolean contains(K key, V value) {
	    return rel.contains(key, value);
	}
	public synchronized boolean containsKey(K key) {
	    return rel.containsKey(key);
	}
	public synchronized boolean isEmpty() { 
	    return rel.isEmpty(); 
	}
	// No need to put a synchronization wrapper around this
	// method.  This method is used only internally;
	// synchronization is handled by the caller.
	protected Set<V> _getValues(K key) { return rel._getValues(key); }
	
	public synchronized Set<K> keys() { 
	    return rel.keys();
	}
	
	public synchronized Iterable<V> values() { 
	    return rel.values();
	}
	
	public synchronized boolean union(Relation<K,V> otherRel) {
	    return rel.union(otherRel);
	}
	
	public synchronized boolean equals(Object o) { 
	    return rel.equals(o); 
	}
	
	public synchronized void forAllEntries(EntryVisitor<K,V> visitor) {
	    rel.forAllEntries(visitor);
	}
	
	public synchronized Relation<K,V> clone() { 
	    return super.clone();
	}
    }

}
