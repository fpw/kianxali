package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpPop extends Instruction {
    private Operand dst;

    public OpPop(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x58:
        case 0x59:
        case 0x5A:
        case 0x5B:
        case 0x5C:
        case 0x5D:
        case 0x5E:
        case 0x5F:
            dst = new RegisterOp(Register.fromByte((short) (opcode - 0x58), ctx, false));
        }
    }

    @Override
    public String getMnemonic() {
        return "pop";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dst};
    }

}
