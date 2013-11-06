package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpGroup80 extends Instruction {
    private final short extID;
    private final Operand dstOp, srcOp;

    public OpGroup80(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        if(opcode < 0x80 || opcode > 0x83) {
            throw new RuntimeException("invalid op80 group: " + opcode);
        }
        if(opcode == 0x80 || opcode == 0x82) {
            ctx.setOpSizeOverride(OperandSize.O8);
        }

        extID = getOpcodeExtension(seq);
        ModRM modRM = new ModRM(seq, ctx);
        dstOp = modRM.getMemOp();
        if(opcode == 0x80 || opcode == 0x82 || opcode == 0x83) {
            srcOp = new ImmediateOp(seq.readSByte());
        } else {
            srcOp = new ImmediateOp(seq.readSDword());
        }
    }

    @Override
    public String getMnemonic() {
        switch(extID) {
        case 0: return "add";
        case 1: return "or";
        case 2: return "adc";
        case 3: return "sbb";
        case 4: return "and";
        case 5: return "sub";
        case 6: return "xor";
        case 7: return "cmp";
        default:
            throw new RuntimeException("invalid op83 extension: " + extID);
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }
}
