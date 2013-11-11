package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormat;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public interface Operand {
    public String asString(OutputFormat options);
    public UsageType getUsage();
}
