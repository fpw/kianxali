package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RelativeAddress;

public class OpJmp extends Instruction {
    private Operand dstOp;

    public OpJmp(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0xE9:
            switch(ctx.getAddressSize()) {
            case A16: dstOp = new RelativeAddress(seq.readSWord()); break;
            case A32: dstOp = new RelativeAddress(seq.readSDword()); break;
            default: throw new UnsupportedOperationException("invalid address size: " + ctx.getAddressSize());
            }
            break;
        case 0xEB:
            dstOp = new RelativeAddress(seq.readSByte());
            break;
        default:
            throw new UnsupportedOperationException("invalid jmp opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "jmp";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp};
    }

}
