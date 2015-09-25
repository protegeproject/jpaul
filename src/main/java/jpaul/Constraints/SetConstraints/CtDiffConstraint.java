// CtDiffConstraint.java, created Tue Aug 16 10:29:12 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Set;
import java.util.Collection;
import jpaul.Constraints.Constraint;
import jpaul.DataStructs.UnionFind;

import jpaul.Misc.Predicate;
import jpaul.Misc.SetMembership;

/**
   <code>CtDiffConstraint</code> models a &quot;difference with a constant set&quot; constraint.
   Mathematically, such a constraint has the form:

   <blockquote>
   <code>
   vIn \ ctSet &lt;= vDest
   </code>
   </blockquote>

   where <code>/\</code> stands for set intersection,
   <code>&lt;=</code> stands for set inclusion, <code>vIn</code> and
   <code>vDest</code> are set-valued variables, and <code>ctSet</code>
   is a constant set.
   
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: CtDiffConstraint.java,v 1.5 2006/03/14 02:29:30 salcianu Exp $ */
public class CtDiffConstraint<T> extends FilterConstraint<T> {
    
    /** Creates a <code>CtDiffConstraint</code> with the meaning
	<code>vIn \ ctSet &lt;= vDest</code>.  This constructor does
	not construct a private copy of <code>ctSet</code>, so you may
	want to pass it an exclusive copy of <code>ctSet</code>. */
    public CtDiffConstraint(SVar<T> vIn, Collection<T> ctSet, SVar<T> vDest) {
	super(vIn,
	      Predicate.NOT(new SetMembership<T>(ctSet)),
	      vDest);
	this.ctSet = ctSet;
    }

    private final Collection<T> ctSet;


    /** We implemented {@link #rewrite}, {@link #equals}, and {@link
        #hashCode}, such that constraints that are identical after
        variable unification are not duplicated needlessly. */
    public Constraint<SVar<T>,Set<T>> rewrite(UnionFind<SVar<T>> uf) {
	SVar<T> vIn_p   = uf.find(vIn);
	SVar<T> vDest_p = uf.find(vDest);	
	return new CtDiffConstraint<T>(vIn_p, ctSet, vDest_p);
    }

    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof CtDiffConstraint/*<T>*/)) return false;
	if(this.hashCode() != o.hashCode()) return false;
	
	@SuppressWarnings("unchecked")
	CtDiffConstraint<T> cd2 = (CtDiffConstraint<T>) o;
	return 
	    this.vIn.equals(cd2.vIn) &&
	    this.vDest.equals(cd2.vDest) &&
	    this.ctSet.equals(cd2.ctSet);
    }


    public int hashCode() {
	if(hashCode == 0) {
	    hashCode = 3 * vIn.hashCode() + 5 * ctSet.hashCode() + 7 * vDest.hashCode();
	}
	return hashCode;
    }
    private int hashCode = 0; // cached hashCode

    
    public String toString() {
	return vIn + " \\ " + ctSet + " <= " + vDest;
    }
    
}
