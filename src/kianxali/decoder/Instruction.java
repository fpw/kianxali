package kianxali.decoder;

import java.util.List;

import kianxali.util.OutputFormatter;

public interface Instruction extends DecodedEntity {
    boolean stopsTrace();
    boolean isFunctionCall();
    boolean isJump();
    String getMnemonicString(OutputFormatter formatter);
    List<Operand> getOperands();
    List<Operand> getDestOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
    List<Long> getProbableDataPointers();
    short[] getRawBytes();
    String getDescription();
}
