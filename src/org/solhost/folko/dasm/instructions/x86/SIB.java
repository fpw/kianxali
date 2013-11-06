package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.PointerOp;

public class SIB {
    private PointerOp sibOp;

    public SIB(ByteSequence seq, short mode, Context ctx) {
        short sib = seq.readUByte();
        int scale = 1 << (sib >> 6);
        short index = (short) ((sib >> 3) & 0x07);
        short base = (short) (sib & 0x07);

        if(index == 4) {
            throw new RuntimeException("invalid index register");
        }
        Register indexReg = Register.fromByte(index, ctx, true);

        if(base == 5) {
            switch(mode) {
            case 0: {
                long disp = seq.readSDword();
                sibOp = new PointerOp(ctx, scale, indexReg, disp);
            } break;
            case 1: {
                long disp = seq.readSByte();
                sibOp = new PointerOp(ctx, Register.EBP, scale, indexReg, disp);
            } break;
            case 2: {
                long disp = seq.readSDword();
                sibOp = new PointerOp(ctx, Register.EBP, scale, indexReg, disp);
            } break;
            default:
                throw new RuntimeException("invalid base");
            }
        } else {
            Register baseReg = Register.fromByte(base, ctx, true);
            sibOp = new PointerOp(ctx, baseReg, scale, indexReg);
        }
    }

    public PointerOp getOp() {
        return sibOp;
    }
}
