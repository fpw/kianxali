package org.solhost.folko.dasm.decoder;

import java.util.List;

import org.solhost.folko.dasm.OutputFormatter;

public interface Instruction {
    long getMemAddress();
    int getSize();
    boolean stopsTrace();
    String asString(OutputFormatter format);
    List<Operand> getOperands();
    Long getBranchAddress();
}
