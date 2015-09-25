// RegExp.java, created Mon Mar  8 19:27:11 2004 by salcianu
// Copyright (C) 2004 Suhabe Bugrara - suhabe@alum.mit.edu, and
//                    Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.RegExps;

import java.io.Serializable;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ForwardNavigator;

import jpaul.DataStructs.DSUtil;

import jpaul.Misc.IdentityWrapper;
import jpaul.Misc.Action;

/**
 * <code>RegExp</code> models a regular expression.  A regular
 * expression describes a set of strings (=lists) made of symbols of
 * some type <code>A</code>.
 *
 * <p>Several inner classes model the different kinds of regular
 * expressions.  Given a regular expression, to do processing on a
 * case by case basis, one should use the visitor pattern; see {@link
 * RegExp.Visitor RegExp.Visitor}.  Alternatives (e.g.,
 * <code>instanceof</code> tests) are not elegant and should be
 * avoided.
 *
 * @author  Suhabe Bugrara - suhabe@alum.mit.edu
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: RegExp.java,v 1.13 2006/03/14 02:29:31 salcianu Exp $ */
public abstract class RegExp<A> implements Cloneable, Serializable {

    /** Method for the visitor pattern.  This method will dynamically
        select and execute the <code>visit</code> method of the
        <code>visitor</code> that corresponds to the dynamic type of
        the regular expression.  E.g., in the case of a regular
        expression that is a <code>RegExp.Concat</code> object,
        <code>accept</code> executes the code of
        <code>visit(RegExp.Concat)</code>.  */
    public <Res> Res accept(Visitor<A,Res> visitor) {
        return visitor.visit(this);
    }

    /** Computes the hashcode for this object.  Dummy recursive
        computation of the hashcode can be TREMENDOUSLY inneficient:
        several transformations (ex: the conversion from NFA to
        regular expressions) can produce in polynomial time regular
        expressions of implicit exponential size (due to sharing:
        e.g., both left and right parts of a
        <code>RegExp.Concat</code> are the same object).  To prevent
        this efficiency problems, <code>hashCode</code> uses caching
        (perfectly safe for immutable objects like the current
        <code>RegExp</code>s).  The hashcode is computed only the
        first time, by calling <code>_hashCode</code> that performs
        the real computation.  <b>Normally, subclasses should
        implement <code>_hashCode</code> without touching
        <code>hashCode</code>.  */
    public int hashCode() {
	// Cache hashCode: instances of this class are supposed to be
	// immutable objects, so hashCode should be computed only
	// once; this leads to TREMENDOUS speedups.
	if(cachedHash == 0)
	    cachedHash = _hashCode();
	return cachedHash;
    }
    private int cachedHash;


    /** Does the real work behing <code>hashCode</code>.

	@see #hashCode() */
    protected abstract int _hashCode();


    /** Regular expression that does not match any string.  Note that
        <code>None</code> is different from <code>EmptyStr</code>:
        <code>EmptyStr</code> matches the empty string. */
    public static class None<A> extends RegExp<A> {
	private static final long serialVersionUID = -8994356095372361249L;

	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}
	
	public boolean equals(Object o) {
	    if(o == null) return false;
	    return (o instanceof None/*<A>*/);
	}
	
	// why 1? just a random choice :)
	protected int _hashCode() { return 2; }
	
