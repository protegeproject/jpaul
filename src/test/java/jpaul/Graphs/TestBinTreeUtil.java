// TestBinTreeUtil.java, created Mon Mar 13 18:30:16 2006 by salcianu
// Copyright (C) 2006 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul.Graphs;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import java.util.Random;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

/**
 * <code>TestBinTreeUtil</code>
 *
 * @author Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: TestBinTreeUtil.java,v 1.2 2006/03/21 17:37:30 adam_kiezun Exp $
 */
public class TestBinTreeUtil extends TestCase {

    public static Test suite() {
        return new TestSuite(TestBinTreeUtil.class);
    }

    public void testBinTree() {
        TestBinTreeUtil.main(new String[]{});
    }


    public static void main(String[] args) {
        System.out.println("\nTEST TREE ITERATORS:\n");

        System.out.println("Test on a simple regular tree:\n");
        BinTreeNavigator<Integer> btNav = new BinTreeNavigator<Integer>() {
            final int LIMIT = 15;

            public Integer left(Integer node) {
                int i = node.intValue();
                return i < LIMIT ? new Integer(2 * i + 1) : null;
            }

            public Integer right(Integer node) {
                int i = node.intValue();
                return i < LIMIT + 5 ? new Integer(2 * i + 2) : null;
            }
        };

        check(new Integer(0), btNav);

        System.out.println("\nTest on randomly generated trees:\n");
        for (int i = 0; i < 10; i++) {
            System.out.println("Test #" + (i + 1));
            Pair<BTNode, BinTreeNavigator<BTNode>> tree = randomTree();
            check(tree.left, tree.right);
        }
    }

    private static class BTNode {
        BTNode(int id, BTNode left, BTNode right) {
            this.id = id;
            this.left = left;
            this.right = right;
        }

        BTNode(int id) {
            this(id, null, null);
        }

        public final int id;
        public final BTNode left;
        public final BTNode right;

        public String toString() {
            return id + "";
        }
    }

    private static Pair<BTNode, BinTreeNavigator<BTNode>> randomTree() {
        Random random = new Random(System.currentTimeMillis());
        int size = random.nextInt(30) + 1;
        LinkedList<BTNode> verts = new LinkedList<BTNode>();
        int count = 0;
        for (int i = 0; i < size; i++) {
            verts.add(new BTNode(count++));
        }
        // add some null links here
        int sizeNull = random.nextInt(10);
        for (int i = 0; i < sizeNull; i++) {
            verts.add(null);
        }
        while (verts.size() > 1) {
            int k1 = random.nextInt(verts.size());
            int k2 = random.nextInt(verts.size());
            BTNode n1 = verts.get(k1);
            BTNode n2 = verts.get(k2);
            verts.remove(k1);
            if (k1 != k2) {
                verts.remove(k1 < k2 ? (k2 - 1) : k2);
            }
            verts.add(new BTNode(count++, n1, n2));
        }

        return new Pair<BTNode, BinTreeNavigator<BTNode>>
                (verts.getFirst(),
                        new BinTreeNavigator<BTNode>() {
                            public BTNode left(BTNode node) {
                                return node.left;
                            }

                            public BTNode right(BTNode node) {
                                return node.right;
                            }
                        });
    }


    // Checks the tree iterators on the tree rooted in "root", with
    // navigator btNav.  For each iterator, we compare the list it
    // defines implicitly with the in/pre/post traversal produced by a
    // classic, recursive classic traversal (that constructs an
    // explicit list and may hence consume too much memory).
    private static <T> void check(T root, BinTreeNavigator<T> btNav) {
        System.out.print("Check in-order: list = ");
        checkSame(BinTreeUtil.<T, T>inOrder(root, btNav),
                classicInOrder(root, btNav));
        System.out.println();

        System.out.print("Check pre-order: list = ");
        checkSame(BinTreeUtil.<T, T>preOrder(root, btNav),
                classicPreOrder(root, btNav));
        System.out.println();

        System.out.print("Check post-order: list = ");
        checkSame(BinTreeUtil.<T, T>postOrder(root, btNav),
                classicPostOrder(root, btNav));
        System.out.println();

        System.out.println("ok");
        System.out.println();
        System.out.flush();
    }

    private static <T> void checkSame(Iterator<T> it, List<T> list) {
        for (Iterator<T> itList = list.iterator(); itList.hasNext(); ) {
            T elem1 = it.next();
            T elem2 = itList.next();
            System.out.print(elem1 + " ");
            assert DSUtil.checkEq(elem1, elem2) : "different elements " + elem1 + " != " + elem2;
        }
        assert !it.hasNext() : "not exhausted iterator";
        //System.out.println("list = " + list);
    }

    private static <T> List<T> classicInOrder(T root, BinTreeNavigator<T> btNav) {
        return classicInOrder(root, btNav, new LinkedList<T>());
    }

    private static <T> List<T> classicInOrder(T node, BinTreeNavigator<T> btNav, List<T> list) {
        if (node == null) {
            return list;
        }
        classicInOrder(btNav.left(node), btNav, list);
        list.add(node);
        classicInOrder(btNav.right(node), btNav, list);
        return list;
    }

    private static <T> List<T> classicPreOrder(T root, BinTreeNavigator<T> btNav) {
        return classicPreOrder(root, btNav, new LinkedList<T>());
    }

    private static <T> List<T> classicPreOrder(T node, BinTreeNavigator<T> btNav, List<T> list) {
        if (node == null) {
            return list;
        }
        list.add(node);
        classicPreOrder(btNav.left(node), btNav, list);
        classicPreOrder(btNav.right(node), btNav, list);
        return list;
    }

    private static <T> List<T> classicPostOrder(T root, BinTreeNavigator<T> btNav) {
        return classicPostOrder(root, btNav, new LinkedList<T>());
    }

    private static <T> List<T> classicPostOrder(T node, BinTreeNavigator<T> btNav, List<T> list) {
        if (node == null) {
            return list;
        }
        classicPostOrder(btNav.left(node), btNav, list);
        classicPostOrder(btNav.right(node), btNav, list);
        list.add(node);
        return list;
    }

}
