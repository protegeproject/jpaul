// RelationFactory.java, created Mon Jul  4 09:23:00 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

/**
 * <code>RelationFactory</code> is a relation-specific instance of the
 * factory pattern.
 * 
 * <p>Various relation factories are available in the class {@link
 * RelFacts RelFacts}.
 *
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: RelationFactory.java,v 1.4 2005/08/22 14:07:21 salcianu Exp $ */
public abstract class RelationFactory<K,V> implements Factory<Relation<K,V>> {
    
    public abstract Relation<K,V> create();

    /** Default implementation: uses {@link #create()} to create an
        empty relation, and next add each pair from <code>r</code> to
        the new relation.  */
    public Relation<K,V> create(Relation<K,V> r) {
	// default implementation
	Relation<K,V> r2 = this.create();
	for(K key : r.keys()) {
	    r2.addAll(key, r.getValues(key));
	}
	return r2;
    }
    
}
