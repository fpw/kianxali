package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpAnd extends Instruction {
    private Operand dstOp, srcOp;

    public OpAnd(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x25:
            switch(ctx.getOperandSize()) {
            case O16:
                dstOp = new RegisterOp(Register.AX);
                srcOp = new ImmediateOp(seq.readUWord());
                break;
            case O32:
                dstOp = new RegisterOp(Register.EAX);
                srcOp = new ImmediateOp(seq.readUDword());
                break;
            default: throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
            }

            break;
        default:
            throw new RuntimeException("Unknown and opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "and";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }
}
