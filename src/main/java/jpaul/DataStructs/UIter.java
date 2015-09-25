// UIter.java, created Thu Jul  7 10:57:19 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Iterator;

/**
 * <code>UIter</code> - read-only iterator represting the union of two iterators
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: UIter.java,v 1.1 2005/07/07 21:40:53 salcianu Exp $
 */
class UIter<E> implements Iterator<E> {
    public UIter(Iterator<E> it1, Iterator<E> it2) {
	this.it1 = it1;
	this.it2 = it2;
    }
    private final Iterator<E> it1;
    private final Iterator<E> it2;

    public E next() {
	if(it1.hasNext()) return it1.next();
	return it2.next();
    }
    public boolean hasNext() {
	return it1.hasNext() || it2.hasNext();
    }
    public void remove() {
	throw new UnsupportedOperationException();
    }    
}
