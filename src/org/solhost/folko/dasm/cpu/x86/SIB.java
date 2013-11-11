package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class SIB {
    private PointerOp sibOp;

    public SIB(ByteSequence seq, OperandType opType, short mode, Context ctx) {
        short sib = seq.readUByte();
        int scale = 1 << (sib >> 6);
        short index = (short) ((sib >> 3) & 0x07);
        short base = (short) (sib & 0x07);

        if(index == 4) {
            throw new RuntimeException("invalid index register");
        }
        Register indexReg =  X86CPU.getGenericRegister(opType, ctx, index);

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
            Register baseReg = X86CPU.getGenericRegister(opType, ctx, base);
            sibOp = new PointerOp(ctx, baseReg, scale, indexReg);
        }
        sibOp.setOpType(opType);
    }

    public PointerOp getOp() {
        return sibOp;
    }
}