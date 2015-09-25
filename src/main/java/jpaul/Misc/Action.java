// Action.java, created Mon Feb 14 20:39:19 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>Action</code> is a wrapper for a <code>void</code> returning
 * method.  OOP is great, especially when we use it to encode
 * functional programming features such as higher-order functions :)
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Action.java,v 1.2 2005/08/12 23:16:49 salcianu Exp $ */
public interface Action<T> {
    
    /** Perform some action on <code>t</code>.  As this method does
        not return anything, the only way the action produces
        something is through side-effects; that is why we call it an
        action. */
    public void action(T t);
    
}
