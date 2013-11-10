package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public interface Operand {
    public String asString(Object options);
    public UsageType getUsage();
}
