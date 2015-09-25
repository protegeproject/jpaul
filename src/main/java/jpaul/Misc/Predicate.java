// Predicate.java, created Thu Feb 24 15:56:13 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>Predicate</code> wraps a boolean predicate.  A predicate is
 * supposed to be pure, i.e., no side-effects.  In those exceptional
 * cases that require side-effects, the programmer should either use
 * {@link ActionPredicate}, or, at the very least, explicitly document
 * the side-effects.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Predicate.java,v 1.8 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class Predicate<T> {

    /** The boolean predicate. */
    public abstract boolean check(T obj);


    /** Returns an always-true predicate. */
    public static <T> Predicate<T> TRUE() {
	return new Predicate<T>() {
	    public boolean check(T obj) { return true; }
	};
    }


    /** Returns an always-false predicate. */
    public static <T> Predicate<T> FALSE() {
	return Predicate.<T>NOT(Predicate.<T>TRUE());
    }


    /** Predicate negation.

	@return A predicate that is true iff <code>pred</code> is
        false. */
    public static <T> Predicate<T> NOT(final Predicate<T> pred) {
	return new Predicate<T>() {
	    public boolean check(T obj) {
		return !pred.check(obj);
	    }
	};
    }

    /** Short-circuited AND operation. 

	@return A predicate that is true iff both <code>a</code> and
	<code>b</code> are true.  Evaluation stops as soon as the
	final result is known.  */
    public static <T> Predicate<T> AND(final Predicate<T> a, final Predicate<T> b) {
	return new Predicate<T>() {
	    public boolean check(T obj) {
		return a.check(obj) && b.check(obj);
	    }
	};
    }

    /** Short-circuited OR operation. 

	@return A predicate that is false iff both <code>a</code> and
	<code>b</code> are false.  Evaluation stops as soon as the
	final result is known.  */
    public static <T> Predicate<T> OR(final Predicate<T> a, final Predicate<T> b) {
	return new Predicate<T>() {
	    public boolean check(T obj) {
		return a.check(obj) || b.check(obj);
	    }
	};
    }

    /** Complete AND operation.  Similar to {@link #AND} but no
        short-circuit: in all situations, <code>a</code> is evaluated
        and next <code>b</code> is evaluated.  Good for impure
        predicates.  */
    public static <T> Predicate<T> FULL_AND(final Predicate<T> a, final Predicate<T> b) {
	return new Predicate<T>() {
	    public boolean check(T obj) {
		return a.check(obj) & b.check(obj);
	    }
	};
    }

    /** Complete OR operation.  Similar to {@link #OR} but no
        short-circuit: in all situations, <code>a</code> is evaluated
        and next <code>b</code> is evaluated.  Good for impure
        predicates.  */
    public static <T> Predicate<T> FULL_OR(final Predicate<T> a, final Predicate<T> b) {
	return new Predicate<T>() {
	    public boolean check(T obj) {
		return a.check(obj) | b.check(obj);
	    }
	};
    }

}
