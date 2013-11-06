package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpInc extends Instruction {
    private Operand dstOp;

    public OpInc(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x40:
        case 0x41:
        case 0x42:
        case 0x43:
        case 0x44:
        case 0x45:
        case 0x46:
        case 0x47:
            dstOp = new RegisterOp(Register.fromByte((short) (opcode - 0x40), ctx, true));
            break;
        default:
            throw new UnsupportedOperationException("invalid inc opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "inc";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp};
    }

}
