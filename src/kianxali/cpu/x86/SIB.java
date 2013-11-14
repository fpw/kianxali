package kianxali.cpu.x86;

import kianxali.cpu.x86.X86CPU.Register;
import kianxali.cpu.x86.xml.OperandDesc;
import kianxali.cpu.x86.xml.OperandDesc.AddressType;
import kianxali.cpu.x86.xml.OperandDesc.OperandType;
import kianxali.image.ByteSequence;

public class SIB {
    private PointerOp sibOp;

    public SIB(ByteSequence seq, OperandDesc op, short mode, X86Context ctx) {
        OperandType operType = op.operType;
        if(operType == null) {
            if(op.adrType == AddressType.MOD_RM_MUST_M) {
                operType = OperandType.WORD_DWORD_64;
            } else {
                throw new UnsupportedOperationException("invalid address type: " + op.adrType);
            }
        }
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

        Register indexReg;
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
                sibOp = new PointerOp(ctx, Register.EBP, scale, indexReg, disp);
                break;
            case 2:
                disp = seq.readSDword();
                sibOp = new PointerOp(ctx, Register.EBP, scale, indexReg, disp);
                break;
            default:
                throw new RuntimeException("invalid base: " + mode);
            }
        } else {
            Register baseReg = X86CPU.getGenericAddressRegister(ctx, base);
            sibOp = new PointerOp(ctx, baseReg, scale, indexReg);
        }
        sibOp.setOpType(op.operType);
        sibOp.setUsage(op.usageType);
    }

    public PointerOp getOp() {
        return sibOp;
    }
}