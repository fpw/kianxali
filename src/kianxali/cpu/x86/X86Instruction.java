package kianxali.cpu.x86;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kianxali.cpu.x86.X86CPU.X86Register;
import kianxali.cpu.x86.X86CPU.Segment;
import kianxali.cpu.x86.xml.OpcodeEntry;
import kianxali.cpu.x86.xml.OpcodeGroup;
import kianxali.cpu.x86.xml.OperandDesc;
import kianxali.cpu.x86.xml.OpcodeSyntax;
import kianxali.decoder.Data;
import kianxali.decoder.Instruction;
import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.image.ByteSequence;
import kianxali.util.OutputFormatter;

public class X86Instruction implements Instruction {
    private final long memAddr;
    private final List<OpcodeSyntax> syntaxes;
    private OpcodeSyntax syntax;
    private final List<Operand> operands;
    private Short parsedExtension;
    private String prefixString;
    private short[] rawData;
    // the size is not known during while decoding operands, so this will cause a (desired) NullPointerException
    private Integer size;

    public X86Instruction(long memAddr, List<OpcodeSyntax> leaves) {
        this.memAddr = memAddr;
        this.syntaxes = leaves;
        this.operands = new ArrayList<>(5);
    }

    public boolean decode(ByteSequence seq, X86Context ctx) {
        OpcodeSyntax selected = null;

        for(OpcodeSyntax syn : syntaxes) {
            this.syntax = syn;
            if(tryDecode(seq, ctx)) {
                selected = syn;
                // FIXME: find some other way..
                if(selected.getOpcodeEntry().opcode == 0x90) {
                    continue;
                } else {
                    break;
                }
            }
        }
        this.syntax = selected;
        return this.syntax != null;
    }

