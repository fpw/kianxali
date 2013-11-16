package kianxali.decoder;

import kianxali.util.OutputFormatter;

public interface DecodedEntity {
    long getMemAddress();
    int getSize();
    String asString(OutputFormatter format);
}
