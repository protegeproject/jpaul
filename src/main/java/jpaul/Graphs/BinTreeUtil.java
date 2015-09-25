// BinTreeUtil.java, created Wed Aug 17 13:31:46 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.Iterator;
import java.util.LinkedList;


import jpaul.DataStructs.Pair;
import jpaul.Misc.Action;

/**
 * <code>BinTreeUtil</code> is a wrapper for binary tree utilities.
 * It is a non-instantiatable class with useful static members,
 * similar to <code>java.util.Collections</code>.
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BinTreeUtil.java,v 1.17 2006/03/21 17:37:30 adam_kiezun Exp $ */
public final class BinTreeUtil {

    /** Make sure nobody can instantiate this class. */
    private BinTreeUtil() {/*no instances*/}


    /** Executes an action on all nodes from a tree, in inorder.  For
        trees with sharing, the same tree node object will be visited
        multiple times.  

	@param root  Binary tree root.

	@param binTreeNav  Binary tree navigator.  

	@param action  Action to execute on each tree node. */
    public static <T> void inOrder(T root, BinTreeNavigator<T> binTreeNav, Action<T> action) {
	for(Iterator<T> it = inOrder(root, binTreeNav); it.hasNext(); ) {
	    T treeVertex = it.next();
	    action.action(treeVertex);
	}
    }


    /** Executes an action on all nodes from a tree, in preorder.  For
        trees with sharing, the same tree node object will be visited
        multiple times.  

	@param root  Binary tree root.

	@param binTreeNav  Binary tree navigator.  

	@param action  Action to execute on each tree node. */
    public static <T> void preOrder(T root, BinTreeNavigator<T> binTreeNav, Action<T> action) {
	for(Iterator<T> it = preOrder(root, binTreeNav); it.hasNext(); ) {
	    T treeVertex = it.next();
	    action.action(treeVertex);
	}
    }


    /** Executes an action on all nodes from a tree, in postorder.  For
        trees with sharing, the same tree node object will be visited
        multiple times.  

	@param root  Binary tree root.

	@param binTreeNav  Binary tree navigator.  

	@param action  Action to execute on each tree node. */
    public static <T> void postOrder(T root, BinTreeNavigator<T> binTreeNav, Action<T> action) {
	for(Iterator<T> it = postOrder(root, binTreeNav); it.hasNext(); ) {
	    T treeVertex = it.next();
	    action.action(treeVertex);
	}
    }


    /** Returns an iterator that traverses all vertices of a binary
        tree in inorder.  The iterator is lazy: it does not construct
        an explicit list with the tree vertices in inorder; instead,
        vertices are returned one-by-one, on demand.  The time
        complexity of iterating over an entire tree is linear in the
        tree size; the space complexity is linear in the tree depth.

	<p><b>Note1:</b> the complication with two type parameters
	simulates the common-sense fact that the iterator subtyping is
	co-variant with the element subtyping.

	<p><b>Note2:</b> supports trees with sharing; shared nodes
	will be returned multiple times.

	<p><b>Note3:</b> of we had a parent pointer, it would have
	been possible to do the traversal with O(1) additional space;
	however, to simplify the interface, no such information is
	provided by the tree navigator, and we have to maintain an
	explicit stack of parent nodes.

	@param root The root node of the binary tree. 

	@param binTreeNav The binary tree navigator.  */
    public static <T, T2 extends T> Iterator<T> inOrder(T2 root, BinTreeNavigator<T2> binTreeNav) {
	return new InOrderIterator<T,T2>(root, binTreeNav);
    }


    /** Returns an iterator that traverses all vertices of a binary
        tree in preorder.  The iterator is lazy: it does not construct
        an explicit list with the tree vertices in preorder; instead,
        vertices are returned one-by-one, on demand.  The time
        complexity of iterating over an entire tree is linear in the
        tree size; the space complexity is linear in the tree depth.

	<p>All notes for {@link #inOrder(Object,BinTreeNavigator) inOrder}
	apply to this method too.

	@param root The root node of the binary tree. 

	@param binTreeNav The binary tree navigator.  */
    public static <T, T2 extends T> Iterator<T> preOrder(T2 root, BinTreeNavigator<T2> binTreeNav) {
	return new PreOrderIterator<T,T2>(root, binTreeNav);
    }


    /** Returns an iterator that traverses all vertices of a binary
        tree in postorder.  The iterator is lazy: it does not construct
        an explicit list with the tree vertices in postorder; instead,
        vertices are returned one-by-one, on demand.  The time
        complexity of iterating over an entire tree is linear in the
        tree size; the space complexity is linear in the tree depth.

	<p>All notes for {@link #inOrder(Object,BinTreeNavigator) inOrder}
	apply to this method too.

	@param root The root node of the binary tree. 

	@param binTreeNav The binary tree navigator.  */
    public static <T, T2 extends T> Iterator<T> postOrder(T2 root, BinTreeNavigator<T2> binTreeNav) {
	return new PostOrderIterator<T,T2>(root, binTreeNav);
    }



