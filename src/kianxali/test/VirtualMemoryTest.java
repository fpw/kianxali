package kianxali.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import kianxali.image.LoadedImage;

import org.junit.Before;
import org.junit.Test;

public class VirtualMemoryTest {
    LoadedImage imageMemory;

    @Before
    public void testMemory() {
        imageMemory = new LoadedImage();
    }

    @Test
    public void testMap() {
        DecodedEntityStub a = new DecodedEntityStub(0x400000, 5, "a");

        imageMemory.insert(a);
        assertEquals(a, imageMemory.getEntityOnExactAddress(0x400000));
        assertNull(imageMemory.find(0x3FFFFF));
        for(int i = 0; i < a.getSize(); i++) {
            assertEquals(a, imageMemory.find(0x400000 + i));
            if(i > 1) {
                assertNull(imageMemory.getEntityOnExactAddress(0x400000 + i));
            }
        }
        assertNull(imageMemory.find(0x400005));

        DecodedEntityStub b = new DecodedEntityStub(0x400002, 2, "b");
        imageMemory.insert(b);
        assertNull(imageMemory.find(0x400000));
        assertNull(imageMemory.find(0x400001));
        assertEquals(b, imageMemory.find(0x400002));
        assertEquals(b, imageMemory.find(0x400003));
        assertNull(imageMemory.find(0x400004));
        assertNull(imageMemory.find(0x400005));
    }
}
