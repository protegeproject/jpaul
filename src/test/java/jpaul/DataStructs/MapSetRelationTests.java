package jpaul.DataStructs;

import java.util.TreeSet;

import junit.framework.TestCase;

public class MapSetRelationTests extends TestCase{
    public void testEquals() throws Exception {
        Relation<String,String> r= new MapSetRelation<String, String>();
        assertTrue(! r.equals(null));
    }
    
    public void testEquals2() throws Exception {
        Relation<String,String> r= new MapSetRelation<String, String>();
        assertTrue(r.equals(r));
    }

    public void testEquals3() throws Exception {
        Relation<String,String> r= new MapSetRelation<String, String>();
        assertTrue(! r.equals(new Integer(1)));
    }

    public void testAdd1() throws Exception {
        Relation<String,Integer> r= new MapSetRelation<String, Integer>();
        r.add("foo", new Integer(1));
        r.add("bar", new Integer(1));
        r.add("foo", new Integer(2));
        
        assertEquals(new ArraySet<String>("bar", "foo"), new TreeSet<String>(r.keys()));
        assertEquals(new ArraySet<Integer>(new Integer(1), new Integer(2)), r.getValues("foo"));
        assertEquals(new ArraySet<Integer>(new Integer(1)), r.getValues("bar"));
    }
}
