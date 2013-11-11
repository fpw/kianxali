package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand;
import org.solhost.folko.dasm.xml.OpcodeOperand.AddressType;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class ModRM {
    private final short codedMod;
    private final short codedReg;
    private final short codedMem;
    private final Context ctx;
    private final ByteSequence seq;

    public ModRM(ByteSequence seq, Context ctx) {
        this.seq = seq;
        this.ctx = ctx;
        short code = seq.readUByte();
        if(ctx.hasRexBPrefix()) {
            codedMod = (short) ((code >> 6) | 4);
        } else {
            codedMod = (short) (code >> 6);
        }
        if(ctx.hasRexRPrefix()) {
            codedReg = (short) (((code >> 3) & 0x07) | 8);
        } else {
            codedReg = (short) ((code >> 3) & 0x07);
        }
        codedMem = (short) (code & 0x07);
    }

    public Operand getReg(OpcodeOperand op) {
        switch(op.adrType) {
        case MOD_RM_R:
            Register reg = X86CPU.getGenericRegister(op.operType, ctx, codedReg);
            return new RegisterOp(op.usageType, reg);
        default:
            throw new UnsupportedOperationException("invalid adrType: " + op.adrType);
        }
    }

    public Operand getMem(OpcodeOperand op) {
        OperandType operType = op.operType;
        if(operType == null) {
            if(op.adrType == AddressType.MOD_RM_M_FORCE) {
                operType = OperandType.WORD_DWORD_64;
            } else {
                throw new UnsupportedOperationException("invalid address type: " + op.adrType);
            }
        }
        switch(codedMod) {
        case 0:
            if(codedMem == 4) {
                SIB sib = new SIB(seq, op, codedMod, ctx);
                return sib.getOp();
            } else if(codedMem == 5) {
                long disp = seq.readUDword();
                PointerOp res = new PointerOp(ctx, disp);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            } else {
                PointerOp res;
                if(ctx.hasAdrSizePrefix()) {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister16(codedMem));
                } else {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister32(codedMem));
                }
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 1:
            if(codedMem == 4) {
                SIB sib = new SIB(seq, op, codedMod, ctx);
                PointerOp pOp = sib.getOp();
                pOp.addOffset(seq.readUByte());
                return pOp;
            } else {
                long disp = seq.readSByte();
                PointerOp res;
                if(ctx.hasAdrSizePrefix()) {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister16(codedMem), disp);
                } else {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister32(codedMem), disp);
                }
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 2:
            if(codedMem == 4) {
                SIB sib = new SIB(seq, op, codedMem, ctx);
                PointerOp pOp = sib.getOp();
                pOp.addOffset(seq.readUDword());
                return pOp;
            } else {
                long disp = seq.readSDword();
                PointerOp res;
                if(ctx.hasAdrSizePrefix()) {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister16(codedMem), disp);
                } else {
                    res = new PointerOp(ctx, X86CPU.getGenericRegister32(codedMem), disp);
                }
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 3:
            return new RegisterOp(op.usageType, X86CPU.getGenericRegister(op.operType, ctx, codedMem));
        default:
            throw new UnsupportedOperationException("unsupported mode: " + codedMod);
        }
    }
}
