package kianxali.cpu.x86;

import kianxali.cpu.x86.X86CPU.X86Register;
import kianxali.cpu.x86.X86CPU.Segment;
import kianxali.cpu.x86.xml.OperandDesc.OperandType;
import kianxali.decoder.Data;
import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.decoder.Data.DataType;
import kianxali.util.OutputFormatter;

public class PointerOp implements Operand {
    // TODO: context should be removed from here
    private final X86Context context;
    private UsageType usage;
    private OperandType opType;
    private Segment segment;
    private X86Register baseRegister, indexRegister;
    private Integer indexScale;
    private Long offset;
    private boolean needSizeFix;

    // ptr [address]
    public PointerOp(X86Context ctx, long offset) {
        this.context = ctx;
        this.offset = offset;
    }

    // ptr [register]
    public PointerOp(X86Context ctx, X86Register baseRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
    }

    public PointerOp(X86Context ctx, X86Register baseRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        this.offset = offset;
    }

    // ptr [scale * register]
    public PointerOp(X86Context ctx, X86Register indexRegister, int scale) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
    }

    // ptr [scale * register + offset]
    public PointerOp(X86Context ctx, X86Register indexRegister, int scale, long offset) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.offset = offset;
    }

    // ptr [base + scale * index]
    public PointerOp(X86Context ctx, X86Register baseRegister, int scale, X86Register indexRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
    }

    // ptr [scale * index + offset]
    public PointerOp(X86Context ctx, int scale, X86Register indexRegister, long offset) {
        this.context = ctx;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    // ptr [base + scale * index + offset]
    public PointerOp(X86Context ctx, X86Register baseRegister, int scale, X86Register indexRegister, long offset) {
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
    public Number asNumber() {
        if(offset != null && baseRegister == null && indexRegister == null) {
            // only this combination is deterministic
            return offset;
        }
        return null;
    }

    public Data getProbableData() {
        DataType type;
        if(offset == null) {
            // only register-based indexing, can't know address
            return null;
        }

        if(baseRegister == null && indexRegister == null) {
            // only addressed by constant -> great because we know the size then
            // TODO: work on opType directly for more information
            switch(X86CPU.getOperandSize(context, opType)) {
            case O8:    type = DataType.BYTE; break;
            case O16:   type = DataType.WORD; break;
            case O32:   type = DataType.DWORD; break;
            case O64:   type = DataType.QWORD; break;
            case O128:  type = DataType.DQWORD; break;
            case O512:  type = DataType.DYWORD; break;
            default:    type = DataType.UNKNOWN;
            }
        } else {
            type = DataType.UNKNOWN;
        }
        return new Data(offset, type);
    }

    @Override
    public String asString(OutputFormatter formatter) {
        StringBuilder str = new StringBuilder();

        switch(opType) {
        case SINGLE_128:
        case DOUBLE_128:
            str.append("xmmword ptr ");
            break;
        default:
            switch(X86CPU.getOperandSize(context, opType)) {
            case O8:    str.append("byte ptr "); break;
            case O16:   str.append("word ptr "); break;
            case O32:   str.append("dword ptr "); break;
            case O64:   str.append("qword ptr "); break;
            case O80:   str.append("tbyte ptr "); break;
            case O128:  str.append("dqword ptr "); break;
            case O512:  str.append("dyword ptr "); break;
            default: throw new RuntimeException("invalid operand size: " + opType);
            }
        }

        if(segment != null) {
            str.append(segment + ":");
        } else if(context.getPrefix().overrideSegment != null) {
            str.append(context.getPrefix().overrideSegment + ":");
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

    public void setNeedSizeFix(boolean b) {
        needSizeFix = b;
    }

    public boolean needsSizeFix() {
        return needSizeFix;
    }
}