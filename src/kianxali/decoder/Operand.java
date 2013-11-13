package kianxali.decoder;

import kianxali.util.OutputFormatter;

public interface Operand {
    String asString(OutputFormatter options);
    UsageType getUsage();
}
