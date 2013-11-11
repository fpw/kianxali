package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputOptions;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public interface Operand {
    public String asString(OutputOptions options);
    public UsageType getUsage();
}
