package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpLeave extends Instruction {
    private Operand dstOp;

    public OpLeave(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0xC9:
            switch(ctx.getOperandSize()) {
            case O16: dstOp = new RegisterOp(Register.BP); break;
            case O32: dstOp = new RegisterOp(Register.EBP); break;
            default: throw new UnsupportedOperationException("invalid operand size: " + ctx.getOperandSize());
            }
            break;
        default:
            throw new UnsupportedOperationException("invalid leave opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "leave";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp};
    }

}
