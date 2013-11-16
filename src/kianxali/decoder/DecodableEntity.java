package kianxali.decoder;

import kianxali.util.OutputFormatter;

public interface DecodableEntity {
    long getMemAddress();
    int getSize();
    String asString(OutputFormatter format);
}
