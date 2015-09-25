// IntMCell.java, created Wed Jul 13 07:30:53 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>IntMCell</code> is a mutable cell with integer content.
 *
 * @see MCell
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: IntMCell.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public class IntMCell implements Comparable<IntMCell> {
    
    /** The current int content of this cell. */
    public int value;

    /** Creates an <code>IntMCell</code> with initial content
        <code>0</code>. */
    public IntMCell() {
	this(0);
    }

    /** Creates an <code>IntMCell</code> with initial content
        <code>initValue</code>. */
    public IntMCell(int initValue) {
        this.value = initValue;
    }

    public int compareTo(IntMCell imc2) {
	return this.value - imc2.value;
    }
    
    /** Returns a string description containing both the value and the
	address of <code>this</code> <code>IntMCell</code>:
	<code>"IntMCell{" + value + "}@" +
	System.identityHashCode(this)</code>. */
    public String toString() {
	return "IntMCell{" + value + "}@" + System.identityHashCode(this);
    }

}
