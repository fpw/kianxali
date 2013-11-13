package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.decoder.UsageType;
import org.solhost.folko.dasm.xml.OpcodeOperand.OperandType;

public class PointerOp implements Operand {
    private UsageType usage;
    private OperandType opType;
    private Segment segment;
    private final X86Context context;
    private Register baseRegister, indexRegister;
    private Integer indexScale;
    private Long offset;

    // ptr [address]
    public PointerOp(X86Context ctx, long offset) {
        this.context = ctx;
        this.offset = offset;
    }

    // ptr [register]
    public PointerOp(X86Context ctx, Register baseRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
    }

    public PointerOp(X86Context ctx, Register baseRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        this.offset = offset;
    }

    // ptr [scale * register]
    public PointerOp(X86Context ctx, Register indexRegister, int scale) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
    }

    // ptr [scale * register + offset]
    public PointerOp(X86Context ctx, Register indexRegister, int scale, long offset) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.offset = offset;
    }

    // ptr [base + scale * index]
    public PointerOp(X86Context ctx, Register baseRegister, int scale, Register indexRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
    }

    // ptr [scale * index + offset]
    public PointerOp(X86Context ctx, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    // ptr [base + scale * index + offset]
    public PointerOp(X86Context ctx, Register baseRegister, int scale, Register indexRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    public boolean hasOffset() {
        return offset != null;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Long getOffset() {
        return offset;
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
    public String asString(OutputFormatter formatter) {
        StringBuilder str = new StringBuilder();

        switch(X86CPU.getOperandSize(context, opType)) {
        case O8:    str.append("byte ptr "); break;
        case O16:   str.append("word ptr "); break;
        case O32:   str.append("dword ptr "); break;
        case O64:   str.append("qword ptr "); break;
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
            str.append(formatter.formatRegister(baseRegister.toString()));
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
            str.append(formatter.formatRegister(indexRegister.toString()));
            needsPlus = true;
        }
        if(offset != null) {
            if(needsPlus) {
                str.append(offset < 0 ? " - " : " + ");
            }
            str.append(formatter.formatAddress(offset));
        }
        str.append("]");

        return str.toString();
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }
}