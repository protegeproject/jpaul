// IntersectConstraint.java, created Tue Aug 16 10:29:12 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Arrays;

import jpaul.Constraints.Constraint;
import jpaul.Constraints.SolAccessor;
import jpaul.DataStructs.UnionFind;

/**
   <code>IntersectConstraint</code> models a set intersection constraint.
   Mathematically, such a constraint has the form:

   <blockquote>
   <code>
   vIn1 /\ vIn2 &lt;= vDest
   </code>
   </blockquote>

   where <code>/\</code> stands for set intersection,
   <code>&lt;=</code> stands for set inclusion, and <code>vIn1</code>,
   <code>vIn2</code>, <code>vDest</code> are set-valued variables.
   
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: IntersectConstraint.java,v 1.6 2006/03/14 02:29:30 salcianu Exp $ */
public class IntersectConstraint<T> extends Constraint<SVar<T>,Set<T>> {
    
    /** Creates a <code>IntersectConstraint</code> with the meaning
	<code>vIn1 /\ vIn2 &lt;= vDest</code>.  */
    public IntersectConstraint(SVar<T> vIn1, SVar<T> vIn2, SVar<T> vDest) {
	this.vIn1  = vIn1;
	this.vIn2  = vIn2;
	this.vDest = vDest;
        this.in  = Arrays.asList(vIn1, vIn2);
	this.out = Arrays.asList(vDest);
    }

    private final SVar<T> vIn1;
    private final SVar<T> vIn2;
    private final SVar<T> vDest;
    
    private final Collection<SVar<T>> in;
    private final Collection<SVar<T>> out;
	
    public Collection<SVar<T>> in()  { return this.in;  }
    public Collection<SVar<T>> out() { return this.out; }

    /** Returns {@link jpaul.Constraints.Constraint#HIGH_COST
        HIGH_COST}. */
    public int cost() { return Constraint.HIGH_COST; }


    public void action(SolAccessor<SVar<T>,Set<T>> sa) {
	Set<T> s_in1 = sa.get(vIn1);
	if(s_in1 == null) return;
	Set<T> s_in2 = sa.get(vIn2);
	if(s_in2 == null) return;

	if(s_in1.size() < s_in2.size()) {
	    Set<T> temp = s_in1;
	    s_in1 = s_in2;
	    s_in2 = temp;
	}

	// TODO: "new LinkedHashSet<T>" is correct, but we should try to use
	// the same set factory that generated s_in1 and s_in2.
	Set<T> intersect = new LinkedHashSet<T>();

	for(T elem : s_in1) {
	    if(s_in2.contains(elem)) {
		intersect.add(elem);
	    }
	}

	// update the destination variable
	sa.join(vDest, intersect);
    }

    /** We implemented {@link #rewrite}, {@link #equals}, and {@link
        #hashCode}, such that constraints that are identical after
        variable unification are not duplicated needlessly. */
    public Constraint<SVar<T>,Set<T>> rewrite(UnionFind<SVar<T>> uf) {
	SVar<T> vIn1_p  = uf.find(vIn1);
	SVar<T> vIn2_p  = uf.find(vIn2);
	SVar<T> vDest_p = uf.find(vDest);	
	return new IntersectConstraint<T>(vIn1_p, vIn2_p, vDest_p);
    }

    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof IntersectConstraint/*<T>*/)) return false;
	if(this.hashCode() != o.hashCode()) return false;

	@SuppressWarnings("unchecked")
	IntersectConstraint<T> ic2 = (IntersectConstraint<T>) o;
	return 
	    this.vIn1.equals(ic2.vIn1) &&
	    this.vIn2.equals(ic2.vIn2) &&
	    this.vDest.equals(ic2.vDest);
    }


    public int hashCode() {
	if(hashCode == 0) {
	    hashCode = 3 * vIn1.hashCode() + 5 * vIn2.hashCode() + 7 * vDest.hashCode();
	}
	return hashCode;
    }
    private int hashCode = 0; // cached hashCode


    public String toString() {
	return vIn1 + " /\\ " + vIn2 + " <= " + vDest;
    }
    
}
