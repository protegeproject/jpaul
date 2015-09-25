// SetFactory.java, created Mon Jul  4 09:12:29 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Set;
import java.util.Collection;


/**
 * <code>SetFactory</code> is a set-instance of the factory pattern.
 * Default implementations are provided for most of the methods.
 * Subclasses must implement the <code>create()</code> method; for
 * efficiency reasons, they may also override the {@link
 * #newColl(Collection)} method.
 * 
 * <p>Various set factories are available in the class {@link
 * SetFacts SetFacts}.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SetFactory.java,v 1.10 2006/01/29 17:49:09 adam_kiezun Exp $ */
public abstract class SetFactory<T> implements Factory<Set<T>>, CollectionFactory<T>, Serializable {

    /** Default implementation: invokes {@link #newColl(Collection)}. 
	@see Factory#create(Object) */
    public Set<T> create(Set<T> s) {
	return newColl(s);
    }

    /** Default implementation: invokes {@link #create()}.  Yes, it
        would have been more consistent to make all
        <code>create</code> methods invoke <code>newColl</code>
        methods (or the other way around).  Still, technical typing
        aspects prevent us from doing so: <code>create</code> must
        return a set, so it cannot invoke a <code>newColl</code> (that
        returns only a collection); similarly, {@link
        #newColl(Collection)} cannot invoke {@link #create(Set)}. */
    public Collection<T> newColl() {
	return this.create();
    }

    /** Default implementation: uses <code>create()</code> to create
        an empty set, and next adds all elements from
        <code>coll</code> to the newly-created set. 

	@see Factory#create(Object) */
    public Set<T> newColl(Collection<T> coll) {
	Set<T> newSet = this.create();
	newSet.addAll(coll);
	return newSet;
    }

}
