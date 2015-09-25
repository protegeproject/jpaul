// AbstractMapEntry.java, created Fri Jan 13 13:25:04 2006 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Map;

/**
 * <code>AbstractMapEntry</code> is an abstract implementation of the
 * <code>Map.Entry</code> interface.  To obtain a working unmodifiable
 * <code>Map.Entry</code>, one needs to implement <code>getKey</code>
 * and <code>getValue</code> in the subclass.  For a mutable
 * <code>Map.Entry</code>, one also needs to override the
 * implementation of <code>setValue</code>; the default implementation
 * simply an <code>UnsupportedOperationException</code>.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: AbstractMapEntry.java,v 1.3 2006/03/14 02:29:30 salcianu Exp $ */
abstract class AbstractMapEntry<K,V> implements Map.Entry<K,V> {

    public abstract K getKey();

    public abstract V getValue();

    public V setValue(V newValue) { throw new UnsupportedOperationException(); }

    public int hashCode() {
	return 
	    (getKey()==null   ? 0 : getKey().hashCode()) ^
	    (getValue()==null ? 0 : getValue().hashCode());
    }

    public boolean equals(Object obj) {
	if(obj == this) return true;
	if(obj == null) return false;
	if(!(obj instanceof Map.Entry))
	    return false;
	@SuppressWarnings("unchecked")
	Map.Entry<K,V> e2 = (Map.Entry<K,V>) obj;
	return 
	    DSUtil.checkEq(this.getKey(), e2.getKey()) &&
	    DSUtil.checkEq(this.getKey(), e2.getKey());
    }

    public String toString() {
	return "<" + getKey() + "," + getValue() + ">";
    }

}
