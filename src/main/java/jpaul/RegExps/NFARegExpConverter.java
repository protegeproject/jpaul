// RegExp.java, created Wed Mar 10 19:27:11 2004 by suhabe
// Copyright (C) 2004 Suhabe Bugrara <suhabe@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.RegExps;

import jpaul.DataStructs.BijMap;
import jpaul.DataStructs.Pair;

class NFARegExpConverter<State,A> {

    public static <State,A> RegExp<A> convert(NFA<State,A> nfa) {
	NFARegExpConverter<State,A> regExpConv = new NFARegExpConverter<State,A>(nfa);
	return regExpConv.regExp;
    }

    private final NFA<State,A> nfa;
    private RegExp<A>[][] gnfa;
    
    private State gnfaStart  = (State) new Object();
    private State gnfaAccept = (State) new Object();

    private NFARegExpConverter(NFA<State,A> nfa) {
        this.nfa = nfa;

	// init map state -> index (it's much easier to use integer
	// indexing into a 2D matrix, instead of maps)
	int count = 0;
        for(State state : nfa.states()) {
	    state2index.put(state, new Integer(count++));
	}
	state2index.put(gnfaStart, new Integer(count++));
	state2index.put(gnfaAccept, new Integer(count++));
        
        initGNFA();

        regExp = compute().simplify();
    }

    private final RegExp<A> regExp;


    private int index(State s) {
	return state2index.get(s).intValue();
    }
    private State state(int index) {
	return state2index.rev().get(new Integer(index));
    }
    private final BijMap<State,Integer> state2index = new BijMap<State,Integer>();

    
    // Initializes the gnfa matrix
    private void initGNFA() {
        int d = state2index.size();
        
        gnfa = new RegExp/*<A>*/[d][d];
           
        //initialize all entries to the None reg exp
        for (int r = 0; r < d; r++)
            for (int c = 0; c < d; c++)
                gnfa[r][c] = new RegExp.None<A>();
        
        // fill in the regular expressions that label the transitions of the GNFA
        for (int r = 0; r < d - 2; r++) {
	    for(Pair<State,A> transition : nfa.getLabeledForwardNavigator().lnext(state(r))) {
		State dst = transition.left;
		A   label = transition.right;
                int c = index(dst);
		// regular expression that describes this transition
		RegExp<A> transRegExp =
		    (label == null) ?
		    // take care with null labels, i.e. epsilon transitions
		    new RegExp.EmptyStr<A>() :
		    // normal, non-null labels
		    new RegExp.Atomic<A>(label);
                gnfa[r][c] = 
		    (new RegExp.Union<A>(gnfa[r][c],
					 transRegExp));
            }
        }
        
        //set up the epsilon transitions for gnfa start and accept states
        int starti  = index(gnfaStart);
        int accepti = index(gnfaAccept);
        
        gnfa[starti][index(nfa.startState())] = new RegExp.EmptyStr<A>();

        for(State s : nfa.acceptStates()) {

	    assert gnfa != null;
	    assert index(s) != -1;
	    assert gnfa[index(s)] != null;

            gnfa[index(s)][accepti] = new RegExp.EmptyStr<A>();
        }
    }

    
    // Removes state from the GNFA using Sipser's alg
    private void removeState(int sIndex) {
        for (int r = sIndex + 1; r < gnfa.length; r++) {
            for (int c = sIndex + 1; c < gnfa.length; c++) {
                gnfa[r][c] = 
		    new RegExp.Union<A>
		    (gnfa[r][c],
		     new RegExp.Concat<A>
		     (new RegExp.Concat<A>
		      (gnfa[r][sIndex],
		       new RegExp.Star<A>(gnfa[sIndex][sIndex])),
		      gnfa[sIndex][c]));
            }
        }
        
        for (int r = 0; r < gnfa.length; r++)
            gnfa[r][sIndex] = new RegExp.None<A>();
        for (int c = 0; c < gnfa.length; c++)
            gnfa[sIndex][c] = new RegExp.None<A>();                        
    }
    

    
    /**
     * Iteratively removes all the states from the gnfa except the
     * gnfaStart and gnfaAccept.  In the end, only the gnfaStart and
     * gnfaAccept remain.
     *
     * @return Final label on transition between gnfaStart and gnfaAccept
     * */
    private RegExp<A> compute() {
        // compute the reg exp
        for (int s = 0; s < gnfa.length - 2; s++) {
            removeState(s);
        }
        
        int starti  = gnfa.length - 2;
        int accepti = gnfa.length - 1;
	assert (starti == index(gnfaStart)) && (accepti == index(gnfaAccept));
        
        // extract the final reg exp which is found on the label from the 
        // start state to the accept state
        RegExp<A> regexp = gnfa[starti][accepti];
        
        return regexp;
    }
    
}
