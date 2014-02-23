package kianxali.cpu.x86;

import kianxali.cpu.x86.xml.OperandDesc;
import kianxali.cpu.x86.xml.OperandDesc.OperandType;
import kianxali.decoder.Register;

public final class X86CPU {
    public enum Model {
        I8086, I80186, I80286, I80386, I80486,
        PENTIUM, PENTIUM_MMX, PENTIUM_PRO, PENTIUM_II, PENTIUM_III, PENTIUM_IV,
        CORE_1, CORE_2, CORE_I7,
        ITANIUM,
        ANY
    }

    // Utility class, no constructor
    private X86CPU() {

    }

    public enum OperandSize {
        O8, O16, O32, O64, O80, O128, O512
    }

    public enum AddressSize {
        A16, A32, A64
    }

    public enum X86Register implements Register {
        // generic 8 bit
        AL, AH, BL, BH, CL, CH, DL, DH,

        // generic 16 bit
        AX, BX, CX, DX, BP, SP, SI, DI,

        // generic 32 bit
        EAX, EBX, ECX, EDX, EBP, ESP, ESI, EDI,

        // generic 64 bit
        RAX, RBX, RCX, RDX, RSP, RBP, RSI, RDI,
        R8, R9, R10, R11, R12, R13, R14, R15,
        // lower 8 bit
        SIL, DIL, BPL, SPL,
        R8B, R9B, R10B, R11B, R12B, R13B, R14B, R15B,
        // lower 16 bit
        R8W, R9W, R10W, R11W, R12W, R13W, R14W, R15W,
        // lower 32 bit
        R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D,

        // segment registers
        CS, DS, ES, FS, GS, SS,

        // Control registers
        CR0, CR2, CR3, CR4,

        // Debug registers
        DR0, DR1, DR2, DR3, DR4, DR5, DR6, DR7,

        // Test registers
        TR0, TR1, TR2, TR3, TR4, TR5, TR6, TR7,

        // FPU registers
        ST0, ST1, ST2, ST3, ST4, ST5, ST6, ST7,

        // MMX registers (are actually aliases for FPU registers)
        MM0, MM1, MM2, MM3, MM4, MM5, MM6, MM7,

        // SSE registers
        XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7, XMM8, XMM9, XMM10, XMM11, XMM12, XMM13, XMM14, XMM15
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

    public static AddressSize getAddressSize(X86Context ctx) {
        switch(ctx.getExecMode()) {
        case SMM:
            // TODO: not sure if fall-through to long mode is correct here
        case LONG:
            if(ctx.getPrefix().adrSizePrefix) {
                return AddressSize.A32;
            } else {
                return AddressSize.A64;
            }
        case PROTECTED:
            if(ctx.getPrefix().adrSizePrefix) {
                return AddressSize.A16;
            } else {
                return AddressSize.A32;
            }
        case REAL:
            return AddressSize.A16;
        default:
            throw new RuntimeException("invalid cpu mode: " + ctx.getExecMode());
        }
    }

    private static OperandSize getDefaultOperandSize(X86Context ctx) {
        switch(ctx.getExecMode()) {
        case SMM:
            // TODO: not sure if fall-through to long mode is correct here
        case LONG:
            if(ctx.getPrefix().rexWPrefix) {
                return OperandSize.O64;
            } else if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        case PROTECTED:
            if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        case REAL:
            return OperandSize.O16;
        default:
            throw new RuntimeException("invalid cpu mode: " + ctx.getExecMode());
        }
    }

    public static OperandSize getOperandSize(X86Context ctx, OperandType opType) {
        switch(opType) {
        case BYTE:
            return OperandSize.O8;
        case WORD_OPS:      // TODO: check
        case WORD_FPU:
        case WORD:
            return OperandSize.O16;
        case WORD_DWORD_64:
        case WORD_DWORD_S64:
            return getDefaultOperandSize(ctx);
        case WORD_DWORD:
            if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        case DWORD_QWORD:
            if(ctx.getPrefix().rexWPrefix) {
                return OperandSize.O64;
            } else {
                return OperandSize.O32;
            }
        case POINTER_REX:
            if(ctx.getPrefix().rexWPrefix) {
                return OperandSize.O80;
            } else if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        case TWO_INDICES:
            if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O32;
            } else {
                return OperandSize.O64;
            }
        case POINTER:
            if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        case DWORD_INT_FPU:
        case REAL_SINGLE_FPU:
            return OperandSize.O32;
        case DOUBLE_FPU:
        case QWORD_FPU:
            return OperandSize.O64;
        case DWORD:
            return OperandSize.O32;
        case QWORD:
        case QWORD_MMX:
            return OperandSize.O64;
        case QWORD_WORD:
            if(ctx.getPrefix().opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O64;
            }
        case SINGLE_64:
        case SCALAR_DOUBLE:
            return OperandSize.O64;
        case SCALAR_SINGLE:
            return OperandSize.O32;
        case DQWORD:
        case DOUBLE_128:
        case SINGLE_128:
            return OperandSize.O128;
        case REAL_EXT_FPU:
            return OperandSize.O80;
        case FPU_SIMD_STATE:
            return OperandSize.O512;
        default:
            throw new UnsupportedOperationException("invalid operand type: " + opType);
        }
    }

