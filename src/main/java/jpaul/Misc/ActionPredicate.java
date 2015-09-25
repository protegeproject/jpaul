// ActionPredicate.java, created Wed Dec 21 07:14:42 2005 by salcianu
// Copyright (C) 2005 Alex Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>ActionPredicate</code> is a combination of an
 * <code>Action</code> and a <code>Predicate</code>.  Intuitively, one
 * may say that <code>ActionPredicate</code> is an impure predicate,
 * but one should be aware of the fact that
 * <code>ActionPredicate</code> and <code>Predicate</code> are not iin
 * any subclassing relationship (in general, it is a bad software
 * engineering practice to have subclassing between pure and impure
 * varieties of the same concept).  Still, we provide a static method
 * ({@link #fromPredicate}) that converts a <code>Predicate</code>
 * into an <code>ActionPredicate</code>.
 *
 * @see Action
 * @see Predicate
 * 
 * @author  Alex Salcianu - salcianu@alum.mit.edu
 * @version $Id: ActionPredicate.java,v 1.4 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class ActionPredicate<T> implements Action<T> {
    
    /** A boolean predicate WITH possible side-effects. */
    public abstract boolean actionPredicate(T obj);


    /** Action: executes {@link #actionPredicate} only for its
        side-effects, ignoring its result. */
    public final void action(T obj) {
	this.actionPredicate(obj);
    }


    /** Creates an <code>ActionPredicate</code> whose
        <code>actionPredicate</code> method first invokes the action
        from <code>action</code> and next returns the constant boolean
        value <code>predicateResult</code>.  */
    public static <T> ActionPredicate<T> fromAction
	(final Action<T> action,
	 final boolean predicateResult) {
	
	return new ActionPredicate<T>() {
	    public boolean actionPredicate(T obj) {
		action.action(obj);
		return predicateResult;
	    }
	};
    }
    

    /** Creates an <code>ActionPredicate</code> wrapper around a
        <code>Predicate</code>: it does not perform any side-effect,
        it just returns the boolean value of teh predicate. */
    public static <T> ActionPredicate<T> fromPredicate(final Predicate<T> predicate) {
	return new ActionPredicate<T>() {
	    public boolean actionPredicate(T obj) {
		return predicate.check(obj);
	    }
	};
    }

}
