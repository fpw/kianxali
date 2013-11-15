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
    public void testPushPop() {
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
    public void testIncDec() {
        checkOpcode32(new short[] {0x40}, "inc eax");
        checkOpcode32(new short[] {0x41}, "inc ecx");
        checkOpcode32(new short[] {0x42}, "inc edx");
        checkOpcode32(new short[] {0x43}, "inc ebx");
        checkOpcode32(new short[] {0x44}, "inc esp");
        checkOpcode32(new short[] {0x45}, "inc ebp");
        checkOpcode32(new short[] {0x46}, "inc esi");
        checkOpcode32(new short[] {0x47}, "inc edi");
        checkOpcode32(new short[] {0x48}, "dec eax");
        checkOpcode32(new short[] {0x49}, "dec ecx");
        checkOpcode32(new short[] {0x4A}, "dec edx");
        checkOpcode32(new short[] {0x4B}, "dec ebx");
        checkOpcode32(new short[] {0x4C}, "dec esp");
        checkOpcode32(new short[] {0x4D}, "dec ebp");
        checkOpcode32(new short[] {0x4E}, "dec esi");
        checkOpcode32(new short[] {0x4F}, "dec edi");
        checkOpcode32(new short[] {0xFE, 0x00}, "inc byte ptr [eax]");
        checkOpcode32(new short[] {0xFF, 0x00}, "inc dword ptr [eax]");
        checkOpcode32(new short[] {0xFE, 0x08}, "dec byte ptr [eax]");
        checkOpcode32(new short[] {0xFF, 0x08}, "dec dword ptr [eax]");
    }

    @Test
    public void testMov() {
        checkOpcode32(new short[] {0xB0, 0x01}, "mov al, 1h");
        checkOpcode32(new short[] {0xB1, 0x01}, "mov cl, 1h");
        checkOpcode32(new short[] {0xB2, 0x01}, "mov dl, 1h");
        checkOpcode32(new short[] {0xB3, 0x01}, "mov bl, 1h");
        checkOpcode32(new short[] {0xB4, 0x01}, "mov ah, 1h");
        checkOpcode32(new short[] {0xB5, 0x01}, "mov ch, 1h");
        checkOpcode32(new short[] {0xB6, 0x01}, "mov dh, 1h");
        checkOpcode32(new short[] {0xB7, 0x01}, "mov bh, 1h");
        checkOpcode32(new short[] {0x88, 0x00}, "mov byte ptr [eax], al");
        checkOpcode32(new short[] {0x89, 0x00}, "mov dword ptr [eax], eax");
        checkOpcode32(new short[] {0x8A, 0x00}, "mov al, byte ptr [eax]");
        checkOpcode32(new short[] {0xA0, 0x40, 0x30, 0x20, 0x10}, "mov al, byte ptr [10203040h]");
        checkOpcode32(new short[] {0xA1, 0x40, 0x30, 0x20, 0x10}, "mov eax, dword ptr [10203040h]");
        checkOpcode32(new short[] {0xA2, 0x40, 0x30, 0x20, 0x10}, "mov byte ptr [10203040h], al");
        checkOpcode32(new short[] {0xA3, 0x40, 0x30, 0x20, 0x10}, "mov dword ptr [10203040h], eax");
        checkOpcode32(new short[] {0xB0, 0x01}, "mov al, 1h");
        checkOpcode32(new short[] {0xB1, 0x01}, "mov cl, 1h");
        checkOpcode32(new short[] {0xB2, 0x01}, "mov dl, 1h");
        checkOpcode32(new short[] {0xB3, 0x01}, "mov bl, 1h");
        checkOpcode32(new short[] {0xB4, 0x01}, "mov ah, 1h");
        checkOpcode32(new short[] {0xB5, 0x01}, "mov ch, 1h");
        checkOpcode32(new short[] {0xB6, 0x01}, "mov dh, 1h");
        checkOpcode32(new short[] {0xB7, 0x01}, "mov bh, 1h");
        checkOpcode32(new short[] {0xB8, 0x10, 0x20, 0x30, 0x40}, "mov eax, 40302010h");
        checkOpcode32(new short[] {0xB9, 0x10, 0x20, 0x30, 0x40}, "mov ecx, 40302010h");
        checkOpcode32(new short[] {0xBA, 0x10, 0x20, 0x30, 0x40}, "mov edx, 40302010h");
        checkOpcode32(new short[] {0xBB, 0x10, 0x20, 0x30, 0x40}, "mov ebx, 40302010h");
        checkOpcode32(new short[] {0xBC, 0x10, 0x20, 0x30, 0x40}, "mov esp, 40302010h");
        checkOpcode32(new short[] {0xBD, 0x10, 0x20, 0x30, 0x40}, "mov ebp, 40302010h");
        checkOpcode32(new short[] {0xBE, 0x10, 0x20, 0x30, 0x40}, "mov esi, 40302010h");
        checkOpcode32(new short[] {0xBF, 0x10, 0x20, 0x30, 0x40}, "mov edi, 40302010h");
    }

    @Test
    public void testArithmetic() {
        checkOpcode32(new short[] {0xD4, 0x0A}, "aam");
        checkOpcode32(new short[] {0xD5, 0x0A}, "aad");
        checkOpcode32(new short[] {0x10, 0x00}, "adc byte ptr [eax], al");
        checkOpcode32(new short[] {0x11, 0x00}, "adc dword ptr [eax], eax");
        checkOpcode32(new short[] {0x12, 0x00}, "adc al, byte ptr [eax]");
        checkOpcode32(new short[] {0x13, 0x00}, "adc eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x14, 0x01}, "adc al, 1h");
        checkOpcode32(new short[] {0x15, 0x10, 0x20, 0x30, 0x40}, "adc eax, 40302010h");
        checkOpcode32(new short[] {0x00, 0x00}, "add byte ptr [eax], al");
        checkOpcode32(new short[] {0x01, 0x00}, "add dword ptr [eax], eax");
        checkOpcode32(new short[] {0x02, 0x00}, "add al, byte ptr [eax]");
        checkOpcode32(new short[] {0x03, 0x00}, "add eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x04, 0x00}, "add al, 0");
        checkOpcode32(new short[] {0x05, 0x10, 0x20, 0x30, 0x40}, "add eax, 40302010h");
        checkOpcode32(new short[] {0x80, 0x00, 0x00}, "add byte ptr [eax], 0");
        checkOpcode32(new short[] {0x81, 0x00, 0x10, 0x20, 0x30, 0x40}, "add dword ptr [eax], 40302010h");
        checkOpcode32(new short[] {0x82, 0x00, 0x00}, "add byte ptr [eax], 0");
        checkOpcode32(new short[] {0x83, 0x00, 0x10}, "add dword ptr [eax], 10h");
        checkOpcode32(new short[] {0x20, 0x00}, "and byte ptr [eax], al");
        checkOpcode32(new short[] {0x21, 0x00}, "and dword ptr [eax], eax");
        checkOpcode32(new short[] {0x22, 0x00}, "and al, byte ptr [eax]");
        checkOpcode32(new short[] {0x23, 0x00}, "and eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x24, 0x00}, "and al, 0");
        checkOpcode32(new short[] {0x25, 0x40, 0x30, 0x20, 0x10}, "and eax, 10203040h");
        checkOpcode32(new short[] {0x38, 0x00}, "cmp byte ptr [eax], al");
        checkOpcode32(new short[] {0x39, 0x00}, "cmp dword ptr [eax], eax");
        checkOpcode32(new short[] {0x3A, 0x00}, "cmp al, byte ptr [eax]");
        checkOpcode32(new short[] {0x3B, 0x00}, "cmp eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x3C, 0x00}, "cmp al, 0");
        checkOpcode32(new short[] {0x3D, 0x40, 0x30, 0x20, 0x10}, "cmp eax, 10203040h");
        checkOpcode32(new short[] {0x08, 0x00}, "or byte ptr [eax], al");
        checkOpcode32(new short[] {0x09, 0x00}, "or dword ptr [eax], eax");
        checkOpcode32(new short[] {0x0A, 0x00}, "or al, byte ptr [eax]");
        checkOpcode32(new short[] {0x0B, 0x00}, "or eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x0C, 0x00}, "or al, 0");
        checkOpcode32(new short[] {0x0D, 0x40, 0x30, 0x20, 0x10}, "or eax, 10203040h");
        checkOpcode32(new short[] {0x18, 0x00}, "sbb byte ptr [eax], al");
        checkOpcode32(new short[] {0x19, 0x00}, "sbb dword ptr [eax], eax");
        checkOpcode32(new short[] {0x1A, 0x00}, "sbb al, byte ptr [eax]");
        checkOpcode32(new short[] {0x1B, 0x00}, "sbb eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x1C, 0x00}, "sbb al, 0");
        checkOpcode32(new short[] {0x1D, 0x40, 0x30, 0x20, 0x10}, "sbb eax, 10203040h");
        checkOpcode32(new short[] {0x28, 0x00}, "sub byte ptr [eax], al");
        checkOpcode32(new short[] {0x29, 0x00}, "sub dword ptr [eax], eax");
        checkOpcode32(new short[] {0x2A, 0x00}, "sub al, byte ptr [eax]");
        checkOpcode32(new short[] {0x2B, 0x00}, "sub eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x2C, 0x00}, "sub al, 0");
        checkOpcode32(new short[] {0x2D, 0x40, 0x30, 0x20, 0x10}, "sub eax, 10203040h");
        checkOpcode32(new short[] {0x84, 0x00}, "test byte ptr [eax], al");
        checkOpcode32(new short[] {0x85, 0x00}, "test dword ptr [eax], eax");
        checkOpcode32(new short[] {0xA8, 0x01}, "test al, 1h");
        checkOpcode32(new short[] {0xA9, 0x40, 0x30, 0x20, 0x10}, "test eax, 10203040h");
        checkOpcode32(new short[] {0xF6, 0x00, 0x01}, "test byte ptr [eax], 1h");
        checkOpcode32(new short[] {0xF7, 0x00, 0x40, 0x30, 0x20, 0x10}, "test dword ptr [eax], 10203040h");
        checkOpcode32(new short[] {0x86, 0x00}, "xchg al, byte ptr [eax]");
        checkOpcode32(new short[] {0x87, 0x00}, "xchg eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x30, 0x00}, "xor byte ptr [eax], al");
        checkOpcode32(new short[] {0x31, 0x00}, "xor dword ptr [eax], eax");
        checkOpcode32(new short[] {0x32, 0x00}, "xor al, byte ptr [eax]");
        checkOpcode32(new short[] {0x33, 0x00}, "xor eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x34, 0x00}, "xor al, 0");
        checkOpcode32(new short[] {0x35, 0x40, 0x30, 0x20, 0x10}, "xor eax, 10203040h");
    }

    @Test
    public void testJMPCall() {
        checkOpcode32(new short[] {0xE8, 0x10, 0x20, 0x30, 0x40}, "call 40302015h");
        checkOpcode32(new short[] {0x9A, 0x10, 0x20, 0x30, 0x40, 0x10, 0x20}, "callf 2010h:40302010h");
        checkOpcode32(new short[] {0xFF, 0x10}, "call dword ptr [eax]");
        checkOpcode32(new short[] {0xFF, 0x18}, "callf dword ptr [eax]");
        checkOpcode32(new short[] {0xEB, 0x01}, "jmp 3h");
        checkOpcode32(new short[] {0xE9, 0x01, 0x00, 0x00, 0x00}, "jmp 6h");
        checkOpcode32(new short[] {0xEA, 0x10, 0x20, 0x30, 0x40, 0x10, 0x20}, "jmpf 2010h:40302010h");
        checkOpcode32(new short[] {0xFF, 0x20}, "jmp dword ptr [eax]");
        checkOpcode32(new short[] {0xFF, 0x28}, "jmpf dword ptr [eax]");
    }

    @Test
    public void testSSE() {
        checkOpcode32(new short[] {0x0F, 0x10, 0x00}, "movups xmm0, xmmword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x11, 0x00}, "movups xmmword ptr [eax], xmm0");
        checkOpcode32(new short[] {0x0F, 0x14, 0x00}, "unpcklps xmm0, qword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x15, 0x00}, "unpckhps xmm0, qword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x12, 0xC0}, "movhlps xmm0, xmm0");
        checkOpcode32(new short[] {0x0F, 0x13, 0x00}, "movlps qword ptr [eax], xmm0");
        checkOpcode32(new short[] {0x0F, 0x12, 0x00}, "movlps xmm0, qword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x16, 0xC0}, "movlhps xmm0, xmm0");
        checkOpcode32(new short[] {0x0F, 0x16, 0x00}, "movhps xmm0, qword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x17, 0x00}, "movhps qword ptr [eax], xmm0");
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
        checkOpcode32(new short[] {0xA8, 0xFF}, "test al, FFh");
        checkOpcode32(new short[] {0xE4, 0xFF}, "in al, FFh");
        checkOpcode32(new short[] {0xE5, 0xFF}, "in eax, FFh");
        checkOpcode32(new short[] {0xEC}, "in al, dx");
        checkOpcode32(new short[] {0xED}, "in eax, dx");
        checkOpcode32(new short[] {0xE6, 0xFF}, "out FFh, al");
        checkOpcode32(new short[] {0xE7, 0xFF}, "out FFh, eax");
        checkOpcode32(new short[] {0xEE}, "out dx, al");
        checkOpcode32(new short[] {0xEF}, "out dx, eax");
        checkOpcode32(new short[] {0xE2, 0x02}, "loop 4h");
        checkOpcode32(new short[] {0xE1, 0x02}, "loopz 4h");
        checkOpcode32(new short[] {0xE0, 0x02}, "loopnz 4h");
        // checkOpcode32(new short[] {0xE3, 0x02}, "jecxz 4h");
        checkOpcode32(new short[] {0x67, 0xE3, 0x02}, "jcxz 5h");
        checkOpcode32(new short[] {0xC8, 0x10, 0x20, 0x30}, "enter 2010h, 30h");

        // checkOpcode32(new short[] {0x0F, 0x05}, "loadall");
        // checkOpcode32(new short[] {0x0F, 0x07}, "loadall");
        checkOpcode32(new short[] {0x0F, 0x06}, "clts");
        checkOpcode32(new short[] {0x0F, 0x37}, "getsec");
        checkOpcode32(new short[] {0x0F, 0xA2}, "cpuid");
        checkOpcode32(new short[] {0x0F, 0x08}, "invd");
        checkOpcode32(new short[] {0x0F, 0x09}, "wbinvd");
        checkOpcode32(new short[] {0xF3, 0x90}, "pause");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC1}, "vmcall");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC2}, "vmlaunch");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC3}, "vmresume");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC4}, "vmxoff");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC8}, "monitor");
        checkOpcode32(new short[] {0x0F, 0x01, 0xC9}, "mwait");
        checkOpcode32(new short[] {0x0F, 0x01, 0xD0}, "xgetbv");
        checkOpcode32(new short[] {0x0F, 0x01, 0xD1}, "xsetbv");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xD8}, "vmrun");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xD9}, "vmcall");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDA}, "vmload");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDB}, "vmsave");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDC}, "stgi");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDD}, "clgi");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDE}, "skinit");
        // checkOpcode32(new short[] {0x0F, 0x01, 0xDF}, "invlpga");
        checkOpcode32(new short[] {0x0F, 0x78, 0x00}, "vmread dword ptr [eax], eax");
        checkOpcode32(new short[] {0x0F, 0x79, 0x00}, "vmwrite eax, dword ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x01, 0xF9}, "rdtscp");

        checkOpcode32(new short[] {0x0F, 0x02, 0x00}, "lar eax, word ptr [eax]");
        checkOpcode32(new short[] {0x0F, 0x03, 0x00}, "lsl eax, word ptr [eax]");
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
