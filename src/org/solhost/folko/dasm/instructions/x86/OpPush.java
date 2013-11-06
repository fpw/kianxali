package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpPush extends Instruction {
    private Operand srcOp;

    public OpPush(ByteSequence seq, Context ctx) {
        short id = seq.readUByte();
        switch(id) {
        case 0x50:
        case 0x51:
        case 0x52:
        case 0x53:
        case 0x54:
        case 0x55:
        case 0x56:
        case 0x57:
            srcOp = new RegisterOp(Register.fromByte((short) (id & ~0x50), ctx, true));
            break;
        case 0x68:
            srcOp = new ImmediateOp(seq.readSDword());
            break;
        case 0x6A:
            srcOp = new ImmediateOp(seq.readSByte());
            break;
        default:
            throw new RuntimeException("invalid push opcode: " + id);
        }
    }

    @Override
    public String getMnemonic() {
        return "push";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {srcOp};
    }
}
