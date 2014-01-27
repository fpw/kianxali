package kianxali.decoder;

import java.util.List;

public interface Instruction extends DecodedEntity {
    boolean stopsTrace();
    boolean isFunctionCall();
    boolean isJump();
    String getMnemonic();
    List<Operand> getOperands();
    List<Operand> getSrcOperands();
    List<Operand> getDestOperands();
    List<Long> getBranchAddresses();
    List<Data> getAssociatedData();
    List<Long> getProbableDataPointers();
    short[] getRawBytes();
    String getDescription();
}
