// Factory.java, created Wed Jul  6 07:37:29 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

/**
 * <code>Factory</code> for the factory pattern.  Encapsulates methods
 * for creating and copying objects of a certain class.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Factory.java,v 1.2 2005/08/10 22:43:41 salcianu Exp $ */
public interface Factory<T> {

    /** Create a new object of class <code>T</code>.  Corresponds to a
        default constructor. */
    public T create();

    /** Create a new object of class <code>T</code>, as a copy of
        <code>t</code>.  Corresponds to a copy constructor. */
    public T create(T t);
    
}
