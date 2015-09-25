// NoCompTreeMap.java, created Sun Aug 16 15:46:56 2000 by salcianu
// Copyright (C) 2005 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Map;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;
import jpaul.Graphs.BinTreeUtil;
import jpaul.Graphs.BinTreeNavigator;

/**
 * <code>NoCompTreeMap</code> is tree map that does not require any
 * used-defined <code>Comparator</code>.  Instead, the tree is ordered
 * by the relative ordering between the haashcodes of the keys.  The
 * implementation is able to cope with the situation when two
 * non-equal keys have the same hashcode: intuitively, the key that is
 * put first in the map is considered lower than the other one.
 *
 * <p>For the curious programmer, when we add a new mapping, if the
 * key to add has the same hashcode as the key from a tree node, we
 * decend into the right subtree (unless the keys are equal).  This
 * means that if two many keys have the same hashcode, the tree can
 * degenerate into a list.  Still, we think this is unlikely to happen
 * frequently in practice; in practice, we expect the complexity of
 * operations to be more or less logarithmic in the size of the tree.
 *
 * <p>This map is useful to use in programs with many small maps or
 * when coming out with a total ordering between elements is difficult
 * (or the programmer is too lazy to think about such an order).
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: NoCompTreeMap.java,v 1.10 2006/03/14 02:29:31 salcianu Exp $ */
public class NoCompTreeMap<K,V> implements Map<K,V>, Cloneable, Serializable {
    private static final long serialVersionUID = -1874980975202189491L;
	// the number of mappings in this map
    private int size = 0;
    // the root of the binary tree used to store the mapping
    private BinTreeNode<K,V> root = null;

    /** Creates a <code>NoCompTreeMap</code>. */
    public NoCompTreeMap() {/*no code*/}

    /** Creates a <code>NoCompTreeMap</code> with the same mappings as the
     *  given map. */
    public NoCompTreeMap(Map<? extends K, ? extends V> map) {
	this();
	putAll(map);
    }

    public final int size() { return size; }

    public final boolean isEmpty() { return size == 0; }

    public final boolean containsKey(Object key) {
	return _get(key) != null;
    }

    /** Unsupported yet. */
    public final boolean containsValue(Object value) {
	throw new UnsupportedOperationException();
    }

    public V get(Object key) {
	BinTreeNode<K,V> node = _get(key);
	if(node == null) return null;
	return node.value;
    }


    private BinTreeNode<K,V> _get(Object key) { 
	BinTreeNode<K,V> p = root;
	int key_hash_code = key.hashCode();

	while(p != null) {
	    if(key_hash_code < p.keyHashCode) {
		p = p.left;
	    }
	    else {
		if(key_hash_code > p.keyHashCode) {
		    p = p.right;
		}
		else {
		    if(p.key.equals(key)) {
			return p;
		    }
		    else {
			p = p.right;
		    }
		}
	    }
	}

	return null;
    }


    /** Associates the specified value with the specified key in this map. */
    public final V put(K key, V value) {
	BinTreeNode<K,V> prev = null;
	BinTreeNode<K,V> node = root;
	int key_hash_code = key.hashCode();

	while(node != null) {
	    prev = node;
	    if(key_hash_code < node.keyHashCode) {
		node = node.left;
	    }
	    else {
		if((key_hash_code > node.keyHashCode) || !node.key.equals(key)) {
		    node = node.right;
		}
		else {
		    cachedHashCode -= node.hashCode();
		    V temp = node.value;
		    node.value = value;
		    cachedHashCode += node.hashCode();
		    return temp;
		}
	    }
	}

	size++;

	BinTreeNode<K,V> new_node = new BinTreeNode<K,V>(key, value);
	cachedHashCode += new_node.hashCode(); // invalidate the cached hash code

	if(prev == null) {
	    root = new_node;
	    return null;
	}

	if(key_hash_code < prev.keyHashCode) {
	    prev.left = new_node;
	}
	else {
	    prev.right = new_node;
	}

	return null;
    }


