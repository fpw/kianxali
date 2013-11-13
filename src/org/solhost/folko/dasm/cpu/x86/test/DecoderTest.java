package org.solhost.folko.dasm.cpu.x86.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;
import org.solhost.folko.dasm.cpu.x86.X86Context;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.decoder.Decoder;
import org.solhost.folko.dasm.images.ByteSequence;

public class DecoderTest {
    private OutputFormatter format;
    private X86Context ctx16, ctx32, ctx64;

    @Before
    public void createContext() {
        ctx16 = new X86Context(Model.ANY, ExecutionMode.REAL);
        ctx32 = new X86Context(Model.ANY, ExecutionMode.PROTECTED);
        ctx64 = new X86Context(Model.ANY, ExecutionMode.LONG);
        format = new OutputFormatter();
    }

    @Test
    public void testModRM16() {
        // mode 0
        checkOpcode16(new short[] {0x88, 0x38}, "mov byte ptr [bx + si], bh");
        checkOpcode16(new short[] {0x88, 0x3E, 0x10, 0x20}, "mov byte ptr [2010h], bh");

        // mode 1
        checkOpcode16(new short[] {0x88, 0x7E, 0x10}, "mov byte ptr [bp + 10h], bh");

        // mode 2
        checkOpcode16(new short[] {0x88, 0xBA, 0x10, 0x20}, "mov byte ptr [bp + si + 2010h], bh");

        // mode 3
        checkOpcode16(new short[] {0x88, 0xC9}, "mov cl, cl");
    }

    @Test
    public void testModRM32() {
        // mode 0
        checkOpcode32(new short[] {0x88, 0x3A}, "mov byte ptr [edx], bh");
        checkOpcode32(new short[] {0x88, 0x3C, 0xCA}, "mov byte ptr [edx + 8 * ecx], bh");
        checkOpcode32(new short[] {0x88, 0x3C, 0x25, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [40030201h], bh");
        checkOpcode32(new short[] {0x88, 0x3C, 0x65, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [40030201h], bh");
        checkOpcode32(new short[] {0x88, 0x3C, 0xA5, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [40030201h], bh");
        checkOpcode32(new short[] {0x88, 0x3C, 0xE5, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [40030201h], bh");
        checkOpcode32(new short[] {0x88, 0x3D, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [40030201h], bh");

        // mode 1
        checkOpcode32(new short[] {0x88, 0x7A, 0x01}, "mov byte ptr [edx + 1h], bh");
        checkOpcode32(new short[] {0x88, 0x7C, 0xCA, 0x01}, "mov byte ptr [edx + 8 * ecx + 1h], bh");
        checkOpcode32(new short[] {0x88, 0x7C, 0x25, 0x01}, "mov byte ptr [ebp + 1h], bh");
        checkOpcode32(new short[] {0x88, 0x7C, 0x65, 0x01}, "mov byte ptr [ebp + 1h], bh");
        checkOpcode32(new short[] {0x88, 0x7C, 0xA5, 0x01}, "mov byte ptr [ebp + 1h], bh");
        checkOpcode32(new short[] {0x88, 0x7C, 0xE5, 0x01}, "mov byte ptr [ebp + 1h], bh");

        // mode 2
        checkOpcode32(new short[] {0x88, 0xBA, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [edx + 40030201h], bh");
        checkOpcode32(new short[] {0x88, 0xBC, 0xCA, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [edx + 8 * ecx + 40030201h], bh");
        checkOpcode32(new short[] {0x88, 0xBC, 0x25, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [ebp + 40030201h], bh");
        checkOpcode32(new short[] {0x88, 0xBC, 0x65, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [ebp + 40030201h], bh");
        checkOpcode32(new short[] {0x88, 0xBC, 0xA5, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [ebp + 40030201h], bh");
        checkOpcode32(new short[] {0x88, 0xBC, 0xE5, 0x01, 0x02, 0x03, 0x40}, "mov byte ptr [ebp + 40030201h], bh");

        // mode 3
        checkOpcode32(new short[] {0x88, 0xC1}, "mov cl, al");

        // more SIB tests
        checkOpcode32(new short[] {0x88, 0x80, 0x01, 0x00, 0x00, 0x00}, "mov byte ptr [eax + 1h], al");
        checkOpcode32(new short[] {0x88, 0x84, 0x08, 0x01, 0x00, 0x00, 0x00}, "mov byte ptr [eax + ecx + 1h], al");
        checkOpcode32(new short[] {0x88, 0x44, 0x55, 0x01}, "mov byte ptr [ebp + 2 * edx + 1h], al");
        checkOpcode32(new short[] {0x89, 0x94, 0x88, 0x00, 0x20, 0x40, 0x00}, "mov dword ptr [eax + 4 * ecx + 402000h], edx");

        // check that REX prefix is ignored / encoded as normal op
        checkOpcode32(new short[] {0x45}, "inc ebp");
    }

    @Test
    public void testModRM64() {
        // mode 0
        checkOpcode64(new short[] {0x88, 0x3A}, "mov byte ptr [rdx], bh");

        // with rex prefix
        checkOpcode64(new short[] {0x45, 0x88, 0x3A}, "mov byte ptr [R10], R15B");
        checkOpcode64(new short[] {0x67, 0x45, 0x88, 0x3A}, "mov byte ptr [R10D], R15B");
    }

    private void checkOpcode16(short opcode[], String expected) {
        checkOpcode(ctx16, opcode, expected);
    }

    private void checkOpcode32(short opcode[], String expected) {
        checkOpcode(ctx32, opcode, expected);
    }

    private void checkOpcode64(short opcode[], String expected) {
        checkOpcode(ctx64, opcode, expected);
    }

    private void checkOpcode(X86Context ct, short opcode[], String expected) {
        byte in[] = new byte[opcode.length];
        for(int i = 0; i < opcode.length; i++) {
            in[i] = (byte) opcode[i];
        }

        ByteSequence seq = ByteSequence.fromBytes(in);
        Decoder decoder = ct.createInstructionDecoder();
        Instruction inst = decoder.decodeOpcode(ct, seq);
        assertEquals(expected.toLowerCase(), inst.asString(format).toLowerCase());
        assertEquals(opcode.length, inst.getSize());
    }
}
