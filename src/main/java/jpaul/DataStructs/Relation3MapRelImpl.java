// Relation3MapRelImpl.java, created Wed Aug 10 09:12:53 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;
import java.util.Collections;

/**
 * <code>Relation3MapRelImpl</code> is a simple implementation of
 * <code>Relation3</code>, backed by a <code>Map</code> from
 * <code>Ta</code> keys to <code>RelationTb,Tc</code> between
 * <code>Tb</code> and <code>Tc</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Relation3MapRelImpl.java,v 1.10 2006/03/14 02:29:31 salcianu Exp $ */
public class Relation3MapRelImpl<Ta,Tb,Tc> extends Relation3<Ta,Tb,Tc> {
    
    /** Creates a <code>Relation3MapRelImpl</code>.  This constructor
        allows the user to indicate what map and relation
        implementations he wants to use.  Therefore, this constructor
        is good while finely-tuning the performances of an application
        using a large number of small ternary relations.  */
    public Relation3MapRelImpl(MapFactory<Ta,Relation<Tb,Tc>> mapFact,
			       RelationFactory<Tb,Tc> relFact) {
	this.map = new MapWithDefault<Ta,Relation<Tb,Tc>>(mapFact.create(), relFact, true);
    }

    /** Creates a <code>Relation3MapRelImpl</code> object using a
        factory of <code>LinkedHashMap</code>s and a factory of
        <code>MapSetRelation</code>s.  This default constructor
        consumes a lot of memory but should work just fine for large
        and not-very-frequent ternary relations.  */
    public Relation3MapRelImpl() {
	this(MapFacts.<Ta,Relation<Tb,Tc>>hash(),
	     new MapSetRelationFactory<Tb,Tc>());
    }

    private MapWithDefault<Ta,Relation<Tb,Tc>> map;

    public boolean add(Ta a, Tb b, Tc c) {
	Relation<Tb,Tc> rel = map.get(a);
	return rel.add(b,c);
    }

    public boolean remove(Ta a, Tb b, Tc c) {
	Relation<Tb,Tc> rel = map.getNoDefault(a);
	if(rel == null)
	    return false;	
	if(!rel.remove(b,c))
	    return false;
	rel.remove(b,c);
	if(rel.isEmpty())
	    map.remove(a);
	return true;
    }

    public boolean contains(Ta a, Tb b, Tc c) {
	Relation<Tb,Tc> rel = map.getNoDefault(a);
	if(rel == null)
	    return false;
	return rel.contains(b,c);
    }

    public Collection<Ta> getKeys() {
	return Collections.<Ta>unmodifiableCollection(map.keySet());
    }

    public Collection<Tb> get2ndValues(Ta a) {
	Relation<Tb,Tc> rel = map.getNoDefault(a);
	if(rel == null) {
	    return Collections.<Tb>emptySet();
	}
	return rel.keys();
    }

    public Collection<Tc> get3rdValues(Ta a, Tb b) {
	Relation<Tb,Tc> rel = map.getNoDefault(a);
	if(rel == null)
	    return Collections.<Tc>emptySet();
	return Collections.<Tc>unmodifiableCollection(rel.getValues(b));
    }

    public void clear() {
	map.clear();
    }

}
