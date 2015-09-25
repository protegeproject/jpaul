// SolReader.java, created Mon Jun 20 14:23:02 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints;

/**
 * <code>SolReader</code> provides read-only access to the (possibly
 * partial) solution of a system of constraints.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SolReader.java,v 1.4 2005/08/11 22:57:08 salcianu Exp $ */
public interface SolReader<V extends Var<Info>, Info> {

    /** @return value associated with the variable <code>v</code>. */
    public Info get(V v);
    
}
