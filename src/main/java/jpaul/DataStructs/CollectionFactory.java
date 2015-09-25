// CollectionFactory.java, created Fri Jul 22 07:11:04 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;

/**
 * <code>CollectionFactory</code> is a collection-specific instance of
 * the factory pattern.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: CollectionFactory.java,v 1.2 2005/08/10 22:43:41 salcianu Exp $ */
public interface CollectionFactory<E> {
    
    /** Creates an empty collection. */
    public Collection<E> newColl();

    /** Creates a collection that contains all elements from
        <code>c</code>. */
    public Collection<E> newColl(Collection<E> c);

}
