package org.solhost.folko.dasm.images;

public interface Section {
    public String getName();
    public long getStartAddress();
    public long getEndAddress();
}
