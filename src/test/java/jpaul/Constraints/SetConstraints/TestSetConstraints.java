// TestSetConstraints.java, created Mon Jun 20 14:29:58 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Constraints.SetConstraints;

import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;

import jpaul.Constraints.ConstraintSystem;
import jpaul.Constraints.Constraint;
import jpaul.Constraints.SolReader;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Var;

import jpaul.Misc.Predicate;

/**
 * <code>TestSetConstraints</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TestSetConstraints.java,v 1.6 2006/03/14 02:29:32 salcianu Exp $
 */
public class TestSetConstraints extends TestCase {
    
    public static Test suite() {
	ConstraintSystem.DEBUG = true;
	return new TestSuite(TestSetConstraints.class);
    }


    private static class FunnyIncrement extends Constraint<SVar<Integer>,Set<Integer>> {
	public FunnyIncrement(SVar<Integer> v) {
	    this.v   = v;
	    this.in  = Collections.<SVar<Integer>>singleton(v);
	    this.out = Collections.<SVar<Integer>>singleton(v);
	}

	public int cost() { return Constraint.HIGH_COST; }
	
	public final SVar<Integer> v;
	
	private final Collection<SVar<Integer>> in;
	private final Collection<SVar<Integer>> out;
	
	public Collection<SVar<Integer>> in()  { return this.in;  }
	public Collection<SVar<Integer>> out() { return this.out; }
	
	public void action(SolAccessor<SVar<Integer>,Set<Integer>> sa) {
	    Set<Integer> vi = sa.get(v);
	    if((vi == null) || vi.size() < 10) {
		int k = findMax(vi);
		sa.join(v, Collections.<Integer>singleton(new Integer(k+1)));
	    }
	}
	
	private int findMax(Set<Integer> si) {
	    if(si == null) return 0;
	    int max = 0;
	    for(Integer i : si) {
		if(i.intValue() > max) {
		    max = i.intValue();
		}
	    }
	    return max;
	}

	public String toString() {
	    return "funnyIncrement(" + v + ")";
	}
    }
	
    private static final SVar<Integer> si1 = new SVar<Integer>();
    private static final SVar<Integer> si2 = new SVar<Integer>();
    private static final SVar<Integer> si3 = new SVar<Integer>();
    private static final SVar<Integer> si4 = new SVar<Integer>();
    private static final SVar<Integer> si5 = new SVar<Integer>();
    private static final SVar<Integer> si6 = new SVar<Integer>();
    private static final SVar<Integer> si7 = new SVar<Integer>();


    public void test0() {
	SetConstraints<Integer> sc = new SetConstraints<Integer>();

	sc.addCtSource(Arrays.asList(new Integer(1), new Integer(2)), si1);
	sc.addInclusion(si1, si2);
	sc.addInclusion(si2, si3);
	sc.addInclusion(si3, si4);
	sc.addInclusion(si1, si3);
	sc.addInclusion(si1, si4);

	TestSetConstraints.<SVar<Integer>,Set<Integer>>solve(sc);
    }


    public void test1() {
	SetConstraints<Integer> sc = new SetConstraints<Integer>();

	sc.addCtSource(Arrays.asList(new Integer(1), new Integer(2)), si1);
	sc.addInclusion(si1, si2);
	sc.addInclusion(si2, si3);
	sc.addInclusion(si3, si4);
	sc.addInclusion(si4, si2);

	TestSetConstraints.<SVar<Integer>,Set<Integer>>solve(sc);
    }


    public void test2() {
	SetConstraints<Integer> sc = new SetConstraints<Integer>();

	sc.addCtSource(Arrays.asList(new Integer(1), new Integer(2)), si1);
	sc.addInclusion(si1, si2);
	sc.addInclusion(si2, si3);
	sc.addInclusion(si3, si4);
	sc.addInclusion(si4, si2);
	sc.addCtSource(Arrays.asList(new Integer(3)), si3);

	TestSetConstraints.<SVar<Integer>,Set<Integer>>solve(sc);
    }


    public void test3() {
	SetConstraints<Integer> sc = new SetConstraints<Integer>();

	sc.addCtSource(Arrays.asList(new Integer(1), new Integer(2)), si1);
	sc.addInclusion(si1, si2);
	sc.addInclusion(si2, si3);
	sc.addInclusion(si3, si4);
	sc.addInclusion(si4, si2);
	sc.addCtSource(Arrays.asList(new Integer(3)), si3);
	sc.add(new FunnyIncrement(si3));

	TestSetConstraints.<SVar<Integer>,Set<Integer>>solve(sc);
    }


    public void test4() {
	SetConstraints<Integer> sc = new SetConstraints<Integer>();

	sc.addCtSource(Arrays.asList(new Integer(1), new Integer(2)), si1);
	sc.addInclusion(si1, si2);
	sc.addInclusion(si2, si3);
	sc.addInclusion(si3, si4);
	sc.addInclusion(si4, si2);
	sc.addCtSource(Arrays.asList(new Integer(3)), si3);
	sc.add(new FunnyIncrement(si3));
	sc.add
	    (new FilterConstraint<Integer>
	     (si3,
	      new Predicate<Integer>() {
		public boolean check(Integer i) { return i.intValue() % 2 == 0; }
		public String toString() { return "even"; }
	      },
	      si5));
	sc.add
	    (new CtDiffConstraint<Integer>
	     (si3,
	      Arrays.asList(new Integer(0), new Integer(1), new Integer(2), 
                      new Integer(3), new Integer(4)),
	      si5));
	sc.addCtSource(Arrays.asList(new Integer(0), new Integer(1), 
                new Integer(2), new Integer(3)), si6);
	sc.add(new IntersectConstraint<Integer>(si5, si6, si7));

	TestSetConstraints.<SVar<Integer>,Set<Integer>>solve(sc);
    }


    private static <V extends Var<Info>, Info> void solve(Collection<Constraint<V,Info>> sc) {
	//System.out.println("Original constraints: " + sc);

	ConstraintSystem<V,Info> sys = new ConstraintSystem<V,Info>(sc);

	sys.debugPrintSolverStructs(System.out);

	SolReader<V,Info> sol = sys.solve();

	System.out.print("Solution:\n" + sol);

	SolAccessor<V,Info> ver = new SolVerifier<V,Info>(sol);
	for(Constraint<V,Info> c : sc) {
	    System.out.print("Verifying constraint " + c + " ... ");
	    c.action(ver);
	    System.out.println("ok!");
	}
	System.out.println("Solution seems to be correct!\n");
    }


    private static final class SolVerifier<V extends Var<Info>, Info> implements SolAccessor<V,Info> {
	public SolVerifier(SolReader<V,Info> sr) { this.sr = sr; }
	
	private SolReader<V,Info> sr;

	public Info get(V v) { return sr.get(v); }

	public void join(V v, Info delta) {
	    Info old = this.get(v);
	    // special case: no previous value for v
	    if(old == null) {
		if(delta == null) return;
	    }
	    else {
		if(!v.join(old, delta)) return;
	    }

	    // if this point is reached, a change was detected ...
	    Assert.assertTrue
		("wrong! action causes info to grow!",
		 false);
	}
    }

}
