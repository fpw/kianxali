package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class X86CPU {
    public enum Model {
        I8086, I80186, I80286, I80386, I80486,
        PENTIUM, PENTIUM_MMX, PENTIUM_PRO, PENTIUM_II, PENTIUM_III, PENTIUM_IV,
        CORE_1, CORE_2, CORE_I7,
        ITANIUM
    }

    public enum Register {
        // generic 8 bit
        AL, AH, BL, BH, CL, CH, DL, DH,

        // generic 16 bit
        AX, BX, CX, DX, BP, SP, SI, DI,

        // generic 32 bit
        EAX, EBX, ECX, EDX, EBP, ESP, ESI, EDI,

        // generic 64 bit
        RAX, RBX, RCX, RDX, RSP, RBP, RSI, RDI,
    }

    public enum ExecutionMode {
        REAL, PROTECTED, LONG, SMM
    }

    public enum Segment {
        CS, DS, SS, ES, FS, GS
    }

    public enum InstructionSetExtension {
        MMX, SMX, VMX,
        SSE_1, SSE_2, SSE_3, SSE_4_1, SSE_4_2, SSSE_3
    }

    public static Register getGenericRegister8(short id) {
        switch(id) {
        case 0: return Register.AL;
        case 1: return Register.CL;
        case 2: return Register.DL;
        case 3: return Register.BL;
        case 4: return Register.AH;
        case 5: return Register.CH;
        case 6: return Register.DH;
        case 7: return Register.BH;
        default:
            throw new UnsupportedOperationException("invalid generic 8 bit register: " + id);
        }
    }

    public static Register getGenericRegister16(short id) {
        switch(id) {
        case 0: return Register.AX;
        case 1: return Register.CX;
        case 2: return Register.DX;
        case 3: return Register.BX;
        case 4: return Register.SP;
        case 5: return Register.BP;
        case 6: return Register.SI;
        case 7: return Register.DI;
        default:
            throw new UnsupportedOperationException("invalid generic 16 bit register: " + id);
        }
    }

    public static Register getGenericRegister32(short id) {
        switch(id) {
        case 0: return Register.EAX;
        case 1: return Register.ECX;
        case 2: return Register.EDX;
        case 3: return Register.EBX;
        case 4: return Register.ESP;
        case 5: return Register.EBP;
        case 6: return Register.ESI;
        case 7: return Register.EDI;
        default:
            throw new UnsupportedOperationException("invalid generic 32 bit register: " + id);
        }
    }

    public static Register getGenericRegister64(short id) {
        switch(id) {
        case 0: return Register.RAX;
        case 1: return Register.RCX;
        case 2: return Register.RDX;
        case 3: return Register.RBX;
        case 4: return Register.RSP;
        case 5: return Register.RBP;
        case 6: return Register.RSI;
        case 7: return Register.RDI;
        default:
            throw new UnsupportedOperationException("invalid generic 64 bit register: " + id);
        }
    }

    public static Register getGenericRegister(OperandType opType, Context ctx, short id) {
        switch(opType) {
        case WORD_DWORD_64:
            if(ctx.hasRexWPrefix()) {
                return getGenericRegister64(id);
            } else if(ctx.hasOpSizePrefix()) {
                return getGenericRegister16(id);
            } else {
                return getGenericRegister32(id);
            }
        case WORD_DWORD:
            if(ctx.hasOpSizePrefix()) {
                return getGenericRegister16(id);
            } else {
                return getGenericRegister32(id);
            }
        default:
            throw new UnsupportedOperationException("invalid generic register type: " + opType);
        }
    }
}
