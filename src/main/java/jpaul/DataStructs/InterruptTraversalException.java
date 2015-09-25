// InterruptTraversalException.java, created Thu Dec  8 14:46:03 2005 by salcianu
// Copyright (C) 2005 Alex Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

/**
 * <code>InterruptTraversalException</code> is a special exception for
 * prematurely terminating the traversal of a data structure.  Some
 * data structures (including non-trivial ones, like
 * <code>jpaul.Graphs.DiGraph</code>) offer methods to traverse all
 * their elements (e.g., {@link
 * jpaul.DataStructs.Relation#forAllEntries Relation.forAllEntries},
 * {@link jpaul.Graphs.DiGraph#dfs DiGraph.dfs}).  As a general rule,
 * these methods take as arguments <code>Visitor</code>s (or similar
 * classes), and invoke their code on all relevant entries.  If a
 * visitor decides that the enclosing traversal should stop, it may
 * throw an <code>InterruptTraversalException</code>.
 * 
 * @author  Alex Salcianu - salcianu@alum.mit.edu
 * @version $Id: InterruptTraversalException.java,v 1.3 2006/01/29 16:05:28 adam_kiezun Exp $ */
public class InterruptTraversalException extends RuntimeException {
    
	private static final long serialVersionUID = 4928455482568559466L;

	/** Creates a <code>InterruptTraversalException</code>. */
    public InterruptTraversalException() { /* no code*/ }

    /** Creates a <code>InterruptTraversalException</code> with a
        certain message.  */
    public InterruptTraversalException(String message) {
	super(message);
    }
    
}
