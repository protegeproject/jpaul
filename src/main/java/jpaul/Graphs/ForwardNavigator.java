// ForwardNavigator.java, created Wed May  7 10:47:23 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;

/**
 * <code>ForwardNavigator</code> is a forward-only graph navigator:
 * given a vertex, it returns its successors in the graph.  It is
 * extended by the <code>Navigator</code> interface which is a
 * bi-directional graph navigator.
 *
 * @see BiDiNavigator
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: ForwardNavigator.java,v 1.4 2005/12/09 19:03:03 salcianu Exp $ */
public interface ForwardNavigator<Vertex> {
    
    /** Returns the successors of <code>vertex</code>.  Returns a list
        (instead of a set) in order to support graphs and graph
        algorithms that care about the order of the in-coming arcs. */
    List<Vertex> next(Vertex vertex);

}
