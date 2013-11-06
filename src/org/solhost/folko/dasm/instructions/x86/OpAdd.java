package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpAdd extends Instruction {
    private Operand dstOp, srcOp;

    public OpAdd(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x03:
            ModRM modRM = new ModRM(seq, ctx);
            dstOp = modRM.getRegOp();
            srcOp = modRM.getMemOp();
            break;
        default:
            throw new UnsupportedOperationException("invalid add opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "add";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }
}
