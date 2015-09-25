// BiDiNavigator.java, created Mon Apr  1 23:43:47 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu - salcianu@alum.mit.edu
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;

/** The <code>BiDiNavigator</code> interface allows graph algorithms to
    detect (and use) the arcs from and to a certain vertex.  This allows
    the use of many graph algorithms (eg construction of strongly
    connected components) even for very general graphs where the arcs
    model only a subtle semantic relation (eg caller-callee) that is
    not directly stored in the structure of the vertices.

   @author  Alexandru Salcianu - salcianu@alum.mit.edu
   @version $Id: BiDiNavigator.java,v 1.1 2005/12/09 16:06:19 salcianu Exp $ */
public interface BiDiNavigator<Vertex> extends ForwardNavigator<Vertex> {
    
    /** Returns the predecessors of <code>vertex</code>.  Returns a
        list (instead of a set) in order to support graphs and graph
        algorithms that care about the order of the out-going arcs;
        e.g., consider an IF node in a control-flow-graph: usually,
        the first outgoing arc is the TRUE branch, while the second is
        the FALSE branch. */
    List<Vertex> prev(Vertex vertex);

}
