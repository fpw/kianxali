package org.solhost.folko.dasm.cpu.x86;

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
    public String asString(Object options) {
        if(immediate < 0) {
            return String.format("-%Xh", -immediate);
        } else if(immediate > 0) {
            return String.format("%Xh", immediate);
        } else {
            return "0";
        }
    }
}
