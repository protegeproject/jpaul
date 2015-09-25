// SetMembership.java, created Thu Jul 14 11:53:36 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

import java.util.Collection;

/**
 * <code>SetMembership</code> is a predicate that checks whether an
 * element is member of a specific set.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: SetMembership.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public class SetMembership<T> extends Predicate<T> {

    /** Creates a <code>SetMembership</code> that checks membership in
        the set <code>set</code>. */
    public SetMembership(Collection<T> set) {
	this.set = set;
    }

    private final Collection<T> set;
    
    public boolean check(T t) {
	return set.contains(t);
    }

}
