package kianxali.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import kianxali.disassembler.DisassemblyData;

import org.junit.Before;
import org.junit.Test;

public class DisassemblyDataTest {
    DisassemblyData disassemblyData;

    @Before
    public void testMemory() {
        disassemblyData = new DisassemblyData();
    }

    @Test
    public void testMap() {
        DecodedEntityStub a = new DecodedEntityStub(0x400000, 5, "a");

        disassemblyData.insertEntity(a);
        assertEquals(a, disassemblyData.getEntityOnExactAddress(0x400000));
        assertNull(disassemblyData.findEntity(0x3FFFFF));
        for(int i = 0; i < a.getSize(); i++) {
            assertEquals(a, disassemblyData.findEntity(0x400000 + i));
            if(i > 1) {
                assertNull(disassemblyData.getEntityOnExactAddress(0x400000 + i));
            }
        }
        assertNull(disassemblyData.findEntity(0x400005));

        DecodedEntityStub b = new DecodedEntityStub(0x400002, 2, "b");
        disassemblyData.insertEntity(b);
        assertNull(disassemblyData.findEntity(0x400000));
        assertNull(disassemblyData.findEntity(0x400001));
        assertEquals(b, disassemblyData.findEntity(0x400002));
        assertEquals(b, disassemblyData.findEntity(0x400003));
        assertNull(disassemblyData.findEntity(0x400004));
        assertNull(disassemblyData.findEntity(0x400005));
    }
}
