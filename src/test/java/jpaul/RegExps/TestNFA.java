// TestNFA.java, created Wed Mar  10 19:31:17 2004 by suhabe
// Copyright (C) 2004 Suhabe Bugrara <suhabe@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.RegExps;

/**
 * <code>NFATest</code> contains regression tests.
 * 
 * @author  Suhabe Bugrara <suhabe@alum.mit.edu>
 * @author  Alex Salcianu <salcianu@alum.mit.edu>
 * @version $Id: TestNFA.java,v 1.7 2006/03/14 02:29:32 salcianu Exp $
 */
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Arrays;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import jpaul.DataStructs.*;

import jpaul.Graphs.LabeledDiGraph.LabeledForwardNavigator;

public class TestNFA extends TestCase {
    
    public static Test suite() {
	return new TestSuite(TestNFA.class);
    }
    
    public void test1() {
	Object[] q = makeStates(2);
	Object q0 = q[0];
	Object q1 = q[1];

	Map<Object,List<Pair<Object,Integer>>> node2arcs = makeNode2Arcs();
	addTrans(node2arcs, q0, new Integer(0), q1);
	addTrans(node2arcs, q1, new Integer(0), q0);

	NFA<Object,Integer> nfa = TestNFA.makeNFA(q0,
			  Collections.singleton(q0),
			  node2arcs);

        System.out.println("Reg exp: " + nfa.toRegExp());
	System.out.println("Simplified reg exp: " + nfa.simplify().toRegExp());
        System.out.println();
    }

    

    //sipser pg 75
    public void test2() {
	Object[] q = makeStates(2);
        Object q1 = q[0];
        Object q2 = q[1];

	Map<Object,List<Pair<Object,String>>> node2arcs = makeNode2Arcs();

	addTrans(node2arcs, q1, "b", q2);
	addTrans(node2arcs, q1, "a", q1);
	addTrans(node2arcs, q2, "a", q2);
	addTrans(node2arcs, q2, "b", q2);

        NFA<Object,String> nfa = makeNFA(q1,
			  Collections.singleton(q2),
			  node2arcs);
        
        System.out.println("Reg exp: " + nfa.toRegExp());
	System.out.println("Simplified reg exp: " + nfa.simplify().toRegExp());
        System.out.println();
    }
    


    //sipser pg 76
    public void test3() {
	Object[] q = makeStates(3);
        Object q1 = q[0];
        Object q2 = q[1];
        Object q3 = q[2];
        
	Map<Object,List<Pair<Object,Object>>> node2arcs = makeNode2Arcs();
	addTrans(node2arcs, q1, "a", q2);
	addTrans(node2arcs, q1, "b", q3);
	addTrans(node2arcs, q2, "b", q2);
	addTrans(node2arcs, q2, "a", q1);
	addTrans(node2arcs, q3, "b", q1);
	addTrans(node2arcs, q3, "a", q2);
        
        HashSet<Object> acceptStates = new HashSet<Object>();
        acceptStates.add(q2);
        acceptStates.add(q3);
        
        NFA<Object,Object> nfa = makeNFA(q1,
			  new HashSet<Object>(Arrays.asList(q2, q3)),
			  node2arcs);

        System.out.println("Reg exp: " + nfa.toRegExp());
	System.out.println("Simplified reg exp: " + nfa.simplify().toRegExp());
        System.out.println();
    }



    // [AS]: just making sure that once we implement NFA minimization,
    // our output will indeed look better!
    public void test4() {
	Object[] q = makeStates(6);

	Map<Object,List<Pair<Object,String>>> node2arcs = TestNFA.<Object,String>makeNode2Arcs();

	addTrans(node2arcs, q[0], "this", q[1]);
	addTrans(node2arcs, q[1], "head", q[2]);
	addTrans(node2arcs, q[2], "next", q[5]);
	addTrans(node2arcs, q[2], "data", q[3]);
	addTrans(node2arcs, q[5], "next", q[5]);
	addTrans(node2arcs, q[5], "data", q[3]);
	addTrans(node2arcs, q[3], "x", q[4]);
	addTrans(node2arcs, q[3], "y", q[4]);

	NFA<Object,String> nfa = makeNFA(q[0],
			  Collections.singleton(q[4]),
			  node2arcs);
	
	System.out.println("NFA = " + nfa);
        System.out.println("Reg exp: " + nfa.toRegExp());
	NFA<NFA.BigState<NFA.BigState<NFA.BigState<Object>>>,String> simplifiedNFA = nfa.simplify().simplify().simplify();
	System.out.println("Simplified NFA = " + simplifiedNFA);
	System.out.println("Simplified reg exp: " + simplifiedNFA.toRegExp());
        System.out.println();
    }


    private static <State,Label> Map<State,List<Pair<State,Label>>> makeNode2Arcs() {
	return
	    new MapWithDefault<State,List<Pair<State,Label>>>
	    (new Factory<List<Pair<State,Label>>>() {
		public List<Pair<State,Label>> create() {
		    return new LinkedList<Pair<State,Label>>();
		}
		public List<Pair<State,Label>> create(List<Pair<State,Label>> l) {
		    throw new Error();
		}
	    },
		true);
    }


    // [AS]: returns an array of nbStates
    private Object[] makeStates(int nbStates) {
	Object[] states = new Object[nbStates];
	for(int i = 0; i < nbStates; i++)
	    states[i] = new String("s" + i);
	return states;
    }

    // Adds the edge/transition <s1, label, s2>
    private static <State,A> void addTrans(Map<State,List<Pair<State,A>>> node2edges,
					   State s1, A label, State s2) {
	List<Pair<State,A>> edges = node2edges.get(s1);
	edges.add(new Pair<State,A>(s2, label));
    }


    private static <State,A> NFA<State,A> makeNFA
	(State begin, Set<State> acceptStates,
	 final Map<State,List<Pair<State,A>>> state2edges) {

	return
	    NFA.<State,A>create
	    (begin,
	     acceptStates,
	     new LabeledForwardNavigator<State,A>() {
		public List<Pair<State,A>> lnext(State s) {
		    return state2edges.get(s);
		}
	    });
    }

}
