// RegExp.java, created Wed Mar 10 19:27:11 2004 by suhabe
// Copyright (C) 2004 Suhabe Bugrara <suhabe@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.RegExps;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpaul.Constraints.ConstraintSystem;
import jpaul.Constraints.SolReader;
import jpaul.Constraints.SetConstraints.SVar;
import jpaul.Constraints.SetConstraints.SetConstraints;
import jpaul.DataStructs.BijMap;
import jpaul.DataStructs.MapWithDefault;
import jpaul.DataStructs.Pair;
import jpaul.DataStructs.SetFacts;
import jpaul.Graphs.LabeledDiGraph.LabeledForwardNavigator;
import jpaul.RegExps.NFA.BigState;

/**
 * <code>NFASimplifier</code>
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @author  Suhabe Bugrara - suhabe@alum.mit.edu
 * @version $Id: NFASimplifier.java,v 1.13 2006/03/14 02:29:31 salcianu Exp $ */ 
class NFASimplifier<State,A>  {

    private static class OurBigState<State> implements BigState<State>{
	private final Set<State> states = new LinkedHashSet<State>();
	public Set<State> getStates() { return states; }
	// equals and hashCode are not redefined: we use object
	// identity equality, for speed purposes.
	public String toString() {
	    return states.toString();
	}
    }
    

    public static <State,A> NFA<BigState<State>,A> simplify(NFA<State,A> nfa) {
	return (new NFASimplifier<State,A>(nfa)).simplifiedNFA;
    }
    
    private NFASimplifier(NFA<State,A> nfa) {
	this.nfa = nfa;
	init();
	iterate();
	this.simplifiedNFA = buildFinalNFA();
    }

    // original NFA that we simplify
    private final NFA<State,A> nfa;

    // result (simplified NFA) will be stored here
    private final NFA<BigState<State>,A> simplifiedNFA;

    private int nbStates; // number of states
    private int nbLabels; // number of transition labels

    // s[i] is the component of the ith state
    private int[] s2comp;
    // total number of components (components: 0, 1, ... nbComps-1)
    private int nbComps;


    private Set<Integer> BT[][];

    private void init() {
	// fill in the state -> int index map	
	nbStates = 0;
	for(State state : nfa.states()) {
	    state2index.put(state, new Integer(nbStates++));
	}

	// fill in the label -> int index map
	nbLabels = 0;
	for(A label : allLabels()) {
	    label2index.put(label, new Integer(nbLabels++));
	}
	
	computeBasicTrans();

	// 2 initial components: component 0 contains non-accepting
	// states, component 1 contains the accepting states

	// Small trick: some NFAs may have no accepting state; so, we
	// are not sure we have any accepting state before we see one.
	// In that case, nbComps is set to 2.
	nbComps = 1;
	s2comp = new int[nbStates];
	for(int i = 0; i < nbStates; i++) {
	    boolean accepts = false;
	    for(State state : reachableByEmpty(index2state(i))) {
		if(nfa.acceptStates().contains(state)) {
		    accepts = true;
		    break;
		}
	    }
	    if(accepts) {
		s2comp[i] = 1;
		nbComps = 2;
	    }
	    else {
		s2comp[i] = 0;
	    }
	}
    }

	
    private Set<A> allLabels() {
	Set<A> labels = new LinkedHashSet<A>();
	LabeledForwardNavigator<State,A> lFwdNav = nfa.getLabeledForwardNavigator();
	for(State state : nfa.states()) {
	    for(Pair<State,A> arc : lFwdNav.lnext(state)) {
		labels.add(arc.right);
	    }
	}
	return labels;
    }


    private void iterate() {
	int nbComps2;
	int s2comp2[] = new int[nbStates];

    @SuppressWarnings("unchecked")
	Set<Integer> T[][] = new Set[nbStates][nbLabels];
	for(int i = 0; i < nbStates; i++)
	    for(int j = 0; j < nbLabels; j++)
		T[i][j] = new LinkedHashSet<Integer>();

	while(true) {
	    recomputeTrans(T);

	    nbComps2 = 0;
	    for(int i = 0; i < nbStates; i++) {
		boolean foundComp = false;
		for(int j = 0; j < i; j++) {
		    if(compatible(i, j, T)) {
			s2comp2[i] = s2comp2[j];
			foundComp = true;
		    }
		}
		if(!foundComp) {
		    s2comp2[i] = nbComps2;
		    nbComps2++;
		}
	    }
	    // if we reached the fixed-point, return
	    if(nbComps == nbComps2) return;
	    // otherwise, keep iterating
	    s2comp  = s2comp2;
	    nbComps = nbComps2;
	}
    }

    
    private void recomputeTrans(Set<Integer> T[][]) {
	for(int i = 0; i < nbStates; i++)
	    for(int j = 0; j < nbLabels; j++)
		T[i][j].clear();

	for(int i = 0; i < nbStates; i++) {
	    for(int j = 0; j < nbLabels; j++) {
		for(Integer dst : BT[i][j]) {
		    T[i][j].add(new Integer(s2comp[dst.intValue()]));
		}
	    }
	}
    }


    // return true if i and j are in the same component, and
    // transition in the same components
    private boolean compatible(int i, int j, Set<Integer> T[][]) {
	if(s2comp[i] != s2comp[j]) return false;
	for(int k = 0; k < nbLabels; k++) {
	    if(! T[i][k].equals(T[j][k])) return false;
	}
	return true;
    }


