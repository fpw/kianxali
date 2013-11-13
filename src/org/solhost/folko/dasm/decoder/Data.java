package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;

public class Data implements DecodedEntity {
    @Override
    public long getMemAddress() {
        return 0;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String asString(OutputFormatter format) {
        return null;
    }
}
