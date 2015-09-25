// Relation3.java, created Wed Aug 10 08:27:11 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Collection;

/**
 * <code>Relation3</code> models a simple ternary relation.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Relation3.java,v 1.7 2006/03/14 02:29:31 salcianu Exp $
 */
public abstract class Relation3<Ta,Tb,Tc> {
    
    /** Adds the triple &lta,b,c&gt; to <code>this</code> ternary
        relation.*/
    public abstract boolean add(Ta a, Tb b, Tc c);

    /** Removes the triple &lta,b,c&gt; from <code>this</code> ternary
        relation.*/
    public abstract boolean remove(Ta a, Tb b, Tc c);
    
    /** Checks the presence of the triple &lta,b,c&gt; in
        <code>this</code> ternary relation.  */
    public abstract boolean contains(Ta a, Tb b, Tc c);

    /** Returns the elements that appear in the 1st position of at
        least one triple from <code>this</code> ternary relation.
        Unmodifiable view. */
    public abstract Collection<Ta> getKeys();

    /** Returns the elements that appear in the 2nd position of at
        least one triple that appears in <code>this</code> ternary
        relation and has <code>a</code> in its 1st position.
        Unmodifiable view. */
    public abstract Collection<Tb> get2ndValues(Ta a);

    /** Returns the elements that appear in the third position of at
        least one triple of the form &lt;a,b,*%gt; that appears in
        <code>this</code> ternary relation.  */
    public abstract Collection<Tc> get3rdValues(Ta a, Tb b);

    /** Returns the number of &lt;a,b,c&gt; triples in
        <code>this</code> ternary relation.  Linear in the size of the
        relation.  This may be also implemented by incrementally in
        O(1) by updating a <code>size</code> field, but that may
        complicate the code in the presence of subclassing, etc.  Will
        think about it if it becomes a problem. */
    public int size() {
	int size = 0;
	for(Ta a: getKeys()) {
	    for(Tb b: get2ndValues(a)) {
		size += get3rdValues(a, b).size();
	    }
	}
	return size;
    }

    /** Removes all associations from <code>this</code> ternary
        relation. */
    public abstract void clear();

    public String toString() {
	StringBuffer buff = new StringBuffer();
	buff.append("{\n");
	for(Ta a : getKeys()) {
	    buff.append("  " + a + " -> \n");
	    for(Tb b : get2ndValues(a)) {
		buff.append("    " + b + " -> " + get3rdValues(a, b) + "\n");
	    }	    
	}
	buff.append("}");
	return buff.toString();
    }

}
