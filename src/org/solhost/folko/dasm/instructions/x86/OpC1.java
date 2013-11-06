package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpC1 extends Instruction {
    private final short extID;
    private final Operand dstOp, srcOp;

    public OpC1(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        if(opcode != 0xC1) {
            throw new RuntimeException("invalid 0xC1 opcode: " + opcode);
        }
        extID = getOpcodeExtension(seq);
        ModRM modRM = new ModRM(seq, ctx);
        dstOp = modRM.getMemOp();
        srcOp = new ImmediateOp(seq.readUByte());
    }

    @Override
    public String getMnemonic() {
        switch(extID) {
        case 0: return "rol";
        case 1: return "ror";
        case 2: return "rcl";
        case 3: return "rcr";
        case 4: return "shl";
        case 5: return "shr";
        case 6: return "shl";
        case 7: return "sar";
        default: throw new RuntimeException("invalid 0xC1 extension: " + extID);
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }
}
