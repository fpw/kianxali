package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.xml.OpcodeOperand;
import org.solhost.folko.dasm.xml.OpcodeOperand.AddressType;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class SIB {
    private PointerOp sibOp;

    public SIB(ByteSequence seq, OpcodeOperand op, short mode, Context ctx) {
        OperandType operType = op.operType;
        if(operType == null) {
            if(op.adrType == AddressType.MOD_RM_M_FORCE) {
                operType = OperandType.WORD_DWORD_64;
            } else {
                throw new UnsupportedOperationException("invalid address type: " + op.adrType);
            }
        }
        short sib = seq.readUByte();
        int scale = 1 << (sib >> 6);
        short index = (short) ((sib >> 3) & 0x07);
        short base = (short) (sib & 0x07);

        Register indexReg;
        if(index == 4) {
            indexReg = null;
        } else {
            indexReg =  X86CPU.getGenericRegister32(index);
        }

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
            Register baseReg = X86CPU.getGenericRegister32(base);
            sibOp = new PointerOp(ctx, baseReg, scale, indexReg);
        }
        sibOp.setOpType(operType);
        sibOp.setUsage(op.usageType);
    }

    public PointerOp getOp() {
        return sibOp;
    }
}