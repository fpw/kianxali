package org.solhost.folko.dasm.images;

public interface Section {
    String getName();
    long getStartAddress();
    long getEndAddress();
}