    private static X86Register getGenericRegister8(short id) {
        switch(id) {
        case 0: return X86Register.AL;
        case 1: return X86Register.CL;
        case 2: return X86Register.DL;
        case 3: return X86Register.BL;
        case 4: return X86Register.AH;
        case 5: return X86Register.CH;
        case 6: return X86Register.DH;
        case 7: return X86Register.BH;
        case 8: return X86Register.R8B;
        case 9: return X86Register.R9B;
        case 10:return X86Register.R10B;
        case 11:return X86Register.R11B;
        case 12:return X86Register.R12B;
        case 13:return X86Register.R13B;
        case 14:return X86Register.R14B;
        case 15:return X86Register.R15B;
        default:
            throw new UnsupportedOperationException("invalid generic 8 bit register: " + id);
        }
    }

    private static X86Register getGenericRegister16(short id) {
        switch(id) {
        case 0: return X86Register.AX;
        case 1: return X86Register.CX;
        case 2: return X86Register.DX;
        case 3: return X86Register.BX;
        case 4: return X86Register.SP;
        case 5: return X86Register.BP;
        case 6: return X86Register.SI;
        case 7: return X86Register.DI;
        case 8: return X86Register.R8W;
        case 9: return X86Register.R9W;
        case 10:return X86Register.R10W;
        case 11:return X86Register.R11W;
        case 12:return X86Register.R12W;
        case 13:return X86Register.R13W;
        case 14:return X86Register.R14W;
        case 15:return X86Register.R15W;
        default:
            throw new UnsupportedOperationException("invalid generic 16 bit register: " + id);
        }
    }

    private static X86Register getGenericRegister32(short id) {
        switch(id) {
        case 0: return X86Register.EAX;
        case 1: return X86Register.ECX;
        case 2: return X86Register.EDX;
        case 3: return X86Register.EBX;
        case 4: return X86Register.ESP;
        case 5: return X86Register.EBP;
        case 6: return X86Register.ESI;
        case 7: return X86Register.EDI;
        case 8: return X86Register.R8D;
        case 9: return X86Register.R9D;
        case 10:return X86Register.R10D;
        case 11:return X86Register.R11D;
        case 12:return X86Register.R12D;
        case 13:return X86Register.R13D;
        case 14:return X86Register.R14D;
        case 15:return X86Register.R15D;
        default:
            throw new UnsupportedOperationException("invalid generic 32 bit register: " + id);
        }
    }

    private static X86Register getGenericRegister64(short id) {
        switch(id) {
        case 0: return X86Register.RAX;
        case 1: return X86Register.RCX;
        case 2: return X86Register.RDX;
        case 3: return X86Register.RBX;
        case 4: return X86Register.RSP;
        case 5: return X86Register.RBP;
        case 6: return X86Register.RSI;
        case 7: return X86Register.RDI;
        case 8: return X86Register.R8;
        case 9: return X86Register.R9;
        case 10:return X86Register.R10;
        case 11:return X86Register.R11;
        case 12:return X86Register.R12;
        case 13:return X86Register.R13;
        case 14:return X86Register.R14;
        case 15:return X86Register.R15;
        default:
            throw new UnsupportedOperationException("invalid generic 64 bit register: " + id);
        }
    }

    private static X86Register getSegmentRegister(short id) {
        switch(id & 0x7) {
        case 0: return X86Register.ES;
        case 1: return X86Register.CS;
        case 2: return X86Register.SS;
        case 3: return X86Register.DS;
        case 4: return X86Register.FS;
        case 5: return X86Register.GS;
        default:
            throw new UnsupportedOperationException("invalid segment register: " + id);
        }
    }

    private static X86Register getFPURegister(short id) {
        switch(id & 0x07) {
        case 0: return X86Register.ST0;
        case 1: return X86Register.ST1;
        case 2: return X86Register.ST2;
        case 3: return X86Register.ST3;
        case 4: return X86Register.ST4;
        case 5: return X86Register.ST5;
        case 6: return X86Register.ST6;
        case 7: return X86Register.ST7;
        default:
            throw new UnsupportedOperationException("invalid FPU register: " + id);
        }
    }

    private static X86Register getMMXRegister(short id) {
        switch(id & 0x07) {
        case 0: return X86Register.MM0;
        case 1: return X86Register.MM1;
        case 2: return X86Register.MM2;
        case 3: return X86Register.MM3;
        case 4: return X86Register.MM4;
        case 5: return X86Register.MM5;
        case 6: return X86Register.MM6;
        case 7: return X86Register.MM7;
        default:
            throw new UnsupportedOperationException("invalid MMX register: " + id);
        }
    }

