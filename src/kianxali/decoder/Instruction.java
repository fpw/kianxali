package kianxali.decoder;

import java.util.List;

import kianxali.util.OutputFormatter;

public interface Instruction extends DecodedEntity {
    boolean stopsTrace();
    boolean isFunctionCall();
    String getMnemonicString(OutputFormatter formatter);
    List<Operand> getOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
    short[] getRawBytes();
}
