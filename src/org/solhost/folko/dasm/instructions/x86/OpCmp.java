package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpCmp extends Instruction {
    private Operand op1, op2;

    public OpCmp(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x3C:
            ctx.setOpSizeOverride(OperandSize.O8);
            op1 = new RegisterOp(Register.AL);
            op2 = new ImmediateOp(seq.readSByte());
            break;
        default:
            throw new UnsupportedOperationException("unknown cmp opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "cmp";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {op1, op2};
    }
}
