package kianxali.decoder.arch.x86;

import kianxali.decoder.arch.x86.X86CPU.X86Register;
import kianxali.decoder.arch.x86.xml.OperandDesc;
import kianxali.loader.ByteSequence;

/**
 * This class is used to parse a SIB byte that can follow a ModR/M byte.
 * @author fwi
 *
 */
class SIB {
    private PointerOp sibOp;

    public SIB(ByteSequence seq, OperandDesc op, short mode, X86Context ctx) {
        short sib = seq.readUByte();
        int scale = 1 << (sib >> 6);
        short index = (short) ((sib >> 3) & 0x07);
        if(ctx.getPrefix().rexXPrefix) {
            index |= 8;
        }
        short base = (short) (sib & 0x07);
        if(ctx.getPrefix().rexBPrefix) {
            base |= 8;
        }

        X86Register indexReg;
        if(index == 4) {
            indexReg = null;
        } else {
            indexReg =  X86CPU.getGenericAddressRegister(ctx, index);
        }

        long disp;
        if(base == 5 || base == 13) {
            switch(mode) {
            case 0:
                disp = seq.readSDword();
                sibOp = new PointerOp(ctx, scale, indexReg, disp);
                break;
            case 1:
                disp = seq.readSByte();
                sibOp = new PointerOp(ctx, X86Register.EBP, scale, indexReg, disp);
                break;
            case 2:
                disp = seq.readSDword();
                sibOp = new PointerOp(ctx, X86Register.EBP, scale, indexReg, disp);
                break;
            default:
                throw new RuntimeException("invalid base: " + mode);
            }
        } else {
            X86Register baseReg = X86CPU.getGenericAddressRegister(ctx, base);
            sibOp = new PointerOp(ctx, baseReg, scale, indexReg);
        }
        sibOp.setOpType(op.operType);
        sibOp.setUsage(op.usageType);
    }

    public PointerOp getOp() {
        return sibOp;
    }
}