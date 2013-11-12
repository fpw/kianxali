package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.OutputFormat;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeSyntax;
import org.solhost.folko.dasm.xml.OpcodeOperand;

public class Instruction implements DecodedEntity {
    private final OpcodeSyntax syntax;
    private final List<Operand> operands;
    private ModRM modRM;
    private List<Short> actualPrefix;

    public Instruction(OpcodeSyntax syntax) {
        this.syntax = syntax;
        this.operands = new ArrayList<>(5);
    }

    public boolean isPrefix() {
        return syntax.getOpcodeEntry().belongsTo(OpcodeGroup.PREFIX);
    }

    public OpcodeSyntax getSyntax() {
        return syntax;
    }

    public OpcodeEntry getOpcode() {
        return syntax.getOpcodeEntry();
    }

    // the prefix has been read from seq already
    public void decode(ByteSequence seq, X86Context ctx) {
        actualPrefix = ctx.getDecodedPrefix();
        OpcodeEntry entry = syntax.getOpcodeEntry();
        if(entry.modRM) {
            // TODO: parse mod r/m
            modRM = new ModRM(seq, ctx);
        }
        for(OpcodeOperand op : syntax.getOperands()) {
            Operand decodedOp = decodeOperand(op, seq, ctx);
            if(decodedOp != null) {
                operands.add(decodedOp);
            }
        }
    }

    private Operand decodeOperand(OpcodeOperand op, ByteSequence seq, X86Context ctx) {
        if(op.indirect) {
            return null;
        }
        switch(op.adrType) {
        case GROUP:                 return decodeGroup(op, ctx);
        case OFFSET:                return decodeOffset(seq, op, ctx);
        case LEAST_REG:             return decodeLeastReg(op, ctx);

        case MOD_RM_R_FORCE:
        case MOD_RM_R_FORCE2:
        case MOD_RM_R_FPU:
        case MOD_RM_R_MMX:
        case MOD_RM_R_SEG:
        case MOD_RM_R_XMM:
        case MOD_RM_R:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getReg(op);

        case MOD_RM_M_FORCE:
        case MOD_RM_M_FPU:
        case MOD_RM_M_MMX:
        case MOD_RM_M_XMM:
        case MOD_RM_M:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getMem(op);

        case DIRECT:
        case IMMEDIATE:             return decodeImmediate(seq, op, ctx);
        case RELATIVE:              return decoreRelative(seq, op, ctx);
        case ES_EDI_RDI:
            // TODO: RDI
            PointerOp res = new PointerOp(ctx, Register.EDI);
            res.setSegment(Segment.ES);
            res.setOpType(op.operType);
            res.setUsage(op.usageType);
            return res;
        case MOD_RM_MMX:
        case MOD_RM_XMM:
            // TODO: not sure about those two
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getMem(op);
        case SEGMENT2:
            Register reg = X86CPU.getOperandRegister(op, ctx, syntax.getOpcodeEntry().opcode);
            return new RegisterOp(op.usageType, reg);
        case SEGMENT30:
        case SEGMENT33:
        case CONTROL:
        case DEBUG:
        case DS_EAX_AL_RBX:
        case DS_EAX_RAX:
        case DS_EDI_RDI:
        case DS_ESI_RSI:
        case FLAGS:
        case STACK:
        case TEST:
        default:
            throw new UnsupportedOperationException("unsupported address type: " + op.adrType);
        }
    }

    private Operand decoreRelative(ByteSequence seq, OpcodeOperand op, X86Context ctx) {
        long relOffset;
        switch(op.operType) {
        case WORD_DWORD_S64:
            relOffset = seq.readSDword();
            break;
        case BYTE_SGN:
            relOffset = seq.readSByte();
            break;
        default:
            throw new UnsupportedOperationException("unsupported relative type: " + op.operType);
        }
        long baseAddr = seq.getPosition() - ctx.getFileOffset() + ctx.getVirtualAddress();
        return new RelativeOp(op.usageType, baseAddr, relOffset);
    }

    private Operand decodeImmediate(ByteSequence seq, OpcodeOperand op, X86Context ctx) {
        long immediate;
        if(op.operType == null) {
            if(op.hardcoded != null) {
                immediate = Long.parseLong(op.hardcoded, 16);
            } else {
                throw new UnsupportedOperationException("invalid immediate: " + op.adrType);
            }
        } else {
            switch(op.operType) {
            case BYTE:
                immediate = seq.readUByte();
                break;
            case BYTE_STACK:
                immediate = seq.readSByte();
                break;
            case BYTE_SGN:
                immediate = seq.readSByte();
                break;
            case WORD:
                immediate = seq.readUWord();
                break;
            case WORD_DWORD_STACK:
                immediate = seq.readSDword();
                break;
            case WORD_DWORD_64:
                immediate = seq.readUDword();
                break;
            case WORD_DWORD_S64:
                immediate = seq.readSDword();
                break;
            case POINTER:
                switch(X86CPU.getAddressSize(ctx)) {
                case A16: immediate = seq.readUWord(); break;
                case A32: immediate = seq.readUDword(); break;
                case A64: immediate = seq.readSQword(); break;
                default: throw new UnsupportedOperationException("unsupported pointer size: " + X86CPU.getAddressSize(ctx));
                } break;
            default:
                throw new UnsupportedOperationException("unsupported immediate type: " + op.operType);
            }
        }
        return new ImmediateOp(op.usageType, immediate);
    }

    private Operand decodeOffset(ByteSequence seq, OpcodeOperand op, X86Context ctx) {
        long offset;
        switch(op.operType) {
        case BYTE:
            offset = seq.readUDword();
            break;
        case WORD_DWORD_64:
            if(ctx.hasRexWPrefix()) {
                offset = seq.readSQword();
            } else {
                if(ctx.hasOpSizePrefix()) {
                    offset = seq.readUDword();
                } else {
                    offset = seq.readUDword();
                }
            }
            break;
        default:
            throw new UnsupportedOperationException("unsupported offset type: " + op.operType);
        }
        PointerOp res = new PointerOp(ctx, offset);
        res.setOpType(op.operType);
        res.setUsage(op.usageType);
        return res;
    }

    private Operand decodeLeastReg(OpcodeOperand op, X86Context ctx) {
        int idx = syntax.getEncodedRegisterPrefixIndex();
        short regId = (short) (ctx.getFromDecodedPrefix(idx) & 0x7);
        Register reg = X86CPU.getOperandRegister(op, ctx, regId);
        return new RegisterOp(op.usageType, reg);
    }

    private Operand decodeGroup(OpcodeOperand op, X86Context ctx) {
        switch(op.directGroup) {
        case GENERIC:
            Register reg = X86CPU.getOperandRegister(op, ctx, (short) op.numForGroup);
            return new RegisterOp(op.usageType, reg);
        case CONTROL:
        case DEBUG:
        case MMX:
        case MSR:
        case SEGMENT:
        case SYSTABP:
        case X87FPU:
        case XCR:
        case XMM:
        default:
            throw new UnsupportedOperationException("unsupported direct group: " + op.directGroup);
        }
    }

    public String asString(OutputFormat options) {
        StringBuilder res = new StringBuilder();
        if(options.isIncludePrefixBytes()) {
            for(Short b : actualPrefix) {
                res.append(String.format("%02X", b));
            }
            res.append("\t");
        }
        res.append(syntax.getMnemonic().toString().toLowerCase());
        for(int i = 0; i < operands.size(); i++) {
            if(i == 0) {
                res.append(" ");
            } else {
                res.append(", ");
            }
            res.append(operands.get(i).asString(options));
        }

        return res.toString();
    }
}
