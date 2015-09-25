// BoolMCell.java, created Wed Jul 13 07:30:53 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>BoolMCell</code> is a mutable cell with boolean content.
 *
 * @see MCell
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BoolMCell.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public class BoolMCell implements Comparable<BoolMCell> {
    
    /** The current boolean content of this cell. */
    public boolean value;

    /** Creates a <code>BoolMCell</code> with initial value
        <code>initValue</code>. */
    public BoolMCell(boolean initValue) {
        this.value = initValue;
    }
    
    public int compareTo(BoolMCell bmc2) {
	if(this.value == bmc2.value) return 0;
	if(this.value) return +1;
	return -1;
    }

    /** Returns a string description containing both the value and the
	address of <code>this</code> <code>BoolMCell</code>:
	<code>"BoolMCell{" + value + "}@" +
	System.identityHashCode(this)</code>. */
    public String toString() {
	return "BoolMCell{" + value + "}@" + System.identityHashCode(this);
    }

}
