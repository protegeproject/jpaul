// SetConstraints.java, created Mon Jun 20 19:01:51 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Collection;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import jpaul.Constraints.Constraint;
import jpaul.Constraints.LtConstraint;
import jpaul.Constraints.CtConstraint;

/**
 * <code>SetConstraints</code> is a collection of set constraints.  It
 * contains methods to quickly add simple constraints.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SetConstraints.java,v 1.8 2006/01/29 16:05:29 adam_kiezun Exp $ */
public class SetConstraints<T> extends LinkedList<Constraint<SVar<T>, Set<T>>> {
    
	private static final long serialVersionUID = -6025695122038395523L;

	/** Adds an inclusion constraints between two sets.  */
    public void addInclusion(SVar<T> s1, SVar<T> s2) {
	this.add(new LtConstraint<SVar<T>,Set<T>>(s1, s2));
    }

    /** Adds an constraint of the form &quot;constant set
        <code>ct</code> is included in the set <code>s</code>&quot;. */
    public void addCtSource(Collection<T> ct, SVar<T> s) {
	this.add(new CtConstraint<SVar<T>,Set<T>>(new LinkedHashSet<T>(ct), s));
    }

    /** Adds two mutual inclusion constraints to reflect the fact that
        the sets <code>s1</code> and <code>s2</code> should be
        equal. */
    public void addEquality(SVar<T> s1, SVar<T> s2) {
	this.addInclusion(s1, s2);
	this.addInclusion(s2, s1);
    }
    
}
