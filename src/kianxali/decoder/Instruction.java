package kianxali.decoder;

import java.util.List;

public interface Instruction extends DecodableEntity {
    boolean stopsTrace();
    List<Operand> getOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
}
