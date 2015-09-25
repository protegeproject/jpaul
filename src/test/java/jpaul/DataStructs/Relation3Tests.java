package jpaul.DataStructs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

public class Relation3Tests extends TestCase {
    private static <T> Set<T> set(T...ts) {
        return new ArraySet<T>(Arrays.asList(ts));
    }
    
    private static <T> Set<T> makeSet(Collection<T> c) {
        return new ArraySet<T>(c);
    }

    public void test1() throws Exception {
        Relation3<Integer,Integer,String> rel3 = new Relation3MapRelImpl<Integer,Integer,String>();
        rel3.add(new Integer(1), new Integer(3), "ala");
        rel3.add(new Integer(1), new Integer(2), "ala");
        rel3.add(new Integer(1), new Integer(2), "bala");
        rel3.add(new Integer(1), new Integer(3), "ala");
        rel3.add(new Integer(1), new Integer(3), "ala");
        
        assertEquals(set(new Integer(3),new Integer(2)), makeSet(rel3.get2ndValues(new Integer(1))));
        assertEquals(set("ala","bala"), makeSet(rel3.get3rdValues(new Integer(1), new Integer(2))));
        assertEquals(set("ala"), makeSet(rel3.get3rdValues(new Integer(1), new Integer(3))));
        assertEquals(3, rel3.size());

	rel3.clear();
	assertEquals(0, rel3.size());
	rel3.add(new Integer(2), new Integer(6), "buhuhu");
        rel3.add(new Integer(1), new Integer(3), "ala");
        rel3.add(new Integer(1), new Integer(3), "ala");
	assertEquals(2, rel3.size());
	assertTrue("<1,3,\"ala\"> should be in!",
		   rel3.contains(new Integer(1), new Integer(3), "ala"));
	assertTrue("<1,3,\"bala\"> should be out!",
		   !rel3.contains(new Integer(1), new Integer(3), "bala"));
    }
    
}
