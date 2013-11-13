package org.solhost.folko.dasm.decoder;

import java.util.List;

public interface Instruction extends DecodedEntity {
    boolean stopsTrace();
    List<Operand> getOperands();
    Long getBranchAddress();
}
