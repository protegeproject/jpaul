// WorkSet.java, created Tue Feb 22 09:17:39 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;

/**
 * <code>WorkSet</code> is an ordered set-like data structure.  In the
 * current implementations, the order the elements are extracted from
 * a <code>WorkSet</code> has some relation to the order the elements
 * were inserted or to the elements' priorities.  This data structure
 * is useful for fixed point computations.  In all the current
 * <code>WorkSet</code> implementations the
 * <code>add</code>/<code>extract</code> operations have O(1)
 * complexity, except <code>WorkPriorityQueue</code> where they are
 * logarithmic.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkSet.java,v 1.8 2005/08/11 18:01:11 salcianu Exp $ */
public interface WorkSet<T> {

    /** Adds the element <code>elem</code> to <code>this</code>
        workset.

	@return <code>true</code> if <code>elem</code> was not already
        in the workset.  If <code>elem</code> was already in the
        workset, the workset does not change in any way, and
        <code>add</code> returns <code>false</code>.  */
    public boolean add(T elem);


    /** Adds all elements from <code>elems</code> to <code>this</code>
        workset.

	@return <code>true</code> if any of the added elements was not
	already in <code>this</code> workset.  Otherwise, the workset
	does not change in any way, and <code>add</code> returns
	<code>false</code>.  */
    public boolean addAll(Collection<T> elems);


    /** Returns the first element of <code>this</code> workset
        (according to the order specific to <code>this</code> workset.
        The element is removed from the workset.  Throws a {@link
        java.util.NoSuchElementException NoSuchElementException} if
        the workset is empty.

	@return The first element from the workset.
    */
    public T extract();


    /** Removes all elements from the workset.<br> Complexity:
        O(1). */
    public void clear();


    /** Checks whether <code>this</code> workset is empty.<br>
        Complexity: O(1). */
    public boolean isEmpty();


    /** Checks whether <code>this</code> workset contains the element
        <code>e</code>.<br>  Complexity: O(1). */
    public boolean contains(T e);


    /** Returns the size of <code>this</code> workset.<br>
	Invariant: <code>isEmpty()</code> iff <code>size() ==
	0</code>.<br>  Complexity: O(1). */
    public int size();

}
