package kianxali.test;

import static org.junit.Assert.assertEquals;
import kianxali.cpu.x86.X86Context;
import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.Model;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ByteSequence;
import kianxali.util.OutputFormatter;

import org.junit.Before;
import org.junit.Test;

public class X86DecoderTest {
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
    public void testOpcodes() {
        checkOpcode32(new short[] {0x98}, "cwde");
        // checkOpcode32(new short[] {0x66, 0x98}, "cbw");
        checkOpcode32(new short[] {0x99}, "cdq");
        // checkOpcode32(new short[] {0x66, 0x99}, "cwd");
        checkOpcode32(new short[] {0xF5}, "cmc");
        checkOpcode32(new short[] {0xF8}, "clc");
        checkOpcode32(new short[] {0xF9}, "stc");
        checkOpcode32(new short[] {0xFA}, "cli");
        checkOpcode32(new short[] {0xFB}, "sti");
        checkOpcode32(new short[] {0xFC}, "cld");
        checkOpcode32(new short[] {0xFD}, "std");
        // checkOpcode32(new short[] {0xD6}, "setalc");
        checkOpcode32(new short[] {0xD7}, "xlat byte ptr ds:[ebx + al]");
        checkOpcode32(new short[] {0xC9}, "leave");
        checkOpcode32(new short[] {0xF1}, "int1");
        checkOpcode32(new short[] {0xCD, 0x01}, "int 1h");
        checkOpcode32(new short[] {0xCC}, "int 3h"); // should be int3, XML needs fixing
        checkOpcode32(new short[] {0xCD, 0x03}, "int 3h");
        // checkOpcode32(new short[] {0xF1}, "smi");
        checkOpcode32(new short[] {0xCE}, "into");
        checkOpcode32(new short[] {0xCF}, "iretd");
        // checkOpcode32(new short[] {0x66, 0xCF}, "iretw");
        checkOpcode32(new short[] {0xC3}, "retn");
        checkOpcode32(new short[] {0xC2, 0x10, 0x20}, "retn 2010h");
        checkOpcode32(new short[] {0xCA, 0x10, 0x20}, "retf 2010h");
        checkOpcode32(new short[] {0xCB}, "retf");
        checkOpcode32(new short[] {0xF4}, "hlt");
        checkOpcode32(new short[] {0x9F}, "lahf");
        checkOpcode32(new short[] {0x9E}, "sahf");
        checkOpcode32(new short[] {0x9C}, "pushfd");
        checkOpcode32(new short[] {0x9D}, "popfd");
        // checkOpcode32(new short[] {0x66, 0x9C}, "pushf");
        // checkOpcode32(new short[] {0x66, 0x9D}, "popf");
        // checkOpcode32(new short[] {0x66, 0x60}, "pushaw");
        checkOpcode32(new short[] {0x60}, "pushad");
        checkOpcode32(new short[] {0x61}, "popad");
        // checkOpcode32(new short[] {0x66, 0x61}, "popad");
        checkOpcode32(new short[] {0x27}, "daa");
        checkOpcode32(new short[] {0x37}, "aaa");
        checkOpcode32(new short[] {0x2F}, "das");
        checkOpcode32(new short[] {0x3F}, "aas");
        checkOpcode32(new short[] {0x50}, "push eax");
        checkOpcode32(new short[] {0x51}, "push ecx");
        checkOpcode32(new short[] {0x52}, "push edx");
        checkOpcode32(new short[] {0x53}, "push ebx");
        checkOpcode32(new short[] {0x54}, "push esp");
        checkOpcode32(new short[] {0x55}, "push ebp");
        checkOpcode32(new short[] {0x56}, "push esi");
        checkOpcode32(new short[] {0x57}, "push edi");
        checkOpcode32(new short[] {0x58}, "pop eax");
        checkOpcode32(new short[] {0x59}, "pop ecx");
        checkOpcode32(new short[] {0x5A}, "pop edx");
        checkOpcode32(new short[] {0x5B}, "pop ebx");
        checkOpcode32(new short[] {0x5C}, "pop esp");
        checkOpcode32(new short[] {0x5D}, "pop ebp");
        checkOpcode32(new short[] {0x5E}, "pop esi");
        checkOpcode32(new short[] {0x5F}, "pop edi");
        checkOpcode32(new short[] {0xFF, 0x30}, "push dword ptr [eax]");
        checkOpcode32(new short[] {0x8F, 0x00}, "pop dword ptr [eax]");
        checkOpcode32(new short[] {0x06}, "push es");
        checkOpcode32(new short[] {0x0E}, "push cs");
        checkOpcode32(new short[] {0x16}, "push ss");
        checkOpcode32(new short[] {0x1E}, "push ds");
        checkOpcode32(new short[] {0x07}, "pop es");
        checkOpcode32(new short[] {0x17}, "pop ss");
        checkOpcode32(new short[] {0x1F}, "pop ds");
        checkOpcode32(new short[] {0x6A, 0x01}, "push 1h");
        checkOpcode32(new short[] {0x68, 0x10, 0x20, 0x30, 0x40}, "push 40302010h");
        checkOpcode32(new short[] {0x0F, 0xA0}, "push fs");
        checkOpcode32(new short[] {0x0F, 0xA1}, "pop fs");
        checkOpcode32(new short[] {0x0F, 0xA8}, "push gs");
        checkOpcode32(new short[] {0x0F, 0xA9}, "pop gs");
    }

    @Test
    public void testSpecials() {
        // Prefixes
        checkOpcode32(new short[] {0x66,  0xD1, 0xEF}, "shr di, 1h");
        checkOpcode32(new short[] {0x51}, "push ecx");
        checkOpcode32(new short[] {0x66, 0x51}, "push cx");
        checkOpcode32(new short[] {0x0F, 0xA4, 0x01, 0x02}, "shld dword ptr [ecx], eax, 2h");
        checkOpcode32(new short[] {0x66, 0x0F, 0xA4, 0x01, 0x02}, "shld word ptr [ecx], ax, 2h");

        // FPU
        checkOpcode32(new short[] {0xDF,  0xE0}, "fnstsw ax");
        checkOpcode32(new short[] {0xDB,  0x29}, "fld tbyte ptr [ecx]");
        checkOpcode32(new short[] {0xDD,  0xC5}, "ffree st5");
        checkOpcode32(new short[] {0xDD,  0x05, 0x40, 0x30, 0x20, 0x10}, "fld qword ptr [10203040h]");
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
        checkOpcode32(new short[] {0x66, 0x81, 0x8e, 0x0E, 0x06, 0x00, 0x00, 0xFF, 0xFF}, "or word ptr [esi + 60Eh], -1h");

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

    private void checkOpcode16(short[] opcode, String expected) {
        checkOpcode(ctx16, opcode, expected);
    }

    private void checkOpcode32(short[] opcode, String expected) {
        checkOpcode(ctx32, opcode, expected);
    }

    private void checkOpcode64(short[] opcode, String expected) {
        checkOpcode(ctx64, opcode, expected);
    }

    private void checkOpcode(X86Context ct, short[] opcode, String expected) {
        byte[] in = new byte[opcode.length];
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
