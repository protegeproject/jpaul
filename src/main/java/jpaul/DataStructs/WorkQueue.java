// WorkQueue.java, created Tue Oct  4 18:29:33 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.AbstractQueue;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * <code>WorkQueue</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkQueue.java,v 1.4 2006/03/14 02:29:31 salcianu Exp $
 */
public class WorkQueue<T> extends AbstractQueue<T> {
    
    public WorkQueue() { this(128); }

    /** Creates a <code>WorkQueue</code>. */
    public WorkQueue(int blockCapacity) {
	this.blockCapacity = blockCapacity;

        headNode = new Node<T>(blockCapacity);
	headNode.next = headNode;
	headNode.prev = headNode;
	headIdx = 0;

	tailNode = headNode;
	tailIdx = 0;
    }

    private final int blockCapacity;

    private static class Node<T> {
	Node(int blockCapacity) {
	    content = new Object[blockCapacity];
	}
	Node<T> next;
	Node<T> prev;
	Object[] content;
    }

    private Node<T> headNode;
    private int headIdx;

    private Node<T> tailNode;
    private int tailIdx;

    private int size = 0;
    private int version = 0;

    public boolean offer(T elem) {
	tailNode.content[tailIdx] = elem;
	tailIdx++;
	if(tailIdx == blockCapacity) {
	    if(tailNode.next == headNode) {
		Node<T> newNode = new Node<T>(blockCapacity);
		newNode.next  = headNode;
		headNode.prev = newNode;
		newNode.prev  = tailNode;
		tailNode.next = newNode;
	    }
	    tailNode = tailNode.next;
	    tailIdx = 0;
	}
	size++;
	version++;
	return true;
    }

    public boolean isEmpty() {
	return size == 0;
    }

    public T peek() {
	if(isEmpty()) return null;
	@SuppressWarnings("unchecked")
	 T peek = (T) headNode.content[headIdx];
	return peek;
    }

    public T poll() {
	if(isEmpty()) return null;
	@SuppressWarnings("unchecked")
	T elem = (T) headNode.content[headIdx];
	// possibly enable some GC
	headNode.content[headIdx] = null;
	headIdx++;
	if(headIdx == blockCapacity) {
	    headIdx = 0;
	    headNode = headNode.next;
	}
	size--;
	version++;
	return elem;
    }


    public int size() {
	return size;
    }


    public Iterator<T> iterator() {
	return new Iterator<T>() {
	    private final int expectedVersion = version;
	    private Node<T> currNode = headNode;
	    private int currIdx = headIdx;
	    public boolean hasNext() {
		return (currNode != tailNode) || (currIdx != tailIdx);
	    }
	    public T next() {
		if(version != expectedVersion) {
		    throw new ConcurrentModificationException();
		}
		@SuppressWarnings("unchecked")
		T elem = (T) currNode.content[currIdx];
		currIdx++;
		if(currIdx == blockCapacity) {
		    currNode = currNode.next;
		    currIdx = 0;
		}
		return elem;
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }
}
