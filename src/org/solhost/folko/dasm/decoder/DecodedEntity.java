package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;

public interface DecodedEntity {
    long getMemAddress();
    int getSize();
    String asString(OutputFormatter format);
}
