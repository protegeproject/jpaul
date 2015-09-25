// SVar.java, created Mon Jun 20 14:34:17 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Set;

import jpaul.DataStructs.SetFactory;
import jpaul.DataStructs.SetFacts;

import jpaul.Constraints.Var;

/**
 * <code>SVar</code> is a variable whose values are sets of <code>T</code>s.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SVar.java,v 1.7 2006/03/14 02:29:30 salcianu Exp $
 */
public class SVar<T> extends Var<Set<T>> {
    
    /** Creates an <code>SVar</code>.

	@param setFact Set factory used internally by the {@link #copy
        copy} method.  For a set constraint systems with a few large
        sets, a <code>HashSet</code> factory ({@link
        jpaul.DataStructs.SetFacts#hash SetFacts.hash}) is a safe
        choice; in other cases, you may gain significant speed by
        using more appropriate set factories. */
    public SVar(SetFactory<T> setFact) {
	this.setFact = setFact;
    }

    private final SetFactory<T> setFact;

    /** Creates an <code>SVar</code> using a <code>HashSet</code>
        factory ({@link jpaul.DataStructs.SetFacts#hash
        SetFacts.hash})

	@see #SVar(SetFactory) */
    public SVar() {
	this(SetFacts.<T>hash());
    }

    public String toString() { return "S" + id; }
    
    /** Returns a set that contains the same elements as
        <code>s</code>.  Constructs the new set using the set factory
        passed to the constructor. */
    public Set<T> copy(Set<T> s) {
	return setFact.create(s);
    }

    /** Adds all the elements from <code>s2</code> to
        <code>s1</code>. */
    public boolean join(Set<T> s1, Set<T> s2) {
	return s1.addAll(s2);
    }
}
