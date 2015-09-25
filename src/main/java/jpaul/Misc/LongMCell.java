// LongMCell.java, created Sun Jan 22 09:38:51 2006 by salcianu
// Copyright (C) 2006 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>LongMCell</code> is a mutable cell with <code>long</code> content.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: LongMCell.java,v 1.4 2006/03/14 02:29:31 salcianu Exp $
 */
public class LongMCell implements Comparable<LongMCell> {
    
    /** The current long content of this cell. */
    public long value;

    /** Creates a <code>LongMCell</code>. */
    public LongMCell() {
        this(0);
    }
    
    /** Creates a <code>LongMCell</code> with initial content
        <code>initValue</code>. */
    public LongMCell(long initValue) {
        this.value = initValue;
    }

    public int compareTo(LongMCell lmc2) {
	// cannot simply return the diff due to the type mismatch;
	// unclear what happens to the sign if we coerce a long into
	// an int ...
	long diff = this.value - lmc2.value;
	if(diff < 0) return -1;
	if(diff == 0) return 0;
	return +1;
    }

    /** Returns a string description containing both the value and the
	address of <code>this</code> <code>LongMCell</code>:
	<code>"LongMCell{" + value + "}@" +
	System.identityHashCode(this)</code>. */
    public String toString() {
	return "LongMCell{" + value + "}@" + System.identityHashCode(this);
    }

}
