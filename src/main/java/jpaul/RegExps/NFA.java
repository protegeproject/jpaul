// NFA.java, created Tue Mar  9 13:07:35 2004 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.RegExps;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jpaul.DataStructs.DSUtil;
import jpaul.Graphs.LabeledDiGraph;
import jpaul.Misc.Predicate;

/**
 * <code>NFA</code> models a Non-deterministic Finite Automaton.  This
 * class is generic over the type <code>State</code> of the states and
 * over the type <code>A</code> of the input symbols.
 *
 * <p>An <code>NFA</code> is a special case of labeled digraph.  As
 * such, it is defined in a similar way, using a (labeled) arc
 * navigator.  To avoid unnecessary complications, we assume that one
 * a navigator is used to define an <code>NFA</code>, the information
 * it returns never changes.  This is almost always the case in
 * practice; if you want to play with dynamically changing
 * <code>NFA</code>s, then you have to manually scrap your old
 * <code>NFA</code> and build a new one every time a change happens.
 * Attempting to use this class with a dynamically changing navigator
 * will result in unspecified (read <i>buggy</i>) behaviour.
 *
 * <p>The "non-determinism" means that we can have multiple
 * transitions with the same label coming out of a state; we can also
 * have <code>null</code>-labeled transitions, modeling
 * "epsilon"-transitions from classic NFA theory (transitions that
 * don't consume any input symbol).
 *
 * <p>To define an NFA, the programmer should subclass this class and
 * implement a few methods: {@link #startState()}, {@link
 * #_acceptStates()}, and one of {@link #getLabeledForwardNavigator()} or
 * {@link #getLabeledBiDiNavigator()}.  The states of the resulting NFA are
 * those states that are reachable from the starting state (as for any
 * <code>LabeledDiGraph</code>); the transitions are given by the (labeled)
 * navigator.
 *
 * <p>Excellent material on NFAs can be found in "Introduction to the
 * Theory of Computation" by Michael Sipser.
 *
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: NFA.java,v 1.17 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class NFA<State,A> extends LabeledDiGraph<State,A> {

    /** Returns the starting state of this NFA. */
    public abstract State startState();

    /** Returns the accepting states of this NFA.  This method returns
        all states returned by {@link #_acceptStates()}, except those
        that are unreachable from the starting state.  */
    public final Collection<State> acceptStates() {
	if(acceptStates == null) {
	    final Collection<State> states = this.states();
	    acceptStates = 
		DSUtil.filterColl(_acceptStates(),
				  new Predicate<State>() {
				      public boolean check(State state) {
					  return states.contains(state);
				      }
				  },
				  new LinkedHashSet<State>());
	}
	return acceptStates;
    }
    private Collection<State> acceptStates = null;


    /** Needs to be implemented by subclasses to return the accepting
        states.  NFA algorithms use {@link #acceptStates()} that take
        into account only those accepting states that are reachable
        from the starting state; it is OK for this method to return
        unreachable states.

	@see #acceptStates() */
    protected abstract Collection<State> _acceptStates();

    /** Returns all states from this NFA.  */
    public Collection<State> states() { return vertices(); }
    
    /** Returns a singleton consisting of the start state, the single
        root of this NFA (a particular case of
        <code>LabeledDiGraph</code>). */
    public final Collection<State> getRoots() {
	return Collections.<State>singleton(startState());
    }

    
    /** Constructs a <code>NFA</code>.  This constructor is meant to
        be invoked by the constructors of the subclasses: it just
        informs the superclass <code>LabeledDiGraph</code> constructor that
        caching is OK; the information provided by the
        <code>LabeledForwardNavigator</code> is expected to stay unchanged
        (see top-level comments for this class).  */
    protected NFA() {
	super(true);
    }


    /** Factors out some implementation details for NFAs that are
        defined by giving a starting state and a collection of
        accepting states.  Subclasses shosuld take care of defining
        the transitions, i.e., the (labeled) navigator. */
    private static abstract class AbstrSimpleNFA<State,A> extends NFA<State,A> {
	public AbstrSimpleNFA(State startState, Collection<State> acceptStates) {
	    this.startState = startState;
	    this._acceptStates = acceptStates;
	}
	private final State startState;
	private final Collection<State> _acceptStates;
	public State startState() { return startState; }
	public Collection<State> _acceptStates() { return _acceptStates; }	
    }



    /** Returns a freshly constructed <code>NFA</code>.

	@param startState Starting state for the automaton.  

	@param acceptStates Accepting states for the automaton.  An
	input string (=list) of <code>A</code>s is accepted iff it can
	lead the <code>NFA</code> from the starting state into one of
	these states.

	@param lFwdNav Forward navigator that defines the outgoing
	transitions for each automaton state.  We can have multiple
	transitions with the same label out of a state; we can also
	have <code>null</code>-labeled transitions, modeling
	"epsilon"-transitions from classic NFA theory (transitions
	that don't consume any input symbol).  If necessary, the
	<code>NFA</code> will compute a full, bi-directional
	navigator. */
    public static <State,A> NFA<State,A> create(final State startState,
						final Collection<State> acceptStates,
						final LabeledForwardNavigator<State,A> lFwdNav) {
	return new AbstrSimpleNFA<State,A>(startState, acceptStates) {
	    public LabeledForwardNavigator<State,A> getLabeledForwardNavigator() {
		return lFwdNav;
	    }
	};
    }


    /** Returns a freshly constructed <code>NFA</code>.  Like {@link
	#create(java.lang.Object,java.util.Collection,jpaul.Graphs.LabeledDiGraph.LabeledForwardNavigator)
	other} <code>nfa</code> static method, but a full navigator is
	passed.  Therefore, the <code>getLabeledBiDiNavigator()</code> of the
	returned NFA (if invoked) does not need to compute one.*/
    public static <State,A> NFA<State,A> create(final State startState,
						final Collection<State> acceptStates,
						final LabeledBiDiNavigator<State,A> lNav) {
	return new AbstrSimpleNFA<State,A>(startState, acceptStates) {
	    public LabeledBiDiNavigator<State,A> getLabeledBiDiNavigator() {
		return lNav;
	    }
	};
    }



    /** Returns a regular expression over the alphabet <code>A</code>,
        representing all strings accepted by this NFA.  Complexity:
        cubic in the number of states of this NFA. */
    public RegExp<A> toRegExp() {
        return NFARegExpConverter.<State,A>convert(this);
    }

    /** Returns a simplified <code>NFA</code> that accepts the same
        strings as this NFA.  "Simplified" means equal or smaller
        number of states.  The states of the simplified automaton
        correspond to disjoint sets of states from this automaton: we
        place two states in the same big state iff for any input
        string starting in any of the two states leads to the same
        acceptance outcome.  Complexity: polynomial (cubic?) in the
        size of the original NFA.  */
    public NFA<BigState<State>,A> simplify() {
	return NFASimplifier.<State,A>simplify(this);
    }

    /** Returns a pretty rough text representation for debugging
        pruposes. */
    public String toString() {
	return 
	    "{\nstart  = " + startState() + 
	    "\naccept = " + acceptStates() + 
	    "\nedges = " + super.toString() + "}";
    }


    /** Certain <code>NFA</code> transformations (ex: {@link
        NFA#simplify}) produce <code>NFA</code>s whose states are sets of
        original states.  Using <code>Set&lt;State&gt;</code> for
        those states has the inconvenient that using sets as maps
        indices (for example, in a map from big states to their arcs)
        is VERY expensive.  Set equality is not what we really want
        here, especially because the implementation of
        <code>simplify</code> is fine with the much faster object
        identity.  Instead of defining a <code>Set</code> that breaks
        the <code>Set</code> specification for <code>equals</code>, we
        have a special interface, that each optimization can implement
        to use the desired equality relation.  */
    public static interface BigState<State> {
	/** Returns the set of states that compose this big state.  */
	Set<State> getStates();
    }

}
