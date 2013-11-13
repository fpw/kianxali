package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public interface Operand {
    public String asString(OutputFormatter options);
    public UsageType getUsage();
}
