// Debug.java, created Thu Feb 10 19:06:16 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jpaul.Misc.UComp;

/**
 * <code>Debug</code> contains some simple debugging methods.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Debug.java,v 1.8 2006/03/14 02:29:31 salcianu Exp $
 */
public final class Debug {

    /** Make sure nobody can instantiate this class. */
    private Debug() { /*empty*/ }

    /** Returns a list containing all the objects from a collection,
	in increasing lexicographic order of their string
	representations. */
    public static <T> List<T> sortedCollection(Collection<T> coll) {
	List<T> list = new LinkedList<T>(coll);
	Collections.sort(list, new UComp<T>());
	return list;
    }

    /** Returns a string representation of all elements from a
	collection, in increasing lexicographic order of their string
	representations. */
    public static <T> String stringImg(Collection<T> coll) {
	StringBuffer buffer = new StringBuffer();

	buffer.append("[ ");
	for(T t : sortedCollection(coll)) {
	    buffer.append(t);
	    buffer.append(" ");
	}
	buffer.append("]");

	return buffer.toString();
    }

    /** Returns a string representation of all elements from an array,
        in increasing lexicographic order of their string
        representations.  */
    public static <T> String stringImg(T[] v) {
	if(v == null) return "null";

	StringBuffer buffer = new StringBuffer();

	Arrays.sort(v, new UComp<T>());
	for(int i=0; i<v.length; i++) {
	    buffer.append(v[i]);
	    buffer.append("\n");
	}

	return buffer.toString();
    }

}
