package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.decoder.UsageType;
import org.solhost.folko.dasm.images.ByteSequence;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeOperand;
import org.solhost.folko.dasm.xml.OpcodeSyntax;

public class X86Instruction implements Instruction {
    private final long memAddr;
    private final OpcodeSyntax syntax;
    private final List<Operand> operands;
    private Prefix prefix;
    // the size is not known during while decoding operands, so this will cause a (desired) NullPointerException
    private Integer size;

    public X86Instruction(long memAddr, OpcodeSyntax syntax) {
        this.memAddr = memAddr;
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
        this.prefix = ctx.getPrefix();
        ModRM modRM = null;
        long operandPos = seq.getPosition();

        OpcodeEntry entry = syntax.getOpcodeEntry();
        if(entry.modRM) {
            modRM = new ModRM(seq, ctx);
        }
        for(OpcodeOperand op : syntax.getOperands()) {
            Operand decodedOp = decodeOperand(op, seq, ctx, modRM);
            if(decodedOp != null) {
                operands.add(decodedOp);
            }
        }
        size = (int) (prefix.prefixBytes.size() + seq.getPosition() - operandPos);

        // now that the size is known, convert RelativeOps to ImmediateOps
        for(int i = 0; i < operands.size(); i++) {
            Operand op = operands.get(i);
            if(op instanceof RelativeOp) {
                operands.set(i, ((RelativeOp) op).toImmediateOp(size));
            }
        }
    }

    private Operand decodeOperand(OpcodeOperand op, ByteSequence seq, X86Context ctx, ModRM modRM) {
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
        case RELATIVE:              return decodeRelative(seq, op);
        case ES_EDI_RDI: {
            // TODO: check
            PointerOp res;
            switch(X86CPU.getAddressSize(ctx)) {
            case A16:   res = new PointerOp(ctx, Register.DI); break;
            case A32:   res = new PointerOp(ctx, Register.EDI); break;
            case A64:   res = new PointerOp(ctx, Register.RDI); break;
            default: throw new UnsupportedOperationException("unsupported address size: " + X86CPU.getAddressSize(ctx));
            }
            res.setSegment(Segment.ES);
            res.setOpType(op.operType);
            res.setUsage(op.usageType);
            return res;
        }
        case DS_ESI_RSI: {
            // TODO: check
            PointerOp res;
            switch(X86CPU.getAddressSize(ctx)) {
            case A16:   res = new PointerOp(ctx, Register.SI); break;
            case A32:   res = new PointerOp(ctx, Register.ESI); break;
            case A64:   res = new PointerOp(ctx, Register.RSI); break;
            default: throw new UnsupportedOperationException("unsupported address size: " + X86CPU.getAddressSize(ctx));
            }
            res.setSegment(Segment.DS);
            res.setOpType(op.operType);
            res.setUsage(op.usageType);
            return res;
        }
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
        case FLAGS:
        case STACK:
        case TEST:
        default:
            throw new UnsupportedOperationException("unsupported address type: " + op.adrType);
        }
    }

    private Operand decodeRelative(ByteSequence seq, OpcodeOperand op) {
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

        return new RelativeOp(op.usageType, memAddr, relOffset);
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
            if(prefix.rexWPrefix) {
                offset = seq.readSQword();
            } else {
                if(prefix.opSizePrefix) {
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
        short regId = (short) (prefix.prefixBytes.get(idx) & 0x7);
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

    // whether this instruction stops an execution trace
    @Override
    public boolean stopsTrace() {
        switch(syntax.getMnemonic()) {
        case JMP:
        case JMPE:
        case JMPF:
        case RETN:
        case RETF:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String asString(OutputFormatter options) {
        StringBuilder res = new StringBuilder();
        if(options.shouldIncludePrefixBytes()) {
            for(Short b : prefix.prefixBytes) {
                res.append(String.format("%02X", b));
            }
            res.append("\t");
        }
        res.append(prefix.toString());
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

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(syntax.getMnemonic().toString().toLowerCase() + ":\t");
        for(Short b : prefix.prefixBytes) {
            res.append(String.format("%02X", b));
        }
        return res.toString();
    }

    @Override
    public long getMemAddress() {
        return memAddr;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public List<Operand> getOperands() {
        return Collections.unmodifiableList(operands);
    }

    @Override
    public Long getBranchAddress() {
        if(syntax.getOpcodeEntry().belongsTo(OpcodeGroup.GENERAL_BRANCH)) {
            for(Operand op : operands) {
                if(op instanceof ImmediateOp) {
                    return ((ImmediateOp) op).getImmediate();
                } else if(op instanceof PointerOp) {
                    // TODO: think about this. Probably not return because it's [addr] and therefore data and not inst
                    continue;
                }
            }
        }
        return null;
    }
}

// lives only temporarily, will be converted to ImmediateOp
class RelativeOp implements Operand {
    private final UsageType usage;
    private final long relOffset, baseAddr;

    public RelativeOp(UsageType usage, long baseAddr, long relOffset) {
        this.usage = usage;
        this.baseAddr = baseAddr;
        this.relOffset = relOffset;
    }

    public ImmediateOp toImmediateOp(int instSize) {
        return new ImmediateOp(usage, baseAddr + instSize + relOffset);
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputFormatter options) {
        return null;
    }
}

