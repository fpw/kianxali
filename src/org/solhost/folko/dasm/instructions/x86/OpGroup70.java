package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RelativeAddress;

public class OpGroup70 extends Instruction {
    private Operand dstOp;
    private final short opcode;

    public OpGroup70(ByteSequence seq, Context ctx) {
        opcode = seq.readUByte();

        switch(opcode) {
        case 0x70:
        case 0x71:
        case 0x72:
        case 0x73:
        case 0x74:
        case 0x75:
        case 0x76:
        case 0x77:
        case 0x78:
        case 0x79:
        case 0x7A:
        case 0x7B:
        case 0x7C:
        case 0x7D:
        case 0x7E:
        case 0x7F:
            dstOp = new RelativeAddress(seq.readSByte());
            break;
        default:
            throw new UnsupportedOperationException("invalid group 70 opcode " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        switch(opcode) {
        case 0x70: return "jo";
        case 0x71: return "jno";
        case 0x72: return "jb";
        case 0x73: return "jnb";
        case 0x74: return "jz";
        case 0x75: return "jnz";
        case 0x76: return "jbe";
        case 0x77: return "ja";
        case 0x78: return "js";
        case 0x79: return "jns";
        case 0x7A: return "jp";
        case 0x7B: return "jnp";
        case 0x7C: return "jl";
        case 0x7D: return "jge";
        case 0x7E: return "jle";
        case 0x7F: return "jg";
        default:
            throw new UnsupportedOperationException("invalid group 70 opcode: " + opcode);
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp};
    }
}
