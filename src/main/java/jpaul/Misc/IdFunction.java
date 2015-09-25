// IdFunction.java, created Tue Jul 12 07:25:03 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>IdFunction</code> - identity function
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: IdFunction.java,v 1.4 2006/03/14 02:29:31 salcianu Exp $
 */
public class IdFunction<T> extends Function<T,T> {
    public T f(T x) { return x; }
}
