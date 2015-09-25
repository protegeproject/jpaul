// FilterConstraint.java, created Tue Aug 16 10:29:12 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Arrays;

import jpaul.Constraints.Constraint;
import jpaul.Constraints.SolAccessor;
import jpaul.Misc.Predicate;

/**
   <code>FilterConstraint</code> models a filtering constraint.
   Mathematically, such a constraint has the form:

   <blockquote>
   <code>
   vIn | pred &lt;= vDest
   </code>
   </blockquote>

   where <code>/\</code> stands for set intersection,
   <code>&lt;=</code> stands for set inclusion, and <code>vIn</code>,
   <code>vDest</code> are set-valued variables, and <code>pred</code>
   is a predicate.  The constraints states that all elements of
   <code>vIn</code> that satisfy the predicate <code>pred</code>
   should appear in <code>vDest</code>.

   <p><b>Note:</b> <code>pred</code> should be a constant predicate
   (it should just be a way of specifying a constant set).  Things may
   get really wild if the predicate changes while the system is
   solved.
   
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: FilterConstraint.java,v 1.6 2006/03/14 02:29:30 salcianu Exp $ */
public class FilterConstraint<T> extends Constraint<SVar<T>,Set<T>> {
    
    /** Creates a <code>FilterConstraint</code> with the meaning
	<code>vIn | pred &lt;= vDest</code>.  */
    public FilterConstraint(SVar<T> vIn, Predicate<T> pred, SVar<T> vDest) {
	this.vIn   = vIn;
	this.pred   = pred;
	this.vDest = vDest;
        this.in  = Arrays.asList(vIn);
	this.out = Arrays.asList(vDest);
    }

    protected final SVar<T> vIn;
    protected final Predicate<T> pred;
    protected final SVar<T> vDest;
    
    private final Collection<SVar<T>> in;
    private final Collection<SVar<T>> out;
	
    public Collection<SVar<T>> in()  { return this.in;  }
    public Collection<SVar<T>> out() { return this.out; }

    /** Returns {@link jpaul.Constraints.Constraint#HIGH_COST
        HIGH_COST}. */
    public int cost() { return Constraint.HIGH_COST; }

    public void action(SolAccessor<SVar<T>,Set<T>> sa) {
	Set<T> sIn = sa.get(vIn);
	if(sIn == null) return;

	// TODO: "new LinkedHashSet<T>" is correct, but we should try to use
	// the same set factory that generated sIn1 and sIn2.
	Set<T> result = new LinkedHashSet<T>();

	for(T elem : sIn) {
	    if(pred.check(elem)) {
		result.add(elem);
	    }
	}

	// update the destination variable
	sa.join(vDest, result);
    }
    
    public String toString() {
	return "setfilter: " + vIn + " | " + pred + " <= " + vDest;
    }

    // No re-implem. for rewrite, equals, and hashCode.    
}
