// IdentityWrapper.java, created Mon Aug 22 09:27:21 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>IdentityWrapper</code> is useful when you want to assign some
 * information to each object, not to each class of equal objects.  An
 * <code>IdentityWrapper</code> is a wrapper around the original
 * object that redefines <code>equals</code> and <code>hashCode</code>
 * to implement "equality as identity".
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: IdentityWrapper.java,v 1.3 2006/03/14 02:29:31 salcianu Exp $ */
public class IdentityWrapper<T> {
    
    /** Creates a <code>IdentityWrapper</code>. 

	@param obj Object to wrap. */
    public IdentityWrapper(T obj) {
        this.obj = obj;
    }

    public final T obj;
    
    public boolean equals(Object o) {
	if(o == null) return false;
	if(o == this) return true;
	if(!(o instanceof IdentityWrapper/*<T>*/)) return false;
	return this.obj == ((IdentityWrapper) o).obj;
    }

    public int hashCode() {
	return System.identityHashCode(this.obj);
    }

    public String toString() {
	return "(iw: " + obj + ")";
    }

}
