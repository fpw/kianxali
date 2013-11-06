package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpRet extends Instruction {

    public OpRet(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        if(opcode != 0xC3) {
            throw new UnsupportedOperationException("invalid ret opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "ret";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] { };
    }

}
