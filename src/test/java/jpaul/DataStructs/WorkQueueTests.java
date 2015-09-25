package jpaul.DataStructs;

import java.util.Queue;

import junit.framework.TestCase;

public class WorkQueueTests extends TestCase {
    public void test1() throws Exception {
        Queue<String> q = new WorkQueue<String>(3);
        q.offer("a");
        q.offer("b");
        q.offer("c");
        q.offer("d");
        q.offer("e");

        assertEquals("a", q.poll());
        assertEquals("b", q.poll());
        assertEquals("c", q.poll());

        q.offer("f");
        q.offer("g");
        q.offer("h");

        assertEquals("d", q.poll());
        assertEquals("e", q.poll());
        q.remove();
        assertEquals("g", q.poll());
        q.remove();
        assertTrue(q.isEmpty());
    }
}
