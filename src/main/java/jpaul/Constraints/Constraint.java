// Constraint.java, created Mon Mar  7 15:01:02 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

import java.util.Collection;
import java.util.Comparator;

import jpaul.DataStructs.UnionFind;

/**
   <code>Constraint</code> models a constraint between several
   variables.  Mathematically, a constraint has the form:
   
   <blockquote>
   <code>
     <i>f</i>(&lt;vi_1,vi_2,...,vi_m&gt;)  &lt;=  &lt;vo_1,vo_2,...,vo_n&gt;
   </code>
   </blockquote>

   where <code>vi_1<code>, <code>vi_2</code>, ..., <code>vi_m</code>
   are the <i>input</i> variables of the constraints,
   <code>vo_1<code>, <code>vo_2</code>, ..., <code>vo_n</code> are the
   <i>output</i> variables, and <i>f</i> is a computable function.

   <p>
   We solve a constraint system by repeatedly applying the
   constraints until we obtain a fixed point.  Hence, we use an
   operational interpretation of a constraint: a constraint reads the
   values of the <i>input</i> variables (see method {@link #in}) and next
   computes and joins some <i>delta</i>s to the values of the <i>output</i>
   variables (see method {@link #out}); the sets of in- and out-variables are not
   necessarily disjoint.  Notice that a constraint cannot decrease the
   value attached to any variable; hence, the values of <b>all</b> variables
   increase, and (if the domains have finite height), the fixed-point solver
   will terminate (Note: this is a solution according to our
   operational view of the constraints; hopefully, this is identical
   to your intended constrain meaning).

   <p>
   The <i>cost</i> of a constraint is supposed to be a very rough
   approximation of the constraint computation cost (see {@link
   #cost}).  This measure is used by the constraint {@link
   jpaul.Constraints.ConstraintSystem solver} in order to increase its
   speed.

   <p>
   To define new constraints, you have to subclass this class and
   provide implementation for its abstract methods.  Here is a sample
   new constraint: simplified <a
   href="SetConstraints/doc-files/IntersectConstraint.java">SetConstraints.IntersectConstraint</a>.


   @see CtConstraint
   @see LtConstraint


 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Constraint.java,v 1.14 2006/01/31 00:42:56 adam_kiezun Exp $ */
public abstract class Constraint<V extends Var<Info>, Info> {

    /** @return List of variables whose values may be read in order to
        construct the values of the outputs. */
    public abstract Collection<V> in();

    /** @return List of variables whose values may be affected by this
        constraint. */
    public abstract Collection<V> out();


    /** Performs the action attached to this constraint. 
	The action should have the following form:
	<ol>

	<li>Read the values of several in-variables using
	<code>sa.</code>{@link jpaul.Constraints.SolReader#get get}.

	<li>Compute some values.

	<li>Use <code>sa.</code>{@link
	jpaul.Constraints.SolAccessor#join join} to join the computed
	values to several out-variables.
	
	</ol>

	<p>
	<b>Note1:</b> It is a <b>serious</b> mistake to write an
	<code>action</code> method that reads/modifies variables that
	are not listed in the collections returned by
	<code>in()</code>/<code>out()</code>.  If you ever suspect
	such an error, please set {@link
	ConstraintSystem#CHECK_IN_OUT} to <code>true</code>.  

	<p>
	<b>Note2:</b> The solver initializes each variable to
	<code>null</code> (<code>null</code> is considered equivalent
	to the bottom element of the correspoding lattice).  The body
	of <code>action</code> should be prepared to receive a
	<code>null</code> result from <code>sa.get</code>.

	@param sa Provides access to the values of the variables that
	are read/modified.  */
    public abstract void action(SolAccessor<V,Info> sa);


    /** Rewrites <code>this</code> constraint by replacing each
	variable with the representative of its equivalence class.
	The real implementation of this method is optional: the
	default implementation returns <code>this</code> constraint,
	unmodified.  This is safe: the constraint writer does not need
	to be aware of variable unification, as the
	<code>SolAccessor</code> passed by the constraint solver
	already deals with it.

	<p>Implementing <code>rewrite</code> may be useful when
	unification causes several constraints to become identical:
	e.g., consider <code>v1 &lt;= v2</code> and <code>v3 &lt;=
	v4</code>, after we unify <code>v1</code> with <code>v3</code>
	and <code>v2</code> with <code>v4</code>.  Implementing
	<code>rewrite</code>, <code>equals</code> (and
	<code>hashCode</code>) allows the solver to avoid working with
	several identical constraints.

	@param uf Union-find structure; for each variable
	<code>v</code>, <code>uf.find(v)</code> is the representative
	of its equivalence class. */
    public Constraint<V,Info> rewrite(UnionFind<V> uf) {
	return this;
    }


    /** Returns a rough estimate of the evaluation cost of
        <code>this</code> constraint.  This cost has only a relative
        meaning: e.g., a constraint is more/less costly than another.

	A constraint solver may choose to iterate first over the cheap
	constraints, and only next iterate over the other, more
	expensive constraints.

	The cost should influence only the speed, not the correctness:
	the solution of a system of constraints should satisfy all
	constraints, regardless of their cost.  

	<p>By default, it returns {@link #AVG_COST AVG_COST}. */
    public int cost() { return AVG_COST; }
    
    /** Possible constraint cost; see {@link #cost} */
    public static final int VERY_LOW_COST  = 100;
    /** Possible constraint cost; see {@link #cost} */
    public static final int LOW_COST       = 200;
    /** Possible constraint cost; see {@link #cost} */
    public static final int AVG_COST       = 300;
    /** Possible constraint cost; see {@link #cost} */
    public static final int HIGH_COST      = 400;

    /** Cost-based constraint comparator.  It enables the use of a
        priority queue for the yet-unsolved constraints in the
        fixed-point solver. */
    static final class CostComparator<V extends Var<Info>, Info> implements Comparator<Constraint<V,Info>> {
	public int compare(Constraint<V,Info> c1, Constraint<V,Info> c2) {
	    int cost1 = c1.cost();
	    int cost2 = c2.cost();
	    if(cost1 < cost2) return -1;
	    if(cost1 > cost2) return +1;
	    return 0;
	}
    }

}