    private static class InOrderIterator<T, T2 extends T> implements Iterator<T> {
	public InOrderIterator(T2 root, BinTreeNavigator<T2> binTreeNav) {
	    this.binTreeNav = binTreeNav;
	    if(root != null) {
		stack.addFirst(root);
		fillStack();
	    }
	}
	
	private final BinTreeNavigator<T2> binTreeNav;

	/** The top of the stack contains the next node to return.
            The rest of the stack contain the path from the next node
            to return to the root, minus the nodes that have already
            been returned (these are precisely those nodes that
            contain the to of the stack in their right subtree). */
	private final LinkedList<T2> stack = new LinkedList<T2>();
	
	private void fillStack() {
	    for(T2 node = binTreeNav.left(stack.getFirst()); node != null; ) {
		stack.addFirst(node);
		node = binTreeNav.left(node);
	    }
	}

	public boolean hasNext() {
	    return !stack.isEmpty();
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}

	public T next() {
	    // we will return the top of the stack
	    T2 node = stack.removeFirst();
	    // before that, we need to go down into the right subtree (if any)
	    T2 rightSon = binTreeNav.right(node);
	    if(rightSon != null) {
		stack.addFirst(rightSon);
		fillStack();
	    }
	    return node;
	}
    }



    private static class PreOrderIterator<T, T2 extends T> implements Iterator<T> {
	public PreOrderIterator(T2 root, BinTreeNavigator<T2> binTreeNav) {
	    this.binTreeNav = binTreeNav;
	    if(root != null) {
		stack.addFirst(root);
	    }
	}
	
	private final BinTreeNavigator<T2> binTreeNav;

	/** The top of the stack contains the next node to return.
            The rest of the stack contain the nodes that (1) appear on
            the path from the next node to return to the root, and (2)
            their righ subtree has not been explored yet.  */
	private final LinkedList<T2> stack = new LinkedList<T2>();
	
	public boolean hasNext() { return !stack.isEmpty(); }

	public void remove() { throw new UnsupportedOperationException(); }

	public T next() {
	    // we will return the top of the stack
	    T2 resNode = stack.getFirst();

	    // make sure the top of the stack contains the next to return node
	    // 1. try to go into the left subtree
	    T2 leftSon = binTreeNav.left(resNode);
	    if(leftSon != null) {
		stack.addFirst(leftSon);
	    }
	    else { // 2. no left son		
		// pop up elements from the stack until we find a node
		// with a (yet-unexplored) right son ...
		while(!stack.isEmpty()) {
		    T2 node = stack.removeFirst();
		    T2 rightSon = binTreeNav.right(node);
		    if(rightSon != null) {
			// ... push the right son on the stack
			stack.addFirst(rightSon);
			break;
		    }
		}
	    }

	    return resNode;
	}
    }


    private static class PostOrderIterator<T, T2 extends T> implements Iterator<T> {
	public PostOrderIterator(T2 root, BinTreeNavigator<T2> binTreeNav) {
	    this.binTreeNav = binTreeNav;
	    if(root != null) {
		fillStack(root);
	    }
	}

	private static Integer ZERO = new Integer(0);
	private static Integer ONE  = new Integer(1);
	
	private final BinTreeNavigator<T2> binTreeNav;

	/** The top of the stack contains the next node to return.
            The rest of the stack contain all nodes that appear on the
            path from the next node to return to the root - these
            nodes will be returned later.  The second component of
            each pair becomes 1 after the left subtree has been
            completely exhausted.  When the right subtree has been
            exhausted and the node is again at the top of the stack,
            the "1" tells us that both subtrees have been exhausted
            and we can hence return the node itself. 

	    Note: normally it is possible to get away without the
	    second pair component.  Still, we wanted to support
	    pseudo-trees where a node can have the same left and right
	    sons.  */
	private final LinkedList<Pair<T2,Integer>> stack = new LinkedList<Pair<T2,Integer>>();
	
	// Go down-left as much as possible from node, until we find
	// the next node to return from the subtree rooted in node.
	// All the nodes of the patg between node and next are put in
	// the stack, with the appropriate label: ZERO/ONE.
	private void fillStack(T2 node) {	    
	    while(node != null) {
		T2 succ = binTreeNav.left(node);
		if(succ != null) {
		    stack.addFirst(new Pair<T2,Integer>(node, ZERO));
		    node = succ;
		}
		else {
		    stack.addFirst(new Pair<T2,Integer>(node, ONE));
		    node = binTreeNav.right(node);
		}
	    }
	}

	public boolean hasNext() { return !stack.isEmpty(); }

	public void remove() { throw new UnsupportedOperationException(); }

	public T next() {
	    // we will return the top of the stack
	    T2 resNode = stack.removeFirst().left;
	    
	    if(!stack.isEmpty()) {
		Pair<T2,Integer> frame = stack.getFirst();
		if(frame.right == ZERO) {
		    // only the left subtree of frame.left has been explored
		    stack.removeFirst();
		    stack.addFirst(new Pair<T2,Integer>(frame.left, ONE));
		    // the next node will be from the right subtree of frame.left ...
		    fillStack(binTreeNav.right(frame.left));
		}
	    }

	    return resNode;
	}
    }

}
