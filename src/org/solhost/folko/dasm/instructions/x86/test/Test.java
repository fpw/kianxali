package org.solhost.folko.dasm.instructions.x86.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.CPUMode;
import org.solhost.folko.dasm.instructions.x86.Context;
import org.solhost.folko.dasm.instructions.x86.ModRM;

public class Test {
    private Context protectedMode;

    @Before
    public void setup() {
        protectedMode = new Context(CPUMode.PROTECTED);
    }

    @org.junit.Test
    public void testModRMProtected() {
        ModRM modRM;
        byte[] b = new byte[] {0};

        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [eax]", modRM.getMemOp().toString());

        b = new byte[] {5, 0x10, 0x20, 0x30, 0x40};
        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [40302010h]", modRM.getMemOp().toString());

        b = new byte[] {4, 0x51};
        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [ecx + 2 * edx]", modRM.getMemOp().toString());

        b = new byte[] {4, 0x5D, 0x10, 0x20, 0x30, 0x40};
        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [2 * ebx + 40302010h]", modRM.getMemOp().toString());

        b = new byte[] {0x44, (byte) 0xDD, 0x10};
        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [ebp + 8 * ebx + 10h]", modRM.getMemOp().toString());

        b = new byte[] {(byte) 0x84, (byte) 0xDD, 0x10, 0x20, 0x30, 0x40};
        modRM = new ModRM(ByteSequence.fromBytes(b), protectedMode);
        assertEquals("dword ptr [ebp + 8 * ebx + 40302010h]", modRM.getMemOp().toString());
    }
}
