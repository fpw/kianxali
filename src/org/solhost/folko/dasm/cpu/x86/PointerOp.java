package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.OutputFormat;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public class PointerOp implements Operand {
    private UsageType usage;
    private OperandType opType;
    private Segment segment;
    private final Context context;
    private Register baseRegister, indexRegister;
    private Integer indexScale;
    private Long offset;

    // ptr [address]
    public PointerOp(Context ctx, long offset) {
        this.context = ctx;
        this.offset = offset;
    }

    // ptr [register]
    public PointerOp(Context ctx, Register baseRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
    }

    public PointerOp(Context ctx, Register baseRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        this.offset = offset;
    }

    // ptr [scale * register]
    public PointerOp(Context ctx, Register indexRegister, int scale) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
    }

    // ptr [scale * register + offset]
    public PointerOp(Context ctx, Register indexRegister, int scale, long offset) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.offset = offset;
    }

    // ptr [base + scale * index]
    public PointerOp(Context ctx, Register baseRegister, int scale, Register indexRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
    }

    // ptr [scale * index + offset]
    public PointerOp(Context ctx, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    // ptr [base + scale * index + offset]
    public PointerOp(Context ctx, Register baseRegister, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    public void addOffset(long add) {
        if(offset == null) {
            offset = add;
        } else {
            offset += add;
        }
    }

    public void setOpType(OperandType opType) {
        this.opType = opType;
    }

    public void setUsage(UsageType usage) {
        this.usage = usage;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    @Override
    public String asString(OutputFormat options) {
        StringBuilder str = new StringBuilder();

        switch(opType) {
        case BYTE: str.append("byte ptr "); break;
        case WORD: str.append("word ptr "); break;
        case WORD_DWORD:
        case WORD_DWORD_64: str.append("dword ptr "); break;
        default: throw new RuntimeException("invalid operand size: " + opType);
        }

        if(segment != null) {
            str.append(segment + ":");
        } else if(context.getOverrideSegment() != null) {
            str.append(context.getOverrideSegment() + ":");
        }

        str.append("[");
        boolean needsPlus = false;
        if(baseRegister != null) {
            str.append(baseRegister);
            needsPlus = true;
        }
        if(indexScale != null) {
            if(needsPlus) {
                str.append(" + ");
            }
            str.append(indexScale + " * ");
            needsPlus = false;
        }
        if(indexRegister != null) {
            if(needsPlus) {
                str.append(" + ");
            }
            str.append(indexRegister);
            needsPlus = true;
        }
        if(offset != null) {
            if(needsPlus) {
                str.append(offset < 0 ? " - " : " + ");
            }
            str.append(options.formatAddress(offset));
        }
        str.append("]");

        return str.toString();
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }
}