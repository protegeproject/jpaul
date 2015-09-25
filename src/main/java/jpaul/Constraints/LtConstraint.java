// LtConstraint.java, created Sat Jun 18 17:24:38 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

import java.util.Collections;
import java.util.Collection;

import jpaul.DataStructs.UnionFind;

/**
 * <code>LtConstraint</code> models a &quot;less than&quot;
 * constraint: the value of variable <code>vs</code> is smaller than
 * the value of variable <code>vd</code>, according to the order
 * relation from the corresponding lattice.  This order is implicitly
 * given by the {@link Var#join join} operation of the two variables.
 * The two variables must represent information from the same lattice
 * (i.e., the same {@link Var#join join} operation)
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: LtConstraint.java,v 1.10 2006/03/14 02:29:29 salcianu Exp $ */
public final class LtConstraint<V extends Var<Info>,Info> extends Constraint<V, Info> {
    
    /** Creates a <code>LtConstraint</code> with default cost {@link
        jpaul.Constraints.Constraint#LOW_COST}.  */
    public LtConstraint(V vs, V vd) {
	this(vs, vd, Constraint.LOW_COST);
    }

    /** Creates an <code>LtConstraint</code> with the meaning
        &quot;value of variable <code>vs</code> is smaller than the
        value of the variable <code>vd</code>.&quot;

	@param cost Relative cost of this constraint. */
    public LtConstraint(V vs, V vd, int cost) {
        this.vs = vs;
	this.vd = vd;
	this.cost = cost;
	this.in  = Collections.<V>singleton(vs);
	this.out = Collections.<V>singleton(vd);
    }
    
    private final int cost;
    public int cost() { return this.cost; }

    public final V vs;
    public final V vd;

    private final Collection<V> in;
    private final Collection<V> out;
    
    public Collection<V> in()  { return this.in; }
    public Collection<V> out() { return this.out; }

    public void action(SolAccessor<V,Info> sa) {
	// get the value of vs and join it to the value of vd
	sa.join(vd, sa.get(vs));
    }

    public Constraint<V,Info> rewrite(UnionFind<V> uf) {
	V vs2 = uf.find(vs);
	V vd2 = uf.find(vd);
	// remove superfluous constraints
	if(vs2.equals(vd2)) return null;
	// nothing changed, so why generate a new constraint ?
	if(vs.equals(vs2) && vd.equals(vd2)) return this;
	return new LtConstraint<V,Info>(vs2, vd2);
    }

    private int hashCode = 0;
    public int hashCode() {
	if(hashCode == 0) {
	    hashCode = 13*vs.hashCode() + 15*vd.hashCode() + 37;
	}
	return hashCode;
    }

    public boolean equals(Object o) {
	if((o == null) || !(o instanceof LtConstraint/*<V,Info>*/))
	    return false;
	@SuppressWarnings("unchecked")
	LtConstraint<V,Info> lt2 = (LtConstraint<V,Info>) o;
	return (this.vs == lt2.vs) && (this.vd == lt2.vd);
    }

    public String toString() {
	return vs + " <= " + vd;
    }

}
