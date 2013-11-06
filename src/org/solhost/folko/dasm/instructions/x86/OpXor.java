package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpXor extends Instruction {
    private final Operand srcOp, dstOp;

    public OpXor(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x33:
            ModRM modRM = new ModRM(seq, ctx);
            dstOp = modRM.getRegOp();
            srcOp = modRM.getMemOp();
            break;
        default:
            throw new RuntimeException("invalid xor opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "xor";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }

}
