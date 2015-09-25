// MapSetRelationFactory.java, created Mon Jul  4 09:24:08 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Set;

/**
 * <code>MapSetRelationFactory</code> generates
 * <code>MapSetRelation</code>s that use a specific
 * <code>MapFactory</code> and a specific <code>SetFactory</code>.
 *
 * @see MapSetRelation
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapSetRelationFactory.java,v 1.9 2006/03/14 02:29:31 salcianu Exp $ */
class MapSetRelationFactory<K,V> extends RelationFactory<K,V> {
    
    /** Default constructor: creates a
        <code>MapSetRelationFactory</code> based on a
        <code>HashMap</code> factory and a
	<code>HashSet</code> factory.

	@see #MapSetRelationFactory(MapFactory, SetFactory) 
	@see jpaul.DataStructs.SetFacts#hash() SetFacts.hash()
	@see jpaul.DataStructs.MapFacts#hash() MapFacts.hash() */
    public MapSetRelationFactory() {
	this(MapFacts.<K,Set<V>>hash(), SetFacts.<V>hash());
    }

    /** Creates a <code>MapSetRelationFactory</code> that will
        generate <code>MapSetRelation</code>s that use the map factory
        <code>mapFact</code> and the set factory <code>setFact</code>.
        This constructor allows the programmer to finely tune the kind
        of relation that he wants.  */
    public MapSetRelationFactory(MapFactory<K,Set<V>> mapFact, SetFactory<V> setFact) {
	this.mapFact = mapFact;
	this.setFact = setFact;
    }

    private final MapFactory<K,Set<V>> mapFact;
    private final SetFactory<V> setFact;

    public Relation<K,V> create() {
	return new MapSetRelation<K,V>(mapFact, setFact);
    }
    
    public Relation<K,V> create(Relation<K,V> r) {
	if(r instanceof MapSetRelation/*<K,V>*/) {
	    MapSetRelation<K,V> msr = (MapSetRelation<K,V>) r;
	    return msr.clone();
	}
	return super.create(r);
    }
    
}
