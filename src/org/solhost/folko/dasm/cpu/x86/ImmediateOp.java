package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public class ImmediateOp implements Operand {
    private final UsageType usage;
    private final long immediate;

    public ImmediateOp(UsageType usage, long immediate) {
        this.usage = usage;
        this.immediate = immediate;
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
