// MapFactory.java, created Thu Jul  7 18:25:13 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;
import java.util.Map;

/**
 * <code>MapFactory</code> is a map-specific instance of the factory
 * pattern.  Various map factories are available in the class {@link
 * MapFacts MapFacts}.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapFactory.java,v 1.8 2006/02/15 16:04:18 adam_kiezun Exp $ */
public abstract class MapFactory<K,V> implements Factory<Map<K,V>>, Serializable {
    
    public abstract Map<K,V> create();

    /** Default implementation: uses <code>create()</code> to create
        an empty map, and next adds each entry from <code>m</code> to
        the newly-created map.

	@see Factory#create(Object) */
    public Map<K,V> create(Map<K,V> m) {
	Map<K,V> newMap = create();
	for(Map.Entry<K,V> entry : m.entrySet()) {
	    newMap.put(entry.getKey(), entry.getValue());
	}
	return newMap;
    }

}
