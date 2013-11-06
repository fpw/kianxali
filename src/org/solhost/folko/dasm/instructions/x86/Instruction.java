package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public abstract class Instruction {
    public abstract String getMnemonic();
    public abstract Operand[] getOperands();

    protected short getOpcodeExtension(ByteSequence seq) {
        short extension = seq.readUByte();
        short extID = (short) ((extension >> 3) & 0x07);
        seq.skip(-1);
        return extID;
    }
}
