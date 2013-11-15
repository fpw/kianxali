package kianxali.cpu.x86;

import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.util.OutputFormatter;

public class ImmediateOp implements Operand {
    private final UsageType usage;
    private final long immediate;
    private final Long segment;

    public ImmediateOp(UsageType usage, long immediate) {
        this.usage = usage;
        this.immediate = immediate;
        this.segment = null;
    }

    public ImmediateOp(UsageType usage, long seg, long off) {
        this.usage = usage;
        this.immediate = off;
        this.segment = seg;
    }

    public long getImmediate() {
        return immediate;
    }

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
}
