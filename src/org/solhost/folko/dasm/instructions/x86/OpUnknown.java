package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpUnknown extends Instruction {
    private final Short opcode1, opcode2;

    public OpUnknown(ByteSequence seq, Context ctx) {
        this.opcode1 = seq.readUByte();
        if(opcode1 == 0x0F) {
            this.opcode2 = seq.readUByte();
        } else {
            this.opcode2 = null;
        }
    }

    @Override
    public String getMnemonic() {
        if(opcode2 == null) {
            return String.format("<unknown opcode %02X>", opcode1);
        } else {
            return String.format("<unknown opcode %02X%02X>", opcode1, opcode2);
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[0];
    }
}
