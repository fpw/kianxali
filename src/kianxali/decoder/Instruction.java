package kianxali.decoder;

import java.util.List;

import kianxali.util.OutputFormatter;

public interface Instruction extends DecodedEntity {
    boolean stopsTrace();
    String getMnemonicString(OutputFormatter formatter);
    List<Operand> getOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
}
