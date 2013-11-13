package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;

public interface Operand {
    String asString(OutputFormatter options);
    UsageType getUsage();
}
