// Pair.java, created Sun Mar  7 13:00:18 2004 by salcianu
// Copyright (C) 2003 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.io.Serializable;

import jpaul.Misc.Function;

/**
 * <code>Pair</code> is an immutable pair of two values.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Pair.java,v 1.7 2006/03/14 02:29:31 salcianu Exp $
 */
public final class Pair<A,B> implements Serializable {
    private static final long serialVersionUID = -8379293161461968107L;

    public Pair(A a, B b) { this.left = a; this.right = b; }
    public final A left;
    public final B right;

    public int hashCode() {
	int hash = 0;
	if(left != null)
	    hash += 3*left.hashCode();
	if(right != null)
	    hash += 5*right.hashCode();
	return hash;
    }

    public boolean equals(Object o2) {
	if(!(o2 instanceof Pair)) return false;
	Object left2  = ((Pair) o2).left;
	Object right2 = ((Pair) o2).right;

	return
	    DSUtil.checkEq(left, left2) &&
	    DSUtil.checkEq(right, right2);
    }

    public String toString() {
	return "<" + DSUtil.toString(left) + "," + DSUtil.toString(right) + ">";
    }

    public static <A,B> Function<Pair<A,B>,A> leftProj() {
	return new Function<Pair<A,B>,A>() {
	    public A f(Pair<A,B> pair) {
		return pair.left;
	    }
	};
    }

    public static <A,B> Function<Pair<A,B>,B> rightProj() {
	return new Function<Pair<A,B>,B>() {
	    public B f(Pair<A,B> pair) {
		return pair.right;
	    }
	};
    }

}