    private static X86Register getXMMRegister(short id) {
        switch(id) {
        case 0: return X86Register.XMM0;
        case 1: return X86Register.XMM1;
        case 2: return X86Register.XMM2;
        case 3: return X86Register.XMM3;
        case 4: return X86Register.XMM4;
        case 5: return X86Register.XMM5;
        case 6: return X86Register.XMM6;
        case 7: return X86Register.XMM7;
        case 8: return X86Register.XMM8;
        case 9: return X86Register.XMM9;
        case 10: return X86Register.XMM10;
        case 11: return X86Register.XMM11;
        case 12: return X86Register.XMM12;
        case 13: return X86Register.XMM13;
        case 14: return X86Register.XMM14;
        case 15: return X86Register.XMM15;
        default:
            throw new UnsupportedOperationException("invalid XMM register: " + id);
        }
    }

    private static X86Register getControlRegister(short id) {
        switch(id) {
        case 0: return X86Register.CR0;
        case 2: return X86Register.CR2;
        case 3: return X86Register.CR3;
        case 4: return X86Register.CR4;
        default:
            throw new UnsupportedOperationException("invalid control register: " + id);
        }
    }

    private static X86Register getDebugRegister(short id) {
        switch(id) {
        case 0: return X86Register.DR0;
        case 1: return X86Register.DR1;
        case 2: return X86Register.DR2;
        case 3: return X86Register.DR3;
        case 4: return X86Register.DR4;
        case 5: return X86Register.DR5;
        case 6: return X86Register.DR6;
        case 7: return X86Register.DR7;
        default:
            throw new UnsupportedOperationException("invalid debug register: " + id);
        }
    }

    private static X86Register getTestRegister(short id) {
        switch(id) {
        case 0: return X86Register.TR0;
        case 1: return X86Register.TR1;
        case 2: return X86Register.TR2;
        case 3: return X86Register.TR3;
        case 4: return X86Register.TR4;
        case 5: return X86Register.TR5;
        case 6: return X86Register.TR6;
        case 7: return X86Register.TR7;
        default:
            throw new UnsupportedOperationException("invalid test register: " + id);
        }
    }

    public static X86Register getGenericAddressRegister(X86Context ctx, short id) {
        if(ctx.getExecMode() != ExecutionMode.LONG && id > 7) {
            throw new UnsupportedOperationException("used 64 bit register id in 32 bit mode");
        }
        AddressSize adrSize = getAddressSize(ctx);
        switch(adrSize) {
        case A16:   return getGenericRegister16(id);
        case A32:   return getGenericRegister32(id);
        case A64:   return getGenericRegister64(id);
        default:    throw new UnsupportedOperationException("invalid adrSize: " + adrSize);
        }
    }

    private static X86Register getOperandRegisterGeneral(OperandDesc op, X86Context ctx, short id) {
        if(ctx.getExecMode() != ExecutionMode.LONG && id > 7) {
            throw new UnsupportedOperationException("used 64 bit register id in 32 bit mode");
        }
        OperandSize opSize = getOperandSize(ctx, op.operType);
        switch(opSize) {
        case O8:    return getGenericRegister8(id);
        case O16:   return getGenericRegister16(id);
        case O32:   return getGenericRegister32(id);
        case O64:   return getGenericRegister64(id);
        default:    throw new UnsupportedOperationException("invalid opSize: " + opSize);
        }
    }

    public static X86Register getOperandRegister(OperandDesc op, X86Context ctx, short id) {
        switch(op.adrType) {
        case MOD_RM_R:
        case MOD_RM_M:
        case LEAST_REG:
            return getOperandRegisterGeneral(op, ctx, id);
        case MOD_RM_R_SEG:
            return getSegmentRegister(id);
        case MOD_RM_M_FPU:
        case MOD_RM_M_FPU_REG:
            return getFPURegister(id);
        case MOD_RM_MMX:
        case MOD_RM_R_MMX:
        case MOD_RM_M_MMX:
            return getMMXRegister(id);
        case MOD_RM_M_XMM_REG:
        case MOD_RM_XMM:
        case MOD_RM_R_XMM:
            return getXMMRegister(id);
        case SEGMENT2:
            return getSegmentRegister((short) ((id >> 3) & 0x3));
        case SEGMENT33:
            return getSegmentRegister((short) ((id >> 3) & 0x7));
        case MOD_RM_R_FORCE_GEN:
            return getGenericAddressRegister(ctx, id);
        case MOD_RM_R_DEBUG:
            return getDebugRegister(id);
        case MOD_RM_R_CTRL:
            return getControlRegister(id);
        case MOD_RM_R_TEST:
            return getTestRegister(id);
        case GROUP:
            switch(op.directGroup) {
            case GENERIC:   return getOperandRegisterGeneral(op, ctx, id);
            case X87FPU:    return getFPURegister(id);
            default:        throw new UnsupportedOperationException("invalid directGroup: " + op.directGroup);
            }
        default:
            throw new UnsupportedOperationException("invalid adrType: " + op.adrType);
        }
    }
}
