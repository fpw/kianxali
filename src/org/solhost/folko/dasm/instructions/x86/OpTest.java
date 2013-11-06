package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpTest extends Instruction {
    private Operand op1, op2;

    public OpTest(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x84: {
            ctx.setOpSizeOverride(OperandSize.O8);
            ModRM modRM = new ModRM(seq, ctx);
            op1 = modRM.getMemOp();
            op2 = modRM.getRegOp();
        } break;
        case 0x85: {
            ModRM modRM = new ModRM(seq, ctx);
            op1 = modRM.getMemOp();
            op2 = modRM.getRegOp();
        } break;
        default:
            throw new UnsupportedOperationException("Unknown test opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "test";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {op1, op2};
    }
}
