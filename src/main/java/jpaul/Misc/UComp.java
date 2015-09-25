// UComp.java, created Thu Feb 10 13:22:57 2000 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

import java.util.Comparator;

/**
 * <code>UComp</code> is an universal comparator, which compares any two
 objecs by simply comparing their string representation.  It is useful
 when you need a deterministic string representation of a set (for debug
 purposes).  As sets don't have any ordering info, two equal sets
 could have different representations; instead you can convert the set to
 an array of elements, sort it with the help of this universal comparator
 and print it.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: UComp.java,v 1.3 2006/03/14 02:29:31 salcianu Exp $
 */
public class UComp<T> implements Comparator<T>{

    /** Compares its two arguments for order.
	Returns a negative integer, zero, or a positive integer as the
	string representation of the first argument is less than, equal
	to, or greater to the string representation of the second. */
    public int compare(T o1, T o2) {
	if(o1 == o2)   return 0;
	String str1 = (o1 == null) ? "null" : o1.toString(); 
	String str2 = (o2 == null) ? "null" : o2.toString();
	return str1.compareTo(str2);
    }

    /** Indicates whether some other object is &quot;equal&quot; to"
	<code>this</code> Comparator. The easiest implementation:
	always return <code>false</code> unless
	<code>obj == this</code>. */
    public boolean equals(Object obj) {
	return this == obj;
    } 
}