    /** Removes the mapping previously attached to <code>key</code>.
	Returns the old mapping if any, or <code>null</code> otherwise. */
    public final V remove(Object key) {
	
	if(key == null) return null;

	int key_hash_code = key.hashCode();
	BinTreeNode<K,V> prev = null;
	int son = 0;
	BinTreeNode<K,V> node = root;

	while(node != null) {
	    if(key_hash_code < node.keyHashCode) {
		prev = node;
		node = node.left;
		son  = 0;
	    }
	    else {
		if((key_hash_code > node.keyHashCode) || !node.key.equals(key)) {
		    prev = node;
		    node = node.right;
		    son  = 1;
		}
		else {
		    size--;
		    cachedHashCode -= node.hashCode();
		    return remove_node(node, prev, son);
		}
	    }
	}
	return null;
    }

    // Remove the BinTreeNode pointed to by node. prev is supposed to be the
    // parent node, pointing to node through his left (son == 0) respectively
    // right (son == 1) link. Returns the old value attached to node.
    private final V remove_node(BinTreeNode<K,V> node, BinTreeNode<K,V> prev, int son) {
	if(node.left == null)
	    return remove_semi_leaf(node, prev, son, node.right);
	if(node.right == null)
	    return remove_semi_leaf(node, prev, son, node.left);

	// The BinTreeNode to replace node in the tree. This is either the
	// next or the precedent node (in the order of the hashCode's.
	// We decide a bit randomly, to gain some balanceness.
	BinTreeNode<K,V> m = 
	    (node.keyHashCode % 2 == 0) ?
	    extract_next(node) : extract_prev(node);

	return finish_removal(node, prev, son, m);
    }


    // Remove a [semi]-leaf (a node with at least one of its sons absent.
    // In this case, we simply "bypass" the link from the predecessor (if any)
    // to the only predecessor of node that could exist.
    private final V remove_semi_leaf(BinTreeNode<K,V> node, BinTreeNode<K,V> prev,
				     int son, BinTreeNode<K,V> m) {
	if(prev == null) {
	    root = m;
	}
	else {
	    if(son == 0)
		prev.left = m;
	    else
		prev.right = m;
	}

	return node.value;
    }


    // Terminal phase of the node removal. Returns the old value attached to
    // node. node, prev and son are as for remove_node; m points to the
    // BinTreeNode that should replace node in the tree.
    private final V finish_removal(BinTreeNode<K,V> node, BinTreeNode<K,V> prev,
				   int son, BinTreeNode<K,V> m) {
	if(m != null) { // set up the links for m
	    m.left  = node.left;
	    m.right = node.right;
	}
	if(prev == null)
	    root = m;
	else {
	    if(son == 0)
		prev.left = m;
	    else
		prev.right = m;
	}

	return node.value;
    }

    // Finds the leftmost BinTreeNode from the right subtree of node;
    // removes it from that subtree and returns it.
    private final BinTreeNode<K,V> extract_next(BinTreeNode<K,V> node) {
	BinTreeNode<K,V> prev = node.right;
	BinTreeNode<K,V> curr = prev.left;

	if(curr == null) {
	    node.right = node.right.right;
	    return prev;
	}

	while(curr.left != null) {
	    prev = curr;
	    curr = curr.left;
	}

	prev.left = curr.right;
	return curr;
    }

    // Finds the rightmost BinTreeNode from the left subtree of node;
    // removes it from that subtree and returns it.
    private final BinTreeNode<K,V> extract_prev(BinTreeNode<K,V> node) {
	BinTreeNode<K,V> prev = node.left;
	BinTreeNode<K,V> curr = prev.right;

	if(curr == null) {
	    node.left = node.left.left;
	    return prev;
	}

	while(curr.right != null) {
	    prev = curr;
	    curr = curr.right;
	}

	prev.right = curr.left;
	return curr;
    }


