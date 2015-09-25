// CtConstraint.java, created Mon Jun 20 18:28:02 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

import java.util.Collections;
import java.util.Collection;

import jpaul.DataStructs.UnionFind;

/**
 * <code>CtConstraint</code> models a constraint of the form
 * &quot;constant <code>ct</code> is less than the value of the
 * variable <code>vd</code>&quot;.  This order is implicitly given by
 * the {@link Var#join join} operation of the variable
 * <code>vd</code>.  The constant <code>ct</code> must have the same
 * type as the values of <code>vd</code>. 
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: CtConstraint.java,v 1.10 2006/03/14 02:29:28 salcianu Exp $ */
public final class CtConstraint<V extends Var<Info>, Info> extends Constraint<V, Info> {

    /** Creates a <code>LtConstraint</code> with default cost {@link
        jpaul.Constraints.Constraint#VERY_LOW_COST}.  */
    public CtConstraint(Info ct, V vd) {
	this(ct, vd, Constraint.VERY_LOW_COST);
    }

    /** Creates a <code>CtConstraint</code> with the meaning &quot;the
        constant <code>ct</code> is smaller than the value of the
        variable <code>vd</code>.&quot;

	@param cost Relative cost of this constraint. */
    public CtConstraint(Info ct, V vd, int cost) {
	this.ct   = ct;
	this.vd   = vd;
	this.cost = cost;
	this.in   = Collections.<V>emptySet();
	this.out  = Collections.<V>singleton(vd);
    }
    
    private final int cost;
    public int cost() { return this.cost; }

    public final Info ct;
    public final V vd;

    private final Collection<V> in;
    private final Collection<V> out;
    
    public Collection<V> in()  { return this.in; }
    public Collection<V> out() { return this.out; }

    public void action(SolAccessor<V,Info> sa) {
	// get the value of vs and join it to the value of vd
	sa.join(vd, ct);
    }

    public Constraint<V,Info> rewrite(UnionFind<V> uf) {
	V vd2 = uf.find(vd);
	// nothing changed, so why generate a new constraint ?
	if(vd.equals(vd2)) return this;
	return new CtConstraint<V,Info>(ct, vd2);
    }

    private int hashCode = 0;
    public int hashCode() {
	if(hashCode == 0) {
	    hashCode = 15*vd.hashCode() + 23;
	}
	return hashCode;
    }

    public boolean equals(Object o) {
	if((o == null) || !(o instanceof CtConstraint/*<V,Info>*/))
	    return false;
	@SuppressWarnings("unchecked")
	CtConstraint<V,Info> ct2 = (CtConstraint<V,Info>) o;
	return this.ct.equals(ct2.ct) && this.vd.equals(ct2.vd);
    }

    public String toString() {
	return ct + " <= " + vd;
    }
    
}
