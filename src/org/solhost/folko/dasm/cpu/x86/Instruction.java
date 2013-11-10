package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeSyntax;
import org.solhost.folko.dasm.xml.OpcodeOperand;

public class Instruction implements DecodedEntity {
    private final OpcodeSyntax syntax;
    private final List<Operand> operands;

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
    public void decode(ByteSequence seq, Context ctx) {
        OpcodeEntry entry = syntax.getOpcodeEntry();
        if(entry.modRM) {
            // TODO: parse mod r/m
            throw new UnsupportedOperationException("mod r/m not supported yet");
        }
        for(OpcodeOperand op : syntax.getOperands()) {
            Operand decodedOp = decodeOperand(op, seq, ctx);
            if(decodedOp != null) {
                operands.add(decodedOp);
            }
        }
    }

    private Operand decodeOperand(OpcodeOperand op, ByteSequence seq, Context ctx) {
        if(op.indirect) {
            return null;
        }
        switch(op.adrType) {
        case GROUP:                 return decodeGroup(op, ctx);
        case OFFSET:                return decodeOffset(seq, op, ctx);
        case LEAST_REG:             return decodeLeastReg(op, ctx);
        case MOD_RM_M:
        case MOD_RM_MMX:
        case MOD_RM_M_FORCE:
        case MOD_RM_M_FPU:
        case MOD_RM_M_MMX:
        case MOD_RM_M_XMM:
        case MOD_RM_R:
        case MOD_RM_R_FORCE:
        case MOD_RM_R_FORCE2:
        case MOD_RM_R_FPU:
        case MOD_RM_R_MMX:
        case MOD_RM_R_SEG:
        case MOD_RM_R_XMM:
        case MOD_RM_XMM:
            // TODO: modrm
            break;
        case DIRECT:
        case CONTROL:
        case DEBUG:
        case DS_EAX_AL_RBX:
        case DS_EAX_RAX:
        case DS_EDI_RDI:
        case DS_ESI_RSI:
        case ES_EDI_RDI:
        case FLAGS:
        case IMMEDIATE:
        case RELATIVE:
        case SEGMENT2:
        case SEGMENT30:
        case SEGMENT33:
        case STACK:
        case TEST:
        default:
            throw new UnsupportedOperationException("unsupported address type: " + op.adrType);
        }
        return null;
    }

    private Operand decodeOffset(ByteSequence seq, OpcodeOperand op, Context ctx) {
        long offset;
        switch(op.operType) {
        case WORD_DWORD_64:
            if(ctx.hasRexWPrefix()) {
                offset = seq.readSQword();
            } else {
                if(ctx.hasOpSizePrefix()) {
                    offset = seq.readUWord();
                } else {
                    offset = seq.readUDword();
                }
            }
            break;
        default:
            throw new UnsupportedOperationException("unsupported offset type: " + op.operType);
        }
        return new OffsetOp(op.usageType, offset, ctx.getOverrideSegment());
    }

    private Operand decodeLeastReg(OpcodeOperand op, Context ctx) {
        int idx = syntax.getEncodedRegisterPrefixIndex();
        short regId = (short) (ctx.getFromDecodedPrefix(idx) & 0x7);
        Register reg = X86CPU.getGenericRegister(op.operType, ctx, regId);
        return new RegisterOp(op.usageType, reg);
    }

    private Operand decodeGroup(OpcodeOperand op, Context ctx) {
        switch(op.directGroup) {
        case GENERIC:
            Register reg = X86CPU.getGenericRegister(op.operType, ctx, (short) op.numForGroup);
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

    public String asString(Object options) {
        StringBuilder res = new StringBuilder();
        res.append(syntax.getMnemonic().toLowerCase());
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

    @Override
    public String toString() {
        return asString(null);
    }
}