    private int state2index(State state) {
	return state2index.get(state).intValue();
    }
    private State index2state(int index) {
	return state2index.rev().get(new Integer(index));
    }
    private final BijMap<State,Integer> state2index = new BijMap<State,Integer>();

    private int label2index(A label) {
	return label2index.get(label).intValue();
    }
    private final Map<A,Integer> label2index = new LinkedHashMap<A,Integer>();


    private NFA<BigState<State>,A> buildFinalNFA() {
	// 1. build the big states (the states of the resulting NFA)
	// comps[i] is the set of states that appear in the big state #i
    @SuppressWarnings("unchecked")
	final BigState<State>[] comps = new BigState/*<State>*/[nbComps];
	for(State state : nfa.states()) {
	    int compIndex = compIndex(state);
	    if(comps[compIndex] == null) {
		comps[compIndex] = new OurBigState<State>();
	    }
	    comps[compIndex].getStates().add(state);
	}

	// 2. find the starting state
	BigState<State> startComp = comps[compIndex(nfa.startState())];

	// 3. build the set of accepting big states
	Set<BigState<State>> acceptComps = new LinkedHashSet<BigState<State>>();
	for(int i = 0; i < nbComps; i++) {
	    BigState<State> bigState = comps[i];
	    assert bigState != null;
	    for(State state : bigState.getStates()) {
		if(nfa.acceptStates().contains(state)) {
		    acceptComps.add(bigState);
		    break;
		}
	    }
	}

	// 4. build the map bigStates -> arcs

	// First, we build a map from big states to sets of arcs: we
	// use sets instead of lists in order to make sure we avoid
	// duplicates.
	Map<BigState<State>,Set<Pair<BigState<State>,A>>> comp2arcs = 
	    new MapWithDefault<BigState<State>,Set<Pair<BigState<State>,A>>>
	    (SetFacts.<Pair<BigState<State>,A>>hash(), true);

	for(State state : nfa.states()) {
	    BigState<State> bigState = comps[compIndex(state)];

	    Set<Pair<BigState<State>,A>> arcs = comp2arcs.get(bigState);
	    
	    LabeledForwardNavigator<State,A> lFwdNav = nfa.getLabeledForwardNavigator();
	    for(Pair<State,A> arc : lFwdNav.lnext(state)) {
		arcs.add(new Pair<BigState<State>,A>
			 (comps[compIndex(arc.left)],
			  arc.right));
	    }
	}

	// Next, we pass to a map from big states to lists of arcs.
	final Map<BigState<State>,List<Pair<BigState<State>,A>>> bigState2arcList = 
	    new LinkedHashMap<BigState<State>, List<Pair<BigState<State>,A>>>();
	for(Map.Entry<BigState<State>,Set<Pair<BigState<State>,A>>> entry : comp2arcs.entrySet()) {
	    bigState2arcList.put(entry.getKey(),
				 new LinkedList<Pair<BigState<State>,A>>(entry.getValue()));
	}

	// 5. construct and return the simplified NFA
	return NFA.<BigState<State>,A>create
	    (startComp,
	     acceptComps,
	     new LabeledForwardNavigator<BigState<State>,A>() {
		public List<Pair<BigState<State>,A>> lnext(BigState<State> comp) {
		    return bigState2arcList.get(comp);
		}
	    });
    }

    // return the index of the big state (from the simplified NFA) that contains state
    private int compIndex(State state) {
	return s2comp[state2index(state)];
    }



    private void computeBasicTrans() {
	findEmptyTrans();

	BT = new Set/*<Integer>*/[nbStates][nbLabels];
	for(int i = 0; i < nbStates; i++) {
	    for(int j = 0; j < nbLabels; j++) {
		BT[i][j] = new LinkedHashSet<Integer>();
	    }
	}

	LabeledForwardNavigator<State,A> lFwdNav = nfa.getLabeledForwardNavigator();
	for(State state : nfa.states()) {
	    int i = state2index(state);
	    for(State state2 : reachableByEmpty(state)) {
		for(Pair<State,A> arc : lFwdNav.lnext(state2)) {
		    State dst = arc.left;
		    A   label = arc.right;
		    int j = label2index(label);
		    BT[i][j].add(new Integer(state2index(dst)));
		}
	    }
	}
    }


    private void findEmptyTrans() {
	SetConstraints<State> sc = new SetConstraints<State>();

	LabeledForwardNavigator<State,A> lFwdNav = nfa.getLabeledForwardNavigator();
	for(State state : nfa.states()) {
	    SVar<State> svarState = state2svar(state);
	    sc.addCtSource(Collections.<State>singleton(state), svarState);
	    for(Pair<State,A> transition : lFwdNav.lnext(state)) {
		State dest = transition.left;
		A    label = transition.right;
		// empty string transition
		if(label == null) {
		    sc.addInclusion(state2svar(dest), svarState);
		}
	    }
	}

	solReader = (new ConstraintSystem<SVar<State>,Set<State>>(sc)).solve();
    }

    private SVar<State> state2svar(State state) {
	SVar<State> svar = state2svar.get(state);
	if(svar == null) {
	    state2svar.put(state, svar = new SVar<State>());
	}
	return svar;
    }
    private final Map<State,SVar<State>> state2svar = new LinkedHashMap<State,SVar<State>>();

    private SolReader<SVar<State>,Set<State>> solReader;

    private Set<State> reachableByEmpty(State state) {
	return solReader.get(state2svar(state));
    }

}
