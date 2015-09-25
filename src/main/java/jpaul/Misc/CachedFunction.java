// Function.java, created Tue Feb 22 09:00:19 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>CachedFunction</code> is a caching wrapper around a (presumably pure) function.
 *
 * @author Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: CachedFunction.java,v 1.6 2006/03/14 02:29:31 salcianu Exp $
 */
public class CachedFunction<TArg,TRes> extends Function<TArg,TRes> {

    /** Constructs a caching wrapper around the function
        <code>func</code>.  <code>func</code> should be
        (observationally) pure (we have no way of checking this, so
        impure functions are likely to result in hard to find
        errors).*/
    public CachedFunction(Function<TArg,TRes> func) {
	this.func = func;
    }

    private final Function<TArg,TRes> func;
    // no need to use a LinkedHashMap here: there is no way to iterate
    // over the domain of a cached function.
    private final Map<TArg,TRes> cache = new HashMap<TArg,TRes>();

    public TRes f(TArg arg) {
	TRes res = this.cache.get(arg);
	if(res == null) {
	    res = this.func.f(arg);
	    this.cache.put(arg,res);
	}
	return res;
    }
    
}
