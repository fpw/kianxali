package org.solhost.folko.dasm;

public interface Section {
    public String getName();
    public long getStartAddress();
    public long getEndAddress();
}
