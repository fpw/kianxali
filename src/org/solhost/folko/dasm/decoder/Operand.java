package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;

public interface Operand {
    public String asString(OutputFormatter options);
    public UsageType getUsage();
}
