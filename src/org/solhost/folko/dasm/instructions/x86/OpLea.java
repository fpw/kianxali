package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpLea extends Instruction {
    private Operand dstOp, srcOp;

    public OpLea(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x8D:
            ModRM modRM = new ModRM(seq, ctx);
            dstOp = modRM.getRegOp();
            srcOp = modRM.getMemOp();
            break;
        default:
            throw new UnsupportedOperationException("invalid lea opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "lea";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }

}
