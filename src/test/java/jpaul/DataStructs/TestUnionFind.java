// TestUnionFind.java, created Fri Jan 13 13:15:42 2006 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.DataStructs;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Arrays;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <code>TestUnionFind</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TestUnionFind.java,v 1.3 2006/01/29 16:05:28 adam_kiezun Exp $
 */
public class TestUnionFind extends TestCase {
    
    public static Test suite() {
	return new TestSuite(TestUnionFind.class);
    }

    public void testUnionFind() {
	UnionFind<String> ds = new UnionFind<String>();
	String a="a", b="b", c="c", d="d", e="e", f="f", g="g", h="h";

	assert ds.unUnified(a) && ds.unUnified(b) && ds.unUnified(c);
	assert ds.find(a)==a && ds.find(b)==b && ds.find(c)==c;
	assert ds.unUnified(a) && ds.unUnified(b) && ds.unUnified(c);
	assert ds.allKnownElements().isEmpty();

	System.out.print(ds.allNonTrivialEquivalenceClasses());
	System.out.println("; " + ds.allKnownElements());

	ds.union(a,a); ds.union(c,c);
	System.out.print(ds.allNonTrivialEquivalenceClasses());
	System.out.println("; " + ds.allKnownElements());
	assert ds.unUnified(a) && ds.unUnified(b) && ds.unUnified(c);
	assert ds.allKnownElements().equals(new LinkedHashSet<String>(Arrays.asList(a, c)));

	ds.union(e, c); ds.union(b, h); 

	assert ds.equivalenceClass(e).contains(c);
	assert !ds.equivalenceClass(e).contains(b);
	assert ds.equivalenceClass(e).size() == 2;
	assert ds.allKnownElements().equals(new LinkedHashSet<String>(Arrays.asList(a, c, e, b, h)));

	ds.union(h, c);

	assert ds.equivalenceClass(e).size() == 4;
	assert ds.equivalenceClass(e).contains(h);

	System.out.print(ds.allNonTrivialEquivalenceClasses());
	System.out.println("; " + ds.allKnownElements());
	assert ds.find(e)==ds.find(c) && ds.find(h)==ds.find(e);
	assert ds.find(b)==ds.find(c) && ds.find(b)!=ds.find(a);

	ds.union(d, f); ds.union(g, d);
	System.out.print(ds.allNonTrivialEquivalenceClasses());
	System.out.println("; " + ds.allKnownElements());
	assert ds.find(d)==ds.find(f) && ds.find(f)==ds.find(g);
	assert ds.find(d)!=ds.find(c) && ds.find(d)!=ds.find(a);
	assert ds.allKnownElements().equals(new LinkedHashSet<String>(Arrays.asList(a, c, d, e, b, h, f, g)));

	ds.union(c, f);
	System.out.println(ds.allNonTrivialEquivalenceClasses());
	assert ds.find(e)==ds.find(f);
	assert ds.find(a)==a;

	Collection<Set<String>> allEquivClasses = ds.allNonTrivialEquivalenceClasses();
	assert allEquivClasses.size() == 1;
	assert DSUtil.getFirst(allEquivClasses).size() == 7;

	System.out.println(ds.allNonTrivialEquivalenceClasses());
	System.err.println("PASSED.");
    }

}
