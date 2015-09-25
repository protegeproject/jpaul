// Function.java, created Mon Feb 14 21:16:34 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>Function</code> is a wrapper around a function that takes a
 * <code>TArg</code> and returns a <code>TRes</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Function.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class Function<TArg,TRes> {
    
    /** Takes a <code>TRes</code> and returns a TArg.  The real
        function! */
    public abstract TRes f(TArg arg);


    /** Computes the composition of two functions. */
    public static <T1,T2,T3> Function<T1,T3> comp(final Function<T2,T3> func1, final Function<T1,T2> func2) {
	return new Function<T1,T3>() {
	    public T3 f(T1 x) {
		return func1.f(func2.f(x));
	    }
	};
    }
    
}
