// BasicBlock.java, created Wed Feb 23 13:47:55 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import java.util.List;
import java.util.LinkedList;

/**
 * <code>BasicBlock</code> is a <i>straight-line</i> sequence of
 * vertices.  Except (possibly) for the first vertex, every other
 * vertex has a single incoming arc, from the immediate predecessor in
 * the same basic block.  Similarly, except (possibly) for the last
 * vertex, every other vertex has a single outgoing arc, to the
 * immediate successor in the same basic block.  Moreover, the basic
 * block is maximal with respect to these properties: it cannot be
 * extended upward (e.g., because the first element has two incoming
 * arcs), not downward.
 *
 * @see BasicBlockDiGraph
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: BasicBlock.java,v 1.6 2005/12/08 06:27:04 salcianu Exp $ */
public interface BasicBlock<Vertex> {

    /** @return List of basic blocks predecessors.  Caller should not
	mutate the returned list.

	Complexity: O(1). */
    public List<BasicBlock<Vertex>> prev();

    /** @return List of basic blocks successors.  Caller should not
	mutate the returned list.

	Complexity: O(1). */
    public List<BasicBlock<Vertex>> next();

    /** @return List of vertices from <code>this</code> basic block.
	Caller should not mutate the returned list.

	Complexity: O(1). */
    public LinkedList<Vertex> elems();

    /** @return Integer identifier for <code>this</code> basic block.
        The identifiers of basic blocks from the same
        <code>BasicBlockDiGraph</code> should be distinct.

	Complexity: O(1). */
    public int getId();

    /** @return Enclosing {@link BasicBlockDiGraph}.

	Complexity: O(1). */
    public DiGraph<BasicBlock<Vertex>> enclosingBBDiGraph();
}
