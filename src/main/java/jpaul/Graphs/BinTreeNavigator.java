// BinTreeNavigator.java, created Wed Aug 17 13:28:56 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.ArrayList;

/**
 * <code>BinTreeNavigator</code> models the arcs of a binary tree.  We model
 * a binary tree by using our favorite trick: an external navigator.
 * This way, we can treat as trees even structures that do not store
 * explicitly the left and right successors (e.g., they can be given
 * by a separate map or can even be computed from scratch).
 *
 * <p>A binary tree is a special case of directed graph; so, a
 * <code>BinTreeNavigator</code> is a special case of a
 * <code>ForwardNavigator</code>.
 *
 * <p>Normally, a tree is a directed graph where all vertices are
 * reachable from a <i>tree root</i>, with no sharing and no cycles.
 * Still, for space reasons, it is sometimes important to use trees
 * with sharing (e.g., the left and the right operands of an
 * arithmetic expression may be identical).  The tree utilities that
 * support trees with sharing should document this explicitly.
 * Clearly, a tree should have <b>no cycles</b> whatsoever (many
 * algorithms would not terminate in that case).
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BinTreeNavigator.java,v 1.2 2006/01/29 16:05:29 adam_kiezun Exp $ */
public abstract class BinTreeNavigator<T> implements ForwardNavigator<T> {
    
    /** Returns the left son of <code>node</code>, if any, or
        <code>null</code> otherwise. */
    public abstract T left(T node);
    
    /** Returns the right son of <code>node</code>, if any, or
        <code>null</code> otherwise. */
    public abstract T right(T node);

    /** Returns all (0, 1 or 2) sons of a node. */
    public List<T> next(T node) {
	List<T> sons = new ArrayList<T>();
	if(left(node) != null) sons.add(left(node));
	if(right(node) != null) sons.add(right(node));
	return sons;
    }
    
}
