// BijMap.java, created Sun Aug 21 07:50:40 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Map;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;

import jpaul.Misc.Function;

/**
 * <code>BijMap</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BijMap.java,v 1.4 2006/03/14 02:29:30 salcianu Exp $
 */
public class BijMap<A,B> extends MapWrapper<A,B> {
    
    /** Creates a <code>BijMap</code>. */
    public BijMap(MapFactory<A,B> mapFact, MapFactory<B,A> revMapFact) {
	super(mapFact.create());
	this.revMap = revMapFact.create();
	this.revBijMap = new BijMap<B,A>(this, revMap, map, revMapFact, mapFact);
    }

    public BijMap() {
	this(MapFacts.<A,B>hash(), MapFacts.<B,A>hash());
    }

    // inverse of the underlying direct map
    private final Map<B,A> revMap;
    // reverse bijective map
    private final BijMap<B,A> revBijMap;


    /** Returns a reverse view of this <code>BijMap</code>.  The
        returned view is fully functional (i.e., allows mutation) and
        connected to this bijective map (i.e., changes in one of them
        are reflected in the other one). */
    public BijMap<B,A> rev() {
	return revBijMap;
    }


    // private constructor used for building up revBijMap
    private BijMap(BijMap<B,A> revBijMap, Map<A,B> dirMap, Map<B,A> revMap,
		   MapFactory<A,B> mapFact, MapFactory<B,A> revMapFact) {
	super(dirMap);
	this.revMap = revMap;
	this.revBijMap = revBijMap;
    }


    public void clear() {
	super.clear();
	revMap.clear();
    }


    public boolean containsValue(Object value) {
	return revMap.containsKey(value);
    }


    /** Returns a set view of the entries from this
        <code>BijMap</code>.  The view is fully functional (i.e.,
        allows mutation) and connected to this bijective map (i.e.,
        changes in one of them are reflected in the other one). */
    public Set<Map.Entry<A,B>> entrySet() {
	return new AbstractSet<Map.Entry<A,B>>() {
	    public int size() {
		return map.size();
	    }
	    public Iterator<Map.Entry<A,B>> iterator() {
		return new Iterator<Map.Entry<A,B>>() {
		    private final Iterator<Map.Entry<A,B>> it = map.entrySet().iterator();
		    private Map.Entry<A,B> last = null;
		    public boolean hasNext() {
			return it.hasNext(); 
		    }
		    public Map.Entry<A,B> next() { 
		        last = it.next();
			return last;
		    }
		    public void remove() {
			it.remove();
			revMap.remove(last.getValue());
		    }
		};
	    }
	    public boolean remove(Object o) {
		if(!(o instanceof Map.Entry/*<A,B>*/)) return false;
		@SuppressWarnings("unchecked")
		Map.Entry<A,B> entry = (Map.Entry<A,B>) o;
		B currValue = BijMap.this.get(entry.getKey());
		if((currValue == null) || !currValue.equals(entry.getValue())) return false;
		BijMap.this.remove(entry.getKey());
		return true;
	    }
	    public void clear() {
		BijMap.this.clear();
	    }
	};
    }

    
    public Set<A> keySet() {
	return new AbstractSet<A>() {
	    public int size() { return map.size(); }
	    public Iterator<A> iterator() {
		return 
		    DSUtil.<Map.Entry<A,B>,A>mapIterator
		    (BijMap.this.entrySet().iterator(),
		     new Function<Map.Entry<A,B>,A>() {
			public A f(Map.Entry<A,B> entry) {
			    return entry.getKey();
			}
		    });
	    }
	    public boolean remove(Object a) {
		return BijMap.this.remove(a) != null;
	    }
	    public void clear() {
		BijMap.this.clear();
	    }
	};
    }


    public B put(A key, B value) {
	B oldValue = map.put(key, value);
	if(oldValue != null) {
	    revMap.remove(oldValue);
	}
	revMap.put(value, key);
	return oldValue;
    }

    public void putAll(Map<? extends A,? extends B> t) {
	for(Map.Entry<? extends A, ? extends B> entry : t.entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }

    public B remove(Object key) {
	B oldValue = map.remove(key);
	if(oldValue != null) {
	    revMap.remove(oldValue);
	}
	return oldValue;
    }

    public int size() {
	return map.size();
    }


    /** Returns a collection view of the values from this
        <code>BijMap</code>.  The view is fully functional (i.e.,
        allows mutation) and connected to this bijective map (i.e.,
        changes in one of them are reflected in the other one). */
    public Collection<B> values() {
	return new AbstractCollection<B>() {
	    public int size() { return map.size(); }
	    public Iterator<B> iterator() {
		return 
		    DSUtil.<Map.Entry<A,B>,B>mapIterator
		    (BijMap.this.entrySet().iterator(),
		     new Function<Map.Entry<A,B>,B>() {
			public B f(Map.Entry<A,B> entry) {
			    return entry.getValue();
			}
		    });
	    }
	    public boolean remove(Object b) {
		return BijMap.this.rev().remove(b) != null;
	    }
	    public void clear() {
		BijMap.this.clear();
	    }
	};
    }
        
}
