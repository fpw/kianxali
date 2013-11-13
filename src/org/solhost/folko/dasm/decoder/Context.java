package org.solhost.folko.dasm.decoder;

public interface Context {
    Decoder createInstructionDecoder();
    void setInstructionPointer(long pointer);
    long getInstructionPointer();
}
