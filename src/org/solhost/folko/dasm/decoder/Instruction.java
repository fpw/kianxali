package org.solhost.folko.dasm.decoder;

import java.util.List;

public interface Instruction extends DecodedEntity {
    int getSize();
    boolean stopsTrace();
    List<Operand> getOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
}
