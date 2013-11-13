package kianxali.cpu.x86;

import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.util.OutputFormatter;

public class ImmediateOp implements Operand {
    private final UsageType usage;
    private final long immediate;

    public ImmediateOp(UsageType usage, long immediate) {
        this.usage = usage;
        this.immediate = immediate;
    }

    public long getImmediate() {
        return immediate;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputFormatter options) {
        return options.formatImmediate(immediate);
    }
}