    // the prefix has been read from seq already
    public boolean tryDecode(ByteSequence seq, X86Context ctx) {
        Prefix prefix = ctx.getPrefix();
        prefixString = prefix.toString();
        if(syntax.isExtended()) {
            if(parsedExtension == null) {
                if(syntax.getOpcodeEntry().secondOpcode != null) {
                    // TODO: verify that this is always correct
                    parsedExtension = (short) ((syntax.getOpcodeEntry().secondOpcode >> 3) & 0x07);
                } else {
                    parsedExtension = (short) ((seq.readUByte() >> 3) & 0x07);
                    seq.skip(-1);
                }
            }
            if(syntax.getExtension() != parsedExtension) {
                return false;
            }
        }

        operands.clear();
        ModRM modRM = null;
        long operandPos = seq.getPosition();

        OpcodeEntry entry = syntax.getOpcodeEntry();
        if(entry.modRM) {
            modRM = new ModRM(seq, ctx);
            if((syntax.isModRMMustMem() && !modRM.isRMMem()) || (syntax.isModRMMustReg() && !modRM.isRMReg())) {
                seq.skip(-1);
                return false;
            }
        }

        for(OperandDesc op : syntax.getOperands()) {
            if(op.indirect) {
                continue;
            }
            Operand decodedOp = decodeOperand(op, seq, ctx, modRM);
            if(decodedOp != null) {
                operands.add(decodedOp);
            } else {
                // failure to decode one operand -> failure to decode instruction
                seq.seek(operandPos);
                return false;
            }
        }
        size = (int) (prefix.prefixBytes.size() + seq.getPosition() - operandPos);

        // now that the size is known, convert RelativeOps to ImmediateOps etc.
        for(int i = 0; i < operands.size(); i++) {
            Operand op = operands.get(i);
            if(op instanceof RelativeOp) {
                operands.set(i, ((RelativeOp) op).toImmediateOp(size));
            } else if(op instanceof PointerOp) {
                PointerOp res = (PointerOp) op;
                if(res.needsSizeFix()) {
                    res.setOffset(res.getOffset() + size);
                }
            }
        }

        // finally, retrieve the raw bytes
        rawData = new short[size];
        seq.skip(-size);
        for(int i = 0; i < size; i++) {
            rawData[i] = seq.readUByte();
        }

        return true;
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

    private Operand decodeOperand(OperandDesc op, ByteSequence seq, X86Context ctx, ModRM modRM) {
        switch(op.adrType) {
        case GROUP:                 return decodeGroup(op, ctx);
        case OFFSET:                return decodeOffset(seq, op, ctx);
        case LEAST_REG:             return decodeLeastReg(op, ctx);

        case MOD_RM_R_FORCE_GEN:
        case MOD_RM_R_CTRL:
        case MOD_RM_R_DEBUG:
        case MOD_RM_R_TEST:
        case MOD_RM_R_MMX:
        case MOD_RM_R_SEG:
        case MOD_RM_R_XMM:
        case MOD_RM_R:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getReg(op);
        case MOD_RM_MUST_M:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getMem(op, false, false);
        case MOD_RM_M_FPU_REG:
        case MOD_RM_M_XMM_REG:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getMem(op, true, true);
        case MOD_RM_M_FORCE_GEN:
        case MOD_RM_M_FPU:
        case MOD_RM_M_MMX:
        case MOD_RM_M:
            if(modRM == null) {
                modRM = new ModRM(seq, ctx);
            }
            return modRM.getMem(op, true, false);
        case DIRECT:
        case IMMEDIATE:             return decodeImmediate(seq, op, ctx);
        case RELATIVE:              return decodeRelative(seq, op);
        case ES_EDI_RDI: {
            // TODO: check
            PointerOp res;
            switch(X86CPU.getAddressSize(ctx)) {
            case A16:   res = new PointerOp(ctx, X86Register.DI); break;
            case A32:   res = new PointerOp(ctx, X86Register.EDI); break;
            case A64:   res = new PointerOp(ctx, X86Register.RDI); break;
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
            case A16:   res = new PointerOp(ctx, X86Register.SI); break;
            case A32:   res = new PointerOp(ctx, X86Register.ESI); break;
            case A64:   res = new PointerOp(ctx, X86Register.RSI); break;
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
            return modRM.getMem(op, true, false);
        case SEGMENT2:
        case SEGMENT33:
        case SEGMENT30:
            X86Register reg = X86CPU.getOperandRegister(op, ctx, syntax.getOpcodeEntry().opcode);
            return new RegisterOp(op.usageType, reg);
        case DS_EBX_AL_RBX: {
            PointerOp res;
            switch(X86CPU.getAddressSize(ctx)) {
            case A16: res = new PointerOp(ctx, X86Register.BX, 1, X86Register.AL); break;
            case A32: res = new PointerOp(ctx, X86Register.EBX, 1, X86Register.AL); break;
            case A64: res = new PointerOp(ctx, X86Register.RBX, 1, X86Register.AL); break;
            default: throw new UnsupportedOperationException("invalid address size: " + X86CPU.getAddressSize(ctx));
            }
            res.setOpType(op.operType);
            res.setUsage(op.usageType);
            res.setSegment(Segment.DS);
            return res;
        }
        case DS_EAX_RAX:
        case DS_EDI_RDI:
        case FLAGS:
        case STACK:
        default:
            throw new UnsupportedOperationException("unsupported address type: " + op.adrType);
        }
    }

    private Operand decodeRelative(ByteSequence seq, OperandDesc op) {
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

    private Operand decodeImmediate(ByteSequence seq, OperandDesc op, X86Context ctx) {
        long immediate;
        if(op.hardcoded != null) {
            immediate = Long.parseLong(op.hardcoded, 16);
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
                switch(X86CPU.getOperandSize(ctx, op.operType)) {
                case O16: immediate = seq.readUWord(); break;
                case O32: immediate = seq.readUDword(); break;
                case O64: immediate = seq.readSQword(); break;
                default: throw new UnsupportedOperationException("invalid size: " + X86CPU.getOperandSize(ctx, op.operType));
                }
                break;
            case WORD_DWORD_S64:
                switch(X86CPU.getOperandSize(ctx, op.operType)) {
                case O16: immediate = seq.readSWord(); break;
                case O32: immediate = seq.readSDword(); break;
                default: throw new UnsupportedOperationException("invalid size: " + X86CPU.getOperandSize(ctx, op.operType));
                }
                break;
            case POINTER: {
                long seg, off;
                switch(X86CPU.getOperandSize(ctx, op.operType)) {
                case O16:  off = seq.readUWord(); break;
                case O32:  off = seq.readUDword(); break;
                default: throw new UnsupportedOperationException("unsupported pointer type: " + X86CPU.getOperandSize(ctx, op.operType));
                }
                seg = seq.readUWord();
                return new ImmediateOp(op.usageType, seg, off);
            }
            default:
                throw new UnsupportedOperationException("unsupported immediate type: " + op.operType);
            }
        }
        return new ImmediateOp(op.usageType, immediate);
    }

    private Operand decodeOffset(ByteSequence seq, OperandDesc op, X86Context ctx) {
        long offset;
        switch(X86CPU.getAddressSize(ctx)) {
        case A16: offset = seq.readUWord(); break;
        case A32: offset = seq.readUDword(); break;
        case A64: offset = seq.readSQword(); break;
        default: throw new UnsupportedOperationException("invalid address size: " + X86CPU.getAddressSize(ctx));
        }
        PointerOp res = new PointerOp(ctx, offset);
        res.setOpType(op.operType);
        return res;
    }

    private Operand decodeLeastReg(OperandDesc op, X86Context ctx) {
        int regIndex = ctx.getPrefix().prefixBytes.size() - 1 - syntax.getEncodedRegisterRelativeIndex();
        short regId = (short) (ctx.getPrefix().prefixBytes.get(regIndex) & 0x7);
        X86Register reg = X86CPU.getOperandRegister(op, ctx, regId);
        return new RegisterOp(op.usageType, reg);
    }

    private Operand decodeGroup(OperandDesc op, X86Context ctx) {
        X86Register reg = X86CPU.getOperandRegister(op, ctx, (short) op.numForGroup);
        return new RegisterOp(op.usageType, reg);
    }

    // whether this instruction stops an execution trace
    @Override
    public boolean stopsTrace() {
        X86Mnemonic mnemonic = syntax.getMnemonic();
        if(mnemonic == null) {
            throw new UnsupportedOperationException(String.format("No mnemonic for %s at %08X", syntax, memAddr));
        }
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
        if(options.shouldIncludeRawBytes()) {
            res.append(OutputFormatter.formatByteString(rawData));
            res.append("\t");
        }
        res.append(prefixString);
        if(syntax.getMnemonic() == null) {
            res.append("NO_MNEM");
        } else {
            res.append(syntax.getMnemonic().toString().toLowerCase());
        }
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
    public List<Operand> getDestOperands() {
        List<Operand> res = new ArrayList<>();
        for(Operand op : operands) {
            if(op.getUsage() == UsageType.DEST) {
                res.add(op);
            }
        }
        return res;
    }

    @Override
    public List<Operand> getSrcOperands() {
        List<Operand> res = new ArrayList<>();
        for(Operand op : operands) {
            if(op.getUsage() == UsageType.SOURCE) {
                res.add(op);
            }
        }
        return res;
    }

    @Override
    public List<Long> getBranchAddresses() {
        List<Long> res = new ArrayList<>(3);

        // we only want call, jmp, jnz etc.
        if(!syntax.getOpcodeEntry().belongsTo(OpcodeGroup.GENERAL_BRANCH)) {
            return res;
        }

        X86Mnemonic mnem = syntax.getMnemonic();
        if(mnem == X86Mnemonic.RETF || mnem == X86Mnemonic.RETN) {
            // these are considered branches, but we can't know their address
            return res;
        }

        for(Operand op : operands) {
            if(op instanceof ImmediateOp) {
                res.add(((ImmediateOp) op).getImmediate());
            } else if(op instanceof PointerOp) {
                // TODO: think about this. Probably not return because it's [addr] and therefore data and not inst
                continue;
            }
        }
        return res;
    }

    @Override
    public List<Data> getAssociatedData() {
        List<Data> res = new ArrayList<Data>(3);
        for(Operand op : operands) {
            if(op instanceof PointerOp) {
                Data data = ((PointerOp) op).getProbableData();
                if(data != null) {
                    res.add(data);
                }
            }
        }
        return res;
    }

    @Override
    public List<Long> getProbableDataPointers() {
        List<Long> res = new ArrayList<Long>(3);
        for(Operand op : operands) {
            if(op instanceof ImmediateOp) {
                res.add(((ImmediateOp) op).getImmediate());
            }
        }
        return res;
    }

    @Override
    public String getMnemonic() {
        return syntax.getMnemonic().toString();
    }

    @Override
    public short[] getRawBytes() {
        return rawData;
    }

    @Override
    public boolean isFunctionCall() {
        X86Mnemonic mnem = syntax.getMnemonic();
        if(mnem == X86Mnemonic.CALL || mnem == X86Mnemonic.CALLF) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isJump() {
        X86Mnemonic mnem = syntax.getMnemonic();
        if(mnem == X86Mnemonic.JMP || mnem == X86Mnemonic.JMPE || mnem == X86Mnemonic.JMPF) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return syntax.getOpcodeEntry().briefDescription;
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

