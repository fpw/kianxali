package org.solhost.folko.dasm.decoder;

import java.util.List;

import org.solhost.folko.dasm.OutputFormatter;

public interface Instruction {
    public long getMemAddress();
    public int getSize();
    public boolean stopsTrace();
    public String asString(OutputFormatter format);
    public List<Operand> getOperands();
    public Long getBranchAddress();
}
