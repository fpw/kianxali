package org.solhost.folko.dasm.decoder;

import org.solhost.folko.dasm.OutputFormatter;

public interface Instruction {
    public long getMemAddress();
    public int getSize();
    public boolean stopsTrace();
    public String asString(OutputFormatter format);
}
