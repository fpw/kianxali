package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;

public class OpFF extends Instruction {
    private final short extID;
    private Operand operand;

    public OpFF(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        if(opcode != 0xFF) {
            throw new RuntimeException("invalid 0xFF opcode: " + opcode);
        }

        extID = getOpcodeExtension(seq);
        switch(extID) {
        case 0:
        case 1:
        case 2:
        case 4:
        case 6:
            ModRM modRM = new ModRM(seq, ctx);
            operand = modRM.getMemOp();
            break;
        case 3:
        case 5:
            switch(ctx.getAddressSize()) {
            case A16: operand = new ImmediateOp(seq.readUWord()); break;
            case A32: operand = new ImmediateOp(seq.readUDword()); break;
            default: throw new RuntimeException("invalid address size: " + ctx.getAddressSize());
            }
            break;
        default:
            throw new RuntimeException("invalid FF extension: " + extID);
        }
    }

    @Override
    public String getMnemonic() {
        switch(extID) {
        case 0: return "inc";
        case 1: return "dec";
        case 2: return "call";
        case 3: return "callf";
        case 4: return "jmp";
        case 5: return "jmpf";
        case 6: return "push";
        default: return "<invalid FF extension: " + extID + ">";
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {operand};
    }

}
