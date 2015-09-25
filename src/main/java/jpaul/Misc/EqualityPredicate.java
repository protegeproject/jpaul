// EqualityPredicate.java, created Tue Feb 21 22:39:25 2006 by salcianu
// Copyright (C) 2006 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

import jpaul.DataStructs.DSUtil;

/**
 * <code>EqualityPredicate</code> is a simple predicate that checks
 * equality with a reference element.  Equality is checked by using
 * <code>.equals</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: EqualityPredicate.java,v 1.4 2006/03/14 02:29:31 salcianu Exp $ */
public class EqualityPredicate<T> extends Predicate<T> {
    
    /** Creates a <code>EqualityPredicate</code>. 

	@param referenceElem The element we check for equality
	against.  The newly created predicate will be
	<code>true</code> only for the elements that are equal with
	<code>referenceElem</code>.  We assume that only
	<code>null</code> is equal to <code>null</code>. */
    public EqualityPredicate(T referenceElem) {
	this.referenceElem = referenceElem;
    }

    private final T referenceElem;

    public boolean check(T elem) {
	return DSUtil.checkEq(referenceElem, elem);
    }
    
}
