package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RelativeAddress;

public class OpCall extends Instruction {
    private Operand srcOp;

    public OpCall(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0xE8:
            switch(ctx.getAddressSize()) {
            case A16: srcOp = new RelativeAddress(seq.readSWord()); break;
            case A32: srcOp = new RelativeAddress(seq.readSDword()); break;
            default: throw new RuntimeException("invalid address size: " + ctx.getAddressSize());
            }
            break;
        default:
            throw new RuntimeException("invalid call opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "call";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {srcOp};
    }
}
