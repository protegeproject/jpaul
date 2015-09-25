// WorkPriorityQueue.java, created Mon Jul  4 13:28:46 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
   <code>WorkPriorityQueue</code> is a <code>WorkSet</code> whose
   elements are extracted in the increasing order of their priorities.
   The <code>add</code>/<code>extract</code> operations have
   logarithmic complexity.
   
   (Note that from a linguistic perspective, a
   <code>PriorityQueue</code> provides elements according to their
   inverse priorities: smallest priorities first)
 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: WorkPriorityQueue.java,v 1.8 2006/03/14 02:29:31 salcianu Exp $ */
public class WorkPriorityQueue<T> extends WorkSetAbstr<T> implements Serializable {
    
    private static final long serialVersionUID = -538051969318294466L;

    /** Creates a <code>WorkPriorityQueue</code>. 

	@param comp Comparator used to determine the priority order
	between the elements of this <code>WorkSet</code>. */
    public WorkPriorityQueue(Comparator<T> comp) {
	pq = new PriorityQueue<T>(1024, comp);
    }

    private final PriorityQueue<T> pq;

    protected void addToOrder(T elem) {
	pq.add(elem);
    }

    protected T extractInOrder() {
	if(pq.size() == 0) {
	    throw new NoSuchElementException("empty priority queue");
	}
	return pq.poll();
    }

    protected Collection<T> underlyingOrder() {
	return pq;
    }
    
}
