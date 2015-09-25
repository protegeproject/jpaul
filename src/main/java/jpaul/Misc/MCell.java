// MCell.java, created Wed Jul 13 07:29:02 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Misc;

/**
 * <code>MCell</code> is a mutable cell with content of type
 * <code>T</code>.  Sometimes useful when using anonymous classes that
 * can access only final variables of the surounding method: we
 * declare a final variable pointing to a mutable cell, and the
 * anonymous class is free to mutate the cell's content.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: MCell.java,v 1.5 2006/03/14 02:29:31 salcianu Exp $ */
public class MCell<T> {

    /** The current content of this cell. */
    public T value;

    /** Creates a <code>MutableCell</code>. */
    public MCell(T initValue) {
	this.value = initValue;        
    }

    /** Returns a string description containing both the value and the
	address of <code>this</code> <code>MCell</code>:
	<code>"MCell{" + value + "}@" +
	System.identityHashCode(this)</code>. */
    public String toString() {
	return "MCell{" + value + "}@" + System.identityHashCode(this);
    }

}
