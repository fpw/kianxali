package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;

public enum Register {
    AL, AH, BL, BH, CL, CH, DL, DH,
    AX, BX, CX, DX, SI, DI, BP, SP,
    EAX, EBX, ECX, EDX, ESI, EDI, EBP, ESP,
    INVALID;

    public static Register fromByte(short b, Context ctx, boolean obeyOverride) {
        OperandSize size;
        if(obeyOverride && ctx.getOpSizeOverride() != null) {
            size = ctx.getOpSizeOverride();
        } else {
            size = ctx.getOperandSize();
        }
        switch(size) {
        case O8:  return fromR8(b);
        case O16: return fromR16(b);
        case O32: return fromR32(b);
        }
        throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
    }

    private static Register fromR8(short r8) {
        switch(r8) {
        case 0: return AL;
        case 1: return CL;
        case 2: return DL;
        case 3: return BL;
        case 4: return AH;
        case 5: return CH;
        case 6: return DH;
        case 7: return BH;
        default: return INVALID;
        }
    }

    private static Register fromR16(short r16) {
        switch(r16) {
        case 0: return AX;
        case 1: return CX;
        case 2: return DX;
        case 3: return BX;
        case 4: return SP;
        case 5: return BP;
        case 6: return SI;
        case 7: return DI;
        default: return INVALID;
        }
    }

    private static Register fromR32(short r32) {
        switch(r32) {
        case 0: return EAX;
        case 1: return ECX;
        case 2: return EDX;
        case 3: return EBX;
        case 4: return ESP;
        case 5: return EBP;
        case 6: return ESI;
        case 7: return EDI;
        default: return INVALID;
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
