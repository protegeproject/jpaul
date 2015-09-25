// WorkList.java, created Tue Feb 22 09:47:07 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;

/**
 * <code>WorkList</code> is a <code>WorkSet</code> with FIFO order.
 * Good for algorithms that work on levels.  The
 * <code>add</code>/<code>extract</code> operations have O(1)
 * complexity.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkList.java,v 1.7 2006/03/14 02:29:31 salcianu Exp $ */
public class WorkList<T> extends WorkListAbstr<T> implements Serializable {
    
    private static final long serialVersionUID = -260214511508356742L;

    /** Overrides the abstract method {@link
        jpaul.DataStructs.WorkSetAbstr#addToOrder}.  Add
        <code>elem</code> at the tail of the list, to implement FIFO
        order. */
    protected void addToOrder(T elem) {
	list.addLast(elem);
    }
    
}