    /** Copies all of the mappings from the specified map to this map. */
    public final void putAll(Map<? extends K,? extends V> map) {
	for(Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }


    public final void clear() {
	size = 0;
	cachedHashCode = 0;
	root = null;
    }


    /** Returns an unmodifiable collection view of the values from this map. */
    public final Collection<V> values() {
	return new AbstractCollection<V>() {
	    public Iterator<V> iterator() {
		final Iterator<Map.Entry<K,V>> ite = entryIterator();
		return new Iterator<V>() {
		    public boolean hasNext() { return ite.hasNext(); }
		    public void    remove()  { ite.remove(); }
		    public V       next()    { return ite.next().getValue(); }
		};
	    }
	    public int size() {
		return size;
	    }
	};
    }


    private Iterator<Map.Entry<K,V>> entryIterator() {
	return BinTreeUtil.<Map.Entry<K,V>,BinTreeNode<K,V>>inOrder(root, binTreeNav);
    }


    /** Returns an unmodifiable set view of the map entries. */
    public final Set<Map.Entry<K,V>> entrySet() {

	return new AbstractSet<Map.Entry<K,V>>() {
	    public Iterator<Map.Entry<K,V>> iterator() {
		return entryIterator();
	    }
	    public int size() {
		return size;
	    }
	};
    }

    /** Returns an unmodifiable set view of the keys contained in this map. */
    public final Set<K> keySet() {
	return new AbstractSet<K>() {
	    public Iterator<K> iterator() {
		final Iterator<Map.Entry<K,V>> ite = entryIterator();
		return new Iterator<K>() {
		    public boolean hasNext() { return ite.hasNext(); }
		    public void    remove()  { ite.remove(); }
		    public K       next()    { return ite.next().getKey(); }
		};
	    }
	    public int size() {
		return size;
	    }
	};
    }


    private BinTreeNode<K,V> copy_tree(BinTreeNode<K,V> node) {
	if(node == null) return null;
	BinTreeNode<K,V> newnode = new BinTreeNode<K,V>(node.key, node.value);

	newnode.left  = copy_tree(node.left);
	newnode.right = copy_tree(node.right);
	
	return newnode;
    }


    public NoCompTreeMap<K,V> clone() {
	try {
        @SuppressWarnings("unchecked")
	    NoCompTreeMap<K,V> newmap = (NoCompTreeMap<K,V>) super.clone();
	    newmap.root = copy_tree(root);
	    return newmap;
	} catch(CloneNotSupportedException e) {
	    throw new InternalError();
	}
    }


    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof Map/*<K,V>*/))
	    return false;
	
	@SuppressWarnings("unchecked")
	Map<K,V> m2 = (Map<K,V>) o;

	Set<Map.Entry<K,V>> set1 = this.entrySet();
	Set<Map.Entry<K,V>> set2 = m2.entrySet();

	// two maps are equal if they have the same set of entries
	return set1.equals(set2);
    }


    public int hashCode() {
	return cachedHashCode;
    }
    private int cachedHashCode = 0; // computed incrementally


    private static class BinTreeNode<K,V> implements Map.Entry<K,V>, Serializable {
	private static final long serialVersionUID = -3462363742216115133L;
	final K key;
	V value;
	final int keyHashCode;

	BinTreeNode<K,V> left  = null;
	BinTreeNode<K,V> right = null;

	BinTreeNode(final K key, final V value) {
	    this.key    = key;
	    this.value  = value;
	    keyHashCode = key.hashCode();
	}

	public String toString() {
	    return "<" + key + "," + value + ">";
	}

	// implementation of the Map.Entry methods
	public K getKey() { return key; }
	public V getValue() { return value; }
	public V setValue(V value) { 
	    V oldValue = this.value;
	    this.value = value;
	    return oldValue;
	}
	public int hashCode() {
	    return 
		((key == null)   ? 0 : keyHashCode) ^
		((value == null) ? 0 : value.hashCode());
	}
	public boolean equals(Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if(!(o instanceof Map.Entry/*<K,V>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;

	    @SuppressWarnings("unchecked")
	    Map.Entry<K,V> e2 = (Map.Entry<K,V>) o;
	    return 
		(key == null ?
		 e2.getKey() == null : key.equals(e2.getKey()))  &&
		(value == null ?
		 e2.getValue()==null : value.equals(e2.getValue()));
	}
    }

    private transient final BinTreeNavigator<BinTreeNode<K,V>> binTreeNav = new BinTreeNavigator<BinTreeNode<K,V>>() {
	public BinTreeNode<K,V> left(BinTreeNode<K,V> node) {
	    return node.left;
	}
	public BinTreeNode<K,V> right(BinTreeNode<K,V> node) {
	    return node.right;
	}
    };

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[");
	build_str(root, buffer);
	buffer.append(" ]");
	return buffer.toString();
    }

    private void build_str(final BinTreeNode<K,V> node, final StringBuffer buffer) {
	if(node == null) return;
	build_str(node.left,  buffer);
	buffer.append(" <" + node.key + "," + node.value + ">");
	build_str(node.right, buffer);
    }

}
