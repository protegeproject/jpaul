// ReverseListIterator.java, created Fri Mar 15 19:39:14 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.List;
import java.util.ListIterator;

/**
   <code>ReverseListIterator</code> takes a <code>List</code> and
   produces a <code>ListIterator</code> that traverses the list in
   reverse order.  This class does not need to take a snapshot of the
   original list and construct a new list.  Instead, it switches the
   actions of <code>next*</code> and <code>prev*</code> methods.

   Note: class taken from the <code>jutil</code> project, with the
   aggreement of the author Scott C. Ananian.
 
   @author  C. Scott Ananian - cananian@alumni.princeton.edu
   @version $Id: ReverseListIterator.java,v 1.2 2005/08/10 23:34:21 salcianu Exp $ */
public class ReverseListIterator<E> implements ListIterator<E> {
    private final ListIterator<E> it;
    /** Creates a <code>ReverseListIterator</code>. */
    public ReverseListIterator(List<E> l) {
        this.it = l.listIterator(l.size());
    }
    public void add(E o) { it.add(o); }
    public boolean hasNext() { return it.hasPrevious(); }
    public boolean hasPrevious() { return it.hasNext(); }
    public E next() { return it.previous(); }
    public int nextIndex() { return it.previousIndex(); }
    public E previous() { return it.next(); }
    public int previousIndex() { return it.nextIndex(); }
    public void remove() { it.remove(); }
    public void set(E o) { it.set(o); }
}
