package kianxali.decoder.arch.x86;

import java.util.logging.Logger;

import kianxali.decoder.Data;
import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.decoder.Data.DataType;
import kianxali.decoder.arch.x86.X86CPU.OperandSize;
import kianxali.decoder.arch.x86.X86CPU.Segment;
import kianxali.decoder.arch.x86.X86CPU.X86Register;
import kianxali.decoder.arch.x86.xml.OperandDesc.OperandType;
import kianxali.util.OutputFormatter;

/**
 * This class is used to represent operands that contain
 * a dereferenced memory address, e.g. an operand of the
 * type [baseRegister +  indexScale * indexRegister + offset]
 * @author fwi
 *
 */
public class PointerOp implements Operand {
    // TODO: context should be removed from here
    private static final Logger LOG = Logger.getLogger("kianxali.decoder.arch.x86");
    private final X86Context context;
    private UsageType usage;
    private OperandType opType;
    private Segment segment;
    private X86Register baseRegister, indexRegister;
    private Integer indexScale;
    private Long offset;
    private boolean needSizeFix;

    // ptr [address]
    PointerOp(X86Context ctx, long offset) {
        this.context = ctx;
        this.offset = offset;
    }

    // ptr [register]
    PointerOp(X86Context ctx, X86Register baseRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
    }

    PointerOp(X86Context ctx, X86Register baseRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        this.offset = offset;
    }

    // ptr [scale * register]
    PointerOp(X86Context ctx, X86Register indexRegister, int scale) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
    }

    // ptr [scale * register + offset]
    PointerOp(X86Context ctx, X86Register indexRegister, int scale, long offset) {
        this.context = ctx;
        this.indexRegister = indexRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.offset = offset;
    }

    // ptr [base + scale * index]
    PointerOp(X86Context ctx, X86Register baseRegister, int scale, X86Register indexRegister) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
    }

    // ptr [scale * index + offset]
    PointerOp(X86Context ctx, int scale, X86Register indexRegister, long offset) {
        this.context = ctx;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    // ptr [base + scale * index + offset]
    PointerOp(X86Context ctx, X86Register baseRegister, int scale, X86Register indexRegister, long offset) {
        this.context = ctx;
        this.baseRegister = baseRegister;
        if(scale > 1 && indexRegister != null) {
            this.indexScale = scale;
        }
        this.indexRegister = indexRegister;
        this.offset = offset;
    }

    /**
     * Returns whether the offset part of the address is present
     * @return true if the address contains an offset part
     */
    public boolean hasOffset() {
        return offset != null;
    }

    void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * Returns the offset part of the address
     * @return the offset, can be null
     */
    public Long getOffset() {
        return offset;
    }

    void setOpType(OperandType opType) {
        this.opType = opType;
    }

    void setUsage(UsageType usage) {
        this.usage = usage;
    }

    void setSegment(Segment segment) {
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

    /**
     * If possible, returns a {@link Data} object with address and type
     * set to match this operand.
     * @return
     */
    public Data getProbableData() {
        if(offset == null) {
            // only register-based indexing, can't know address
            return null;
        }

        if(baseRegister == null && indexRegister == null) {
            DataType type;
            // only addressed by constant -> great because we know the size then
            // TODO: work on opType directly for more information
            try {
                OperandSize size = X86CPU.getOperandSize(context, opType);

                switch(size) {
                case O8:    type = DataType.BYTE; break;
                case O16:   type = DataType.WORD; break;
                case O32:   type = DataType.DWORD; break;
                case O64:   type = DataType.QWORD; break;
                case O128:  type = DataType.DQWORD; break;
                case O512:  type = DataType.DYWORD; break;
                default:    type = DataType.UNKNOWN;
                }
            } catch(Exception e) {
                LOG.warning("Unknown operand size for " + opType);
                type = DataType.UNKNOWN;
            }
            return new Data(offset, type);
        } else {
            Data res = new Data(offset, DataType.UNKNOWN);
            if(indexScale != null) {
                res.setTableScaling(indexScale);
            }
            return res;
        }
    }

    void setNeedSizeFix(boolean b) {
        needSizeFix = b;
    }

    boolean needsSizeFix() {
        return needSizeFix;
    }

    @Override
    public Short getPointerDestSize() {
        switch(X86CPU.getOperandSize(context, opType)) {
        case O8:    return 8;
        case O16:   return 16;
        case O32:   return 32;
        case O64:   return 64;
        case O80:   return 80;
        case O128:  return 128;
        case O512:  return 512;
        default: throw new RuntimeException("invalid operand size: " + opType);
        }
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
            try {
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
            } catch(Exception e) {
                LOG.warning("Unknown operand size for " + opType);
                str.append("? ptr ");
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
}