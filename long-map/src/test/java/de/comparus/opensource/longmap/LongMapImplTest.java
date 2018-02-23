package de.comparus.opensource.longmap;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LongMapImplTest {
    private LongMap<String> longMap = new LongMapImpl<>();

    @Test(expected = IllegalArgumentException.class)
    public void createLongMapImplWithInvalidCapacity() {
        new LongMapImpl<>(-1, 0.75f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLongMapImplWithMinusLoadfactor() {
        new LongMapImpl<>(10, -0.75f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLongMapImplWithNanLoadfactor() {
        new LongMapImpl<>(10, Float.NaN);
    }

    @Test
    public void put() {
        for (int i = 0; i < 16; i++) {
            longMap.put(i, "" + i);
        }

        assertEquals(longMap.size(), 16);
        assertEquals(longMap.get(15), "15");
        assertEquals(longMap.get(0), "0");
    }

    @Test
    public void get() {
        assertEquals(longMap.get(0), null);

        longMap.put(0, "0");
        longMap.put(1, "1");
        longMap.put(10, "10");
        longMap.put(11, null);
        longMap.put(32, "32");
        longMap.put(16, "16");
        longMap.put(-16, "-16");

        assertEquals(longMap.get(0), "0");
        assertEquals(longMap.get(1), "1");
        assertEquals(longMap.get(10), "10");
        assertEquals(longMap.get(11), null);
        assertEquals(longMap.get(32), "32");
        assertEquals(longMap.get(16), "16");
        assertEquals(longMap.get(-16), "-16");
    }

    @Test
    public void removeIfNodeInArray() {
        longMap.put(0, "0");
        longMap.put(16, "16");
        longMap.put(32, "32");
        longMap.put(1, "1");

        assertEquals(longMap.remove(0), "0");
        assertEquals(longMap.get(0), null);
        assertEquals(longMap.get(16), "16");
        assertEquals(longMap.get(32), "32");

        assertEquals(longMap.remove(1), "1");
        assertEquals(longMap.get(1), null);
    }

    @Test
    public void removeIfNodeInLinkedList() {
        longMap.put(0, "0");
        longMap.put(16, "16");
        longMap.put(32, "32");
        longMap.put(48, "48");

        assertEquals(longMap.remove(16), "16");
        assertEquals(longMap.get(0), "0");
        assertEquals(longMap.get(16), null);
        assertEquals(longMap.get(32), "32");
        assertEquals(longMap.get(48), "48");

        assertEquals(longMap.remove(32), "32");
        assertEquals(longMap.get(0), "0");
        assertEquals(longMap.get(32), null);
        assertEquals(longMap.get(48), "48");
    }

    @Test
    public void isEmpty() {
        assertTrue(longMap.isEmpty());
        longMap.put(1, "1");

        assertFalse(longMap.isEmpty());

        longMap.put(17, "17");
        longMap.remove(1);
        longMap.remove(17);

        assertTrue(longMap.isEmpty());
    }

    @Test
    public void containsKey() {
        assertFalse(longMap.containsKey(0));
        assertFalse(longMap.containsKey(16));
        longMap.put(0, "0");
        longMap.put(16, "16");
        assertTrue(longMap.containsKey(0));
        assertTrue(longMap.containsKey(16));
    }

    @Test
    public void containsValue() {
        assertFalse(longMap.containsValue("1"));
        longMap.put(1, "1");
        assertTrue(longMap.containsValue("1"));

        assertFalse(longMap.containsValue(null));
        longMap.put(1, null);
        assertTrue(longMap.containsValue(null));
    }

    @Test
    public void keys() {
        long[] arrayForComparison = new long[100];
        for (int i = 0; i < 100; i++) {
            longMap.put(i, "" + i);
            arrayForComparison[i] = i;
        }

        assertArrayEquals(longMap.keys(), arrayForComparison);
    }

    @Test
    public void values() {
        String[] arrayForComparison = new String[100];
        for (int i = 0; i < 100; i++) {
            longMap.put(i, "" + i);
            arrayForComparison[i] = "" + i;
        }

        assertArrayEquals(longMap.values(), arrayForComparison);
    }

    @Test
    public void size() {
        assertEquals(longMap.size(), 0);
        for (int i = 0; i < 200; i++) {
            longMap.put(i, null);
        }
        assertEquals(longMap.size(), 200);
        for (int i = 50; i < 150; i++) {
            longMap.remove(i);
        }
        assertEquals(longMap.size(), 100);
    }

    @Test
    public void clear() {
        assertTrue(longMap.isEmpty());
        longMap.put(1, null);
        longMap.put(16, null);
        assertFalse(longMap.isEmpty());
        longMap.clear();
        assertTrue(longMap.isEmpty());
        assertEquals(longMap.size(), 0);
    }
}