package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.AddressSize;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand;
import org.solhost.folko.dasm.xml.OpcodeOperand.AddressType;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class ModRM {
    private final short codedMod;
    private final short codedReg;
    private final short codedMem;
    private final X86Context ctx;
    private final ByteSequence seq;

    public ModRM(ByteSequence seq, X86Context ctx) {
        this.seq = seq;
        this.ctx = ctx;
        short code = seq.readUByte();

        codedMod = (short) (code >> 6);

        if(ctx.hasRexRPrefix()) {
            codedReg = (short) (((code >> 3) & 0x07) | 8);
        } else {
            codedReg = (short) ((code >> 3) & 0x07);
        }

        if(ctx.hasRexBPrefix()) {
            codedMem = (short) ((code & 0x07) | 8);
        } else {
            codedMem = (short) (code & 0x07);
        }
    }

    public Operand getReg(OpcodeOperand op) {
        Register reg = X86CPU.getOperandRegister(op, ctx, codedReg);
        return new RegisterOp(op.usageType, reg);
    }

    public Operand getMem(OpcodeOperand op) {
        AddressSize addrSize = X86CPU.getAddressSize(ctx);
        switch(addrSize) {
        case A16:   return getMem16(op);
        case A32:   return getMem32(op);
        case A64:   return getMem32(op);
        default:    throw new UnsupportedOperationException("invalid address size: " + addrSize);
        }
    }

    public Operand getMem16(OpcodeOperand op) {
        OperandType operType = op.operType;
        if(operType == null) {
            if(op.adrType == AddressType.MOD_RM_M_FORCE) {
                operType = OperandType.WORD;
            } else {
                throw new UnsupportedOperationException("invalid address type: " + op.adrType);
            }
        }
        Register baseReg = null, indexReg = null;
        switch(codedMem) {
        case 0: baseReg = Register.BX; indexReg = Register.SI; break;
        case 1: baseReg = Register.BX; indexReg = Register.DI; break;
        case 2: baseReg = Register.BP; indexReg = Register.SI; break;
        case 3: baseReg = Register.BP; indexReg = Register.DI; break;
        case 4: baseReg = Register.SI; break;
        case 5: baseReg = Register.DI; break;
        case 6: baseReg = Register.BP; break;
        case 7: baseReg = Register.BX; break;
        default: throw new UnsupportedOperationException("invalid mem type: " + codedMem);
        }

        switch(codedMod) {
        case 0: {
            if(codedMem != 6) {
                PointerOp res = new PointerOp(ctx, baseReg, 1, indexReg);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            } else {
                long disp = seq.readSWord();
                PointerOp res = new PointerOp(ctx, disp);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        }
        case 1: {
            long disp = seq.readSByte();
            PointerOp res = new PointerOp(ctx, baseReg, 1, indexReg, disp);
            res.setOpType(operType);
            res.setUsage(op.usageType);
            return res;
        }
        case 2: {
            long disp = seq.readSWord();
            PointerOp res = new PointerOp(ctx, baseReg, 1, indexReg, disp);
            res.setOpType(operType);
            res.setUsage(op.usageType);
            return res;
        }
        case 3: {
            return new RegisterOp(op.usageType, X86CPU.getOperandRegister(op, ctx, codedMem));
        }
        default: throw new UnsupportedOperationException("invalid mode: " + codedMod);
        }
    }

    public Operand getMem32(OpcodeOperand op) {
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
            if(codedMem == 4 || codedMem == 12) {
                SIB sib = new SIB(seq, op, codedMod, ctx);
                return sib.getOp();
            } else if(codedMem == 5 || codedMem == 13) {
                long disp = seq.readUDword();
                PointerOp res = new PointerOp(ctx, disp);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            } else {
                PointerOp res;
                res = new PointerOp(ctx, X86CPU.getGenericAddressRegister(ctx, codedMem));
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 1:
            if(codedMem == 4 || codedMem == 12) {
                SIB sib = new SIB(seq, op, codedMod, ctx);
                PointerOp pOp = sib.getOp();
                if(!pOp.hasOffset()) {
                    pOp.setOffset(seq.readUByte());
                }
                return pOp;
            } else {
                long disp = seq.readSByte();
                PointerOp res;
                res = new PointerOp(ctx, X86CPU.getGenericAddressRegister(ctx, codedMem), disp);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 2:
            if(codedMem == 4 || codedMem == 12) {
                SIB sib = new SIB(seq, op, codedMod, ctx);
                PointerOp pOp = sib.getOp();
                if(!pOp.hasOffset()) {
                    pOp.setOffset(seq.readUDword());
                }
                return pOp;
            } else {
                long disp = seq.readSDword();
                PointerOp res;
                res = new PointerOp(ctx, X86CPU.getGenericAddressRegister(ctx, codedMem), disp);
                res.setOpType(operType);
                res.setUsage(op.usageType);
                return res;
            }
        case 3:
            return new RegisterOp(op.usageType, X86CPU.getOperandRegister(op, ctx, codedMem));
        default:
            throw new UnsupportedOperationException("unsupported mode: " + codedMod);
        }
    }
}
