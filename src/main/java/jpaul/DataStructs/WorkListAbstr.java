// WorkListAbstr.java, created Mon Jul  4 13:23:18 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;
import java.util.LinkedList;

/**
 * <code>WorkListAbstr</code> extends a <code>WorkSetAbstr</code> by
 * adding a <code>List</code> field that provides the extraction order
 * of the elements.  The order in which elements are added to the list
 * must be chosen in its subclasses.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkListAbstr.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
abstract class WorkListAbstr<T> extends WorkSetAbstr<T> {
    
    /** List that provides the order of the elements from
        <code>this</code> workset. */
    protected LinkedList<T> list = new LinkedList<T>();

    /** Overrides {@link
        jpaul.DataStructs.WorkSetAbstr#extractInOrder}.  States that
        we always extract elements from the head of the list.
        Therefore, subclasses define the extraction order by
        overriding {@link
        jpaul.DataStructs.WorkSetAbstr#addToOrder}. */
    protected T extractInOrder() {
	return list.removeFirst();
    }

    protected Collection<T> underlyingOrder() {
	return list;
    }

}
