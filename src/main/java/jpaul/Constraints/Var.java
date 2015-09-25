// Var.java, created Mon Mar  7 14:59:23 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

/**
 * <code>Var</code> models a variable from a system of constraints.  A
 * system of constraints may involve variables that take values in
 * different lattices (all of them subclasses of <code>Info</code>).
 * Hence, the value lattice is an attribute of each variable.
 * Ideally, we would encapsulate the operations of each lattice into
 * an object that is pointed to from relevant variables.  Instead, for
 * simplicity, we have the lattice-related operations ({@link #copy}
 * and {@link #join}) as methods of each variable.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Var.java,v 1.5 2006/03/14 02:29:29 salcianu Exp $ */
public abstract class Var<Info> {

    // counter used to generate unique ids (for debug)
    private static int counter;
    // thread-safe way of getting a unique id!
    private static synchronized int get_id() { return counter++; }
    
    protected int id = get_id();
    

    /** String representation of <code>this</code> variable.  The
	default implementation returns &quot;V<i>id</i>&quot; where
	<i>id</i> is a unique integer id. */
    public String toString() { return "V" + id; }


    /** Returns a clone of the information <code>x</code>.  Mutation
        on the returned object should not affect the original
        information <code>x</code>.  The constraint solver will invoke
        this method only with non-null values for <code>x</code>.  */
    public abstract Info copy(Info x);


    /** Joins two values from the domain <code>this</code> variable
        takes values from.  This mutate mutates parameter
        <code>x</code> by joining to it the parameter <code>y</code>.
        It does not mutate <code>y</code>.  The constraint solver will
        invoke this method only with non-null values for
        <code>x</code> and <code>y</code>.

	@return <code>true</code> iff the <code>x</code> changed after
	<code>y</code> was joined to it.

	A <code>false</code> result means that <code>x</code> was
	already bigger than <code>y</code>.  Hence, this operation
	implictly defines the order relation in the lattice where this
	variable takes values.  */
    public abstract boolean join(Info x, Info y);

}