	public String toString() { return "~"; }
    }


    /** Regular expressions that matches only the empty string. */
    public static class EmptyStr<A> extends RegExp<A> {
    	private static final long serialVersionUID = -5732092253081025302L;

	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}
	
	public boolean equals(Object o) {
	    if(o == null) return false;
	    return (o instanceof EmptyStr/*<A>*/);
	}
	
	// why 2? I don't know, but 2 != 1 (the hashcode of None) :)
	protected int _hashCode() { return 1; }
	
	public String toString() { return "\\eps{}"; }
    }

    


    /** Regular expression that matches only the 1-length string
        consisting of exactly one specific symbol.  */
    public static class Atomic<A> extends RegExp<A> {
	private static final long serialVersionUID = 5971092090139758737L;

	/** Creates an <code>Atomic</code> regular expression that
            matches only the 1-length string consisting of the symbol
            <code>a</code>. */
	public Atomic(A a) { this.a = a; }
	
	public final A a;
	
	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}
	
	public boolean equals(Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if(!(o instanceof Atomic/*<A>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;
	    
	    return this.a.equals(((Atomic) o).a);
	}
	
	protected int _hashCode() { return a.hashCode(); }
	public String toString()  { return a.toString(); }
    }
    

    /** Regular expression produced by concatenating two regular
        expressions. */
    public static class Concat<A> extends RegExp<A> {

	private static final long serialVersionUID = 5902457822685909373L;

	/** Creates a <code>Concat</code> regular expression that
            matches any string of <code>A</code>s consisting of a
            string matched by <code>left</code> followed by a string
            matched by <code>right</code>. */
	public Concat(RegExp<A> left, RegExp<A> right) {
	    assert (left != null) && (right != null);
	    this.left  = left;
	    this.right = right;
	}

	public final RegExp<A> left;
	public final RegExp<A> right;
        
	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}

	public boolean equals(Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if (!(o instanceof Concat/*<A>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;
	    
	    Concat<?> cre = (Concat) o;
	    return cre.left.equals(left) && cre.right.equals(right);
	}
    
	protected int _hashCode() {
	    return 19 * left.hashCode() + 23 * right.hashCode();
	}
	
	public String toString() {
	    StringBuffer buff = new StringBuffer();	    
	    addOperand(buff, left);
	    buff.append(".");
	    addOperand(buff, right);	    
	    return buff.toString();
	}
	
	private void addOperand(StringBuffer buff, RegExp<A> re) {
	    if(re instanceof Union/*<A>*/)
		buff.append("(").append(re).append(")");
	    else
		buff.append(re);
	}


	/** Returns all transitive terms of this <code>Concat</code>.
            More details: this <code>Concat</code> regexp may be the
            root of a subtree of <code>Concat</code> internal nodes,
            modeling the concatenation of several regexps: e.g.,
            <code>a.((b.c).d)</code>.  This method returns all the
            leaves of this subtree, from left to right; for the
            previous example, it returns <code>[a,b,c,d]</code>.  */
	public List<RegExp<A>> allTransTerms() {
	    final List<RegExp<A>> terms = new LinkedList<RegExp<A>>();
	    DiGraph.<RegExp<A>>diGraph(Collections.<RegExp<A>>singleton(this),
				       RegExp.<A>concatNav()).
		dfs(new Action<RegExp<A>>() {
		    public void action(RegExp<A> re) {
			if(re instanceof Concat/*<A>*/) return;
			terms.add(re);
		    }
		}, null);
	    return terms;
	}

    }


    /** Builds a regular expression equivalent to the concatenation of
        several (possibly more than 2) terms.  In the normal case,
        this method applies the binary <code>Concat</code> constructor
        repeatedly.  If the list of terms contains a single term, the
        method returns that term (so, the resulting
        <code>RegExp</code> is not always an instance of
        <code>Concat</code>).  If the list is empty, the method
        returns the <code>EmptyStr</code> regexp. */
    public static <A> RegExp<A> buildConcat(List<RegExp<A>> concatTerms) {
	if(concatTerms.isEmpty())
	    return new EmptyStr<A>();

	Iterator<RegExp<A>> it = concatTerms.iterator();
	RegExp<A> re = it.next();
	while(it.hasNext()) {
	    re = new Concat<A>(re, it.next());
	}
	return re;
    }
    

    /** Builds a regular expression equivalent to the union of several
        (possibly more than 2) terms.  In the normal case, this method
        applies the binary <code>Union</code> constructor repeatedly.
        If the set of terms contains a single distinct term, the
        method returns that term (so, the resulting
        <code>RegExp</code> is not always an instance of
        <code>Union</code>).  If the set of terms is empty, the method
        returns <code>None</code>. */
    public static <A> RegExp<A> buildUnion(Set<RegExp<A>> unionTerms) {
	if(unionTerms.isEmpty())
	    return new None<A>();

	Iterator<RegExp<A>> it = unionTerms.iterator();
	RegExp<A> re = it.next();
	while(it.hasNext()) {
	    re = new Union<A>(re, it.next());
	}
	return re;
    }


    

    /** The regular expression that matches any string matched by at
        least one of two specific regular expression. */
    public static class Union<A> extends RegExp<A> {

	private static final long serialVersionUID = 2695604868756054776L;

	/** Creates a <code>Union</code> regular expression that
            matches any string that is matched by (at least one of)
            <code>left</code> and <code>right</code>. */
	public Union(RegExp<A> left, RegExp<A> right) {
	    assert (left != null) && (right != null);
	    this.left  = left;
	    this.right = right;
	}
	
	public final RegExp<A> left;
	public final RegExp<A> right;
    
	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}
	
	public boolean equals(Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if(!(o instanceof Union/*<A>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;
	    
	    Union<?> ure = (Union) o;
	    return 
		(ure.left.equals(left) && ure.right.equals (right)) ||
		(ure.right.equals(left) && ure.left.equals (right));
	}
    
	protected int _hashCode() {
	    return 19 * left.hashCode() + 23 * right.hashCode();
	}
    
	public String toString() {
	    return left.toString() + "|" + right.toString();
	}


	/** Returns all transitive terms of this <code>Union</code>.
            More details: this <code>Union</code> regexp may be the
            root of a subtree of <code>Union</code> internal nodes,
            modeling the union of several regexps: e.g.,
            <code>a|((b|c)|d)</code>.  This method returns all the
            leaves of this subtree; for the previous example, it
            returns <code>[a,b,c,d]</code>.  */
	public List<RegExp<A>> allTransTerms() {
	    final List<RegExp<A>> terms = new LinkedList<RegExp<A>>();
	    DiGraph.<RegExp<A>>diGraph(Collections.<RegExp<A>>singleton(this),
				       RegExp.<A>unionNav()).
		dfs(new Action<RegExp<A>>() {
		    public void action(RegExp<A> re) {
			if(re instanceof Union/*<A>*/) return;
			terms.add(re);
		    }
		}, null);
	    return terms;
	}

    }


    // forward navigator through the sons of Union nodes.
    private static <A> ForwardNavigator<RegExp<A>> unionNav() {
	return new ForwardNavigator<RegExp<A>>() {
	    private Visitor<A,List<RegExp<A>>> unionVis = new Visitor<A,List<RegExp<A>>>() {
		public List<RegExp<A>> visit(Union<A> ure) {
		    return Arrays.<RegExp<A>>asList(ure.left, ure.right);
		}
		public List<RegExp<A>> visit(RegExp<A> re) {
		    return Collections.<RegExp<A>>emptyList();
		}
	    };
	    public List<RegExp<A>> next(RegExp<A> re) {
		return re.accept(unionVis);
	    }
	};
    }

    // forward navigator through the sons of Concat nodes.
    private static <A> ForwardNavigator<RegExp<A>> concatNav() {
	return new ForwardNavigator<RegExp<A>>() {
	    private Visitor<A,List<RegExp<A>>> concatVis = new Visitor<A,List<RegExp<A>>>() {
		public List<RegExp<A>> visit(Concat<A> ure) {
		    return Arrays.<RegExp<A>>asList(ure.left, ure.right);
		}
		public List<RegExp<A>> visit(RegExp<A> re) {
		    return Collections.<RegExp<A>>emptyList();
		}
	    };
	    public List<RegExp<A>> next(RegExp<A> re) {
		return re.accept(concatVis);
	    }
	};
    }


    /** The star regular expression.  */
    public static class Star<A> extends RegExp<A> {

	private static final long serialVersionUID = -5321883108062971275L;

	/** Creates a <code>RegExp</code> that matches any string that
            is obtained by the concatenation of a finite number of
            strings (possibly none), each matched by the regular
            expression <code>regExp</code>.  Any <code>Star</code>
            regular expression accepts at least the empty string.  */
	public Star(RegExp<A> regExp) {
	    assert regExp != null;
	    this.starred = regExp;
	}
	
	public final RegExp<A> starred;
	
	public <Res> Res accept(Visitor<A,Res> visitor) {
	    return visitor.visit(this);
	}
	
	public boolean equals (Object o) {
	    if(o == null) return false;
	    if(o == this) return true;
	    if(!(o instanceof Star/*<A>*/)) return false;
	    if(o.hashCode() != this.hashCode()) return false;

	    Star<?> sre = (Star) o;	    
	    return sre.starred.equals(this.starred);
	}
	
	protected int _hashCode() {
	    return starred.hashCode();
	}
	
	public String toString() {
	    if(starred instanceof Atomic/*<A>*/)
		return starred.toString() + "*";
	    return "(" + starred.toString() + ")*";
	}
    }
    

    /** Instance of the visitor pattern for <code>RegExp</code>s.
        Usage of this pattern avoids clumsy sequences of several
        <code>instanceof</code> tests.  A visitor takers a regular
        expression over symbols of type <code>A</code> and returns a
        result of type <code>Res</code>.  */
    public static abstract class Visitor<A,Res> {

	/** Subclasses should override this method to define special
            processing for a <code>None</code> regular expression. */
	public Res visit(None<A> re) {
	    return visit((RegExp<A>) re);
	}

	/** Subclasses should override this method to define special
            processing for an <code>Atomic</code> regular expression. */
	public Res visit(Atomic<A> re) {
	    return visit((RegExp<A>) re);
	}

	/** Subclasses should override this method to define special
            processing for an <code>EmptyStr</code> regular expression. */
	public Res visit(EmptyStr<A> re) {
	    return visit((RegExp<A>) re);
	}

	/** Subclasses should override this method to define special
            processing for a <code>Union</code> regular expression. */
	public Res visit(Union<A> re) {
	    return visit((RegExp<A>) re);
	}

	/** Subclasses should override this method to define special
            processing for a <code>Concat</code> regular expression. */
	public Res visit(Concat<A> re) {
	    return visit((RegExp<A>) re);
	}

	/** Subclasses should override this method to define special
            processing for a <code>Star</code> regular expression. */
	public Res visit(Star<A> re)  {
	    return visit((RegExp<A>) re);
	}
	
	/** Default code to execute for a regular expression.  If a
            subclass does not override the processing for a certain
            <code>RegExp</code> subclass, this code will be executed
            instead. */
	public Res visit(RegExp<A> re) {
	    throw new RuntimeException("unimplemented");
	}    

    }


    /** Returns a simplified, equivalent version of this regular
        expression.  Applies some simple simplifications for regular
        expression.  E.g., concatenation with <code>EmptyStr</code>
        and union with <code>None</code> can be ignored.  This method
        does not necessarily return the smallest equivalent regexp;
        regexp minimization is a hard problem, PSPACE-complete.  */
    public RegExp<A> simplify() {
	return (new SimplifyContext<A>()).simplify(this);
    }


    /** Caching wrapper around a method that performs simple
        simplification of regular expressions.  */
    private static class SimplifyContext<A> {

	private final Map<IdentityWrapper<RegExp<A>>,RegExp<A>> cache = 
	    new LinkedHashMap<IdentityWrapper<RegExp<A>>,RegExp<A>>();

	/** Performs several simple simplifications of regular
            expressions: e.g., <code>Star(None)<code> is equivalent to
            <code>EmptyStr</code>. */
	public RegExp<A> simplify(RegExp<A> regExp) {
	    IdentityWrapper<RegExp<A>> rew = new IdentityWrapper<RegExp<A>>(regExp);
	    RegExp<A> simplifiedRE = cache.get(rew);
	    if(simplifiedRE == null) {
		cache.put(rew, simplifiedRE = _simplify(regExp));
	    }
	    return simplifiedRE;
	}
	

	// does the actual job behind "simplify"
	private RegExp<A> _simplify(RegExp<A> regExp) {
	    return regExp.<RegExp<A>>accept
		(new Visitor<A,RegExp<A>>() {

		    public RegExp<A> visit(Union<A> ure) {
			// This Union node can be the top of an entire
			// subtree of Unions e.g., a|((a|b)|c).  We
			// first explore all the leaves of this
			// subtree (non-union regexps), simplify them
			// and put the results in a set (to remove
			// duplicates).
			Set<RegExp<A>> terms = new LinkedHashSet<RegExp<A>>();
			for(RegExp<A> term : ure.allTransTerms()) {
			    RegExp<A> simplTerm = simplify(term);
			    if(!(simplTerm instanceof None/*<A>*/)) {
				terms.add(simplify(simplTerm));
			    }			    
			}

			// Special cases:
			// 1. union of None regExps
			if(terms.isEmpty()) {
			    return new None<A>();
			}

			// 2. union of a single regExp
			if(terms.size() == 1) {
			    return DSUtil.<RegExp<A>>getFirst(terms);
			}

			// Normal case: at least 2 terms
			// rebuild a structure of binary Union nodes ...
			RegExp<A> last = null;
			for(RegExp<A> term : terms) {
			    if(last == null) {
				last = term;
			    }
			    else {
				last = new Union<A>(last, term);
			    }
			}
			// ... and return it
			return last;
		    }
		    
		    public RegExp<A> visit(Concat<A> cre) {
			RegExp<A> left  = simplify(cre.left);
			RegExp<A> right = simplify(cre.right);
			
			if((left instanceof None/*<A>*/) ||
			   (right instanceof None/*<A>*/)) {
			    return new None<A>();
			}
			
			if(left instanceof EmptyStr/*<A>*/) {
			    return right;
			}

			if(right instanceof EmptyStr/*<A>*/) {
			    return left;
			}

			return new Concat<A>(left, right);
		    }
		    
		    public RegExp<A> visit(Star<A> sre) {
			RegExp<A> starred = simplify(sre.starred);
			
			if(starred instanceof None/*<A>*/)
			    return new EmptyStr<A>();

			if(starred instanceof EmptyStr/*<A>*/)
			    return new EmptyStr<A>();

			// consecutive stars are redundant
			if(starred instanceof Star/*<A>*/)
			    return starred;
			
			return new Star<A>(starred);
		    }
		    
		    // default case: don't simplify anything
		    public RegExp<A> visit(RegExp<A> re) {
			return re; 
		    }
		});
	}
    }
}
