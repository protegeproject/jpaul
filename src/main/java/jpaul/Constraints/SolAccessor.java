// SolAccessor.java, created Mon Mar  7 15:04:01 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

/**
 * <code>SolAccessor</code> provides access to the values of the
 * variables from a system of constraints.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SolAccessor.java,v 1.5 2005/08/11 22:57:08 salcianu Exp $ */
public interface SolAccessor<V extends Var<Info>, Info> extends SolReader<V,Info> {
    
    /** Change the value attached to the variable <code>v</code> by
        joining <code>delta</code> to it.  The real join operation is
        performed by the <code>join</code> method of the variable
        <code>v</code>; that method should be able to handle
        <code>delta</code>.  In addition to the real join, this method
        will do some internal book-keeping for the fixed-point
        computation (e.g., it puts back in the workset the variables
        that change). */
    public void join(V v, Info delta);
    
}
