// WorkStack.java, created Tue Feb 22 09:49:08 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;

/**
 * <code>WorkStack</code> is a <code>WorkSet</code> with LIFO order.
 * Good for algorithms that like to iterate over the most-recently
 * changed spots.  The <code>add</code>/<code>extract</code>
 * operations have O(1) complexity.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkStack.java,v 1.7 2006/03/14 02:29:31 salcianu Exp $ */
public class WorkStack<T> extends WorkListAbstr<T> implements Serializable {

    private static final long serialVersionUID = 1552653464204988856L;

    protected void addToOrder(T elem) {
	list.addFirst(elem);
    }

}
