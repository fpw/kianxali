package kianxali.decoder.arch.x86;

import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.util.OutputFormatter;

/**
 * This class is used to represent operands that contain
 * an immediate value, e.g. the 123 in mov eax, 123.
 * Since it is not always possible to distinguish whether
 * an immediate is an address or just a number, this class
 * is used for both types.
 * @author fwi
 *
 */
public class ImmediateOp implements Operand {
    private final UsageType usage;
    private final long immediate;
    private final Long segment;

    ImmediateOp(UsageType usage, long immediate) {
        this.usage = usage;
        this.immediate = immediate;
        this.segment = null;
    }

    ImmediateOp(UsageType usage, long seg, long off) {
        this.usage = usage;
        this.immediate = off;
        this.segment = seg;
    }

    /**
     * Returns the actual immediate stored in the operand
     * @return the decoded immediate value
     */
    public long getImmediate() {
        return immediate;
    }

    /**
     * If the immediate also contains a segment, it will be
     * returned here.
     * @return the segment belonging to the immediate, can be null
     */
    public Long getSegment() {
        return segment;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputFormatter options) {
        if(segment != null) {
            return options.formatImmediate(segment) + ":" + options.formatImmediate(immediate);
        } else {
            return options.formatImmediate(immediate);
        }
    }

    @Override
    public Number asNumber() {
        return immediate;
    }

    @Override
    public Short getPointerDestSize() {
        return null;
    }
}
