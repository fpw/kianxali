package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.Context.AddressSize;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.PointerOp;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class ModRM {
    private Operand regOp, memOp;
    private final short mode;

    public ModRM(ByteSequence seq, Context ctx) {
        if(ctx.getAddressSize() != AddressSize.A32) {
            // TODO
            throw new UnsupportedOperationException("only modRM32 supported");
        }
        short code = seq.readUByte();

        mode = (short) (code >> 6);
        short op2 = (short) (code & 0x07);
        short op1 = (short) ((code >> 3) & 0x07);
        switch(mode) {
        case 0:
            if(op2 == 4) {
                SIB sib = new SIB(seq, mode, ctx);
                memOp = sib.getOp();
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            } else if(op2 == 5) {
                long disp = seq.readUDword();
                memOp = new PointerOp(ctx, disp);
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            } else {
                memOp = new PointerOp(ctx, Register.fromByte(op2, ctx, false));
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            }
            break;
        case 1:
            if(op2 == 4) {
                SIB sib = new SIB(seq, mode, ctx);
                memOp = sib.getOp();
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            } else {
                long disp = seq.readSByte();
                memOp = new PointerOp(ctx, Register.fromByte(op2, ctx, false), disp);
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            }
            break;
        case 2:
            if(op2 == 4) {
                SIB sib = new SIB(seq, mode, ctx);
                memOp = sib.getOp();
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            } else {
                long disp = seq.readSDword();
                memOp = new PointerOp(ctx, Register.fromByte(op2, ctx, false), disp);
                regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            }
            break;
        case 3:
            regOp = new RegisterOp(Register.fromByte(op1, ctx, true));
            memOp = new RegisterOp(Register.fromByte(op2, ctx, true));
            break;
        }
    }

    public Operand getRegOp() {
        return regOp;
    }

    public Operand getMemOp() {
        return memOp;
    }
}
