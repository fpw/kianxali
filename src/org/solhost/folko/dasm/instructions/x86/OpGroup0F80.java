package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RelativeAddress;

public class OpGroup0F80 extends Instruction {
    private short opcode2;
    private Operand dstOp;

    public OpGroup0F80(ByteSequence seq, Context ctx) {
        short opcode1 = seq.readUByte();
        switch(opcode1) {
        case 0x0F:
            opcode2 = seq.readUByte();
            switch(opcode2) {
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8A:
            case 0x8B:
            case 0x8C:
            case 0x8D:
            case 0x8E:
            case 0x8F:
                switch(ctx.getAddressSize()) {
                case A16: dstOp = new RelativeAddress(seq.readSWord()); break;
                case A32: dstOp = new RelativeAddress(seq.readSDword()); break;
                default: throw new UnsupportedOperationException("invalid address size: " + ctx.getAddressSize());
                }
                break;
            default:
                throw new UnsupportedOperationException("invalid jz-opcode 0F " + opcode2);
            } break;
        default:
            throw new UnsupportedOperationException("invalid jz " + opcode1);
        }

    }

    @Override
    public String getMnemonic() {
        switch(opcode2) {
        case 0x80: return "jo";
        case 0x81: return "jno";
        case 0x82: return "jb";
        case 0x83: return "jnb";
        case 0x84: return "jz";
        case 0x85: return "jnz";
        case 0x86: return "jbe";
        case 0x87: return "ja";
        case 0x88: return "js";
        case 0x89: return "jns";
        case 0x8A: return "jp";
        case 0x8B: return "jnp";
        case 0x8C: return "jl";
        case 0x8D: return "jge";
        case 0x8E: return "jle";
        case 0x8F: return "jg";
        default:
            throw new UnsupportedOperationException("invalid jz-opcode 0F " + opcode2);
        }
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp};
    }

}
