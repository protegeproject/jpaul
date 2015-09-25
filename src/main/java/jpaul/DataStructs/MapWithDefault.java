// MapWithDefault.java, created Wed Aug 10 08:02:56 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Map;
import java.util.LinkedHashMap;

/**
   <code>MapWithDefault</code> is a map that returns default values
   for unassigned keys.  Notice the difference with the usual
   implementations of <code>Map</code>, that return <code>null</code>
   for unassigned keys.
   
   A boolean flag in the constructor choose between two possible
   behaviours for the <code>get</code> method.

   <ul>

   <li><i>with memory</i>: for any unassigned key, in addition to
   returning a default value, <code>get</code> will store this value
   in the map.  Therefore, if you execute another <code>get</code> for
   the same key (without any relevant <code>put</code> in between, you
   will get the same object.

   <li><i>without memory</i>: for unassigned keys, <code>get</code>
   just returns a default value.  It does not mutate the map.

   </ul>
   
   
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MapWithDefault.java,v 1.6 2006/03/14 02:29:31 salcianu Exp $ */
public class MapWithDefault<K,V> extends MapWrapper<K,V> {
    
    /** Creates a <code>MapWithDefault</code>, backed by a
        <code>LinkedHashMap</code>.

	@param defValFact Factory that provides the default values for
	previously unassigned keys. 

	@param withMemory Selects one of the two behaviours described
	above. */
    public MapWithDefault(Factory<V> defValFact, boolean withMemory) {
	this(new LinkedHashMap<K,V>(), defValFact, withMemory);
    }


    /** Creates a <code>MapWithDefault</code>. 

	@param map Underlying map.

	@param defValFact Factory that provides the default values for
	previously unassigned keys.

	@param withMemory Selects one of the two behaviours described
	above. */
    public MapWithDefault(Map<K,V> map, Factory<V> defValFact, boolean withMemory) {
	super(map);
        this.defValFact = defValFact;
	this.withMemory = withMemory;
    }

    // the default value factory
    private final Factory<V> defValFact;
    // if true, get will mutate the map (see comments around constructors).
    private final boolean withMemory;


    /** Returns the value to which this map maps the specified key.
        Unlike the common implementations of <code>Map</code>, if the
        key is not mapped to anything, a freshly-generated value will
        be returned instead.  In addition, for maps with memory, the
        previously unassigned key will be mapped to the
        freshly-generated value (note that in this case,
        <code>get</code> mutates the map). */
    public V get(Object key) {
	V val = super.get(key);
	if(val == null) {
	    val = defValFact.create();
	    if(withMemory) {
		this.put((K) key, val);
	    }
	}
	return val;
    }


    /** Returns the value assigned to <code>key</code>, if any.
        Otherwise, returns <code>null</code> (unlike <code>get</code>,
        no default element is generated, and the map is never mutated
        by this method. */
    public V getNoDefault(Object key) {
	return super.get(key);
    }

}
