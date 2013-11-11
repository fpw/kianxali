package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.OutputFormat;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public class RelativeOp implements Operand {
    private final UsageType usage;
    private final long relOffset, baseAddr;

    public RelativeOp(UsageType usage, long baseAddr, long relOffset) {
        this.usage = usage;
        this.baseAddr = baseAddr;
        this.relOffset = relOffset;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputFormat options) {
        return options.formatAddress(baseAddr + relOffset);
    }
}
