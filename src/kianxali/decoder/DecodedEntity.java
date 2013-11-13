package kianxali.decoder;

import kianxali.util.OutputFormatter;

public interface DecodedEntity {
    long getMemAddress();
    String asString(OutputFormatter format);
}
