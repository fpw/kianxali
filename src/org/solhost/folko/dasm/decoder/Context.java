package org.solhost.folko.dasm.decoder;

public interface Context {
    public InstructionDecoder createInstructionDecoder();
    public void setInstructionPointer(long pointer);
    public long getInstructionPointer();
}
