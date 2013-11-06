package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.Context.OperandSize;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.PointerOp;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpMov extends Instruction {
    public Operand dstOp, srcOp;

    public OpMov(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        switch(opcode) {
        case 0x89: {
            ModRM modRM = new ModRM(seq, ctx);
            srcOp = modRM.getRegOp();
            dstOp = modRM.getMemOp();
            break;
        }
        case 0x8A:
        case 0x8B: {
            if(opcode == 0x8A) {
                ctx.setOpSizeOverride(OperandSize.O8);
            }
            ModRM modRM = new ModRM(seq, ctx);
            dstOp = modRM.getRegOp();
            srcOp = modRM.getMemOp();
            break;
        }
        case 0xA0: {
            dstOp = new RegisterOp(Register.AL);
            ctx.setOpSizeOverride(OperandSize.O8);
            switch(ctx.getAddressSize()) {
            case A16: srcOp = new PointerOp(ctx, seq.readUWord()); break;
            case A32: srcOp = new PointerOp(ctx, seq.readUDword()); break;
            default: throw new RuntimeException("invalid address size " + ctx.getAddressSize());
            }
            break;
        }
        case 0xA1: {
            switch(ctx.getAddressSize()) {
            case A16: srcOp = new PointerOp(ctx, seq.readUWord()); break;
            case A32: srcOp = new PointerOp(ctx, seq.readUDword()); break;
            default: throw new RuntimeException("invalid address size: " + ctx.getAddressSize());
            }

            switch(ctx.getOperandSize()) {
            case O16: dstOp = new RegisterOp(Register.AX); break;
            case O32: dstOp = new RegisterOp(Register.EAX); break;
            default: throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
            }
            break;
        }
        case 0xA3: {
            switch(ctx.getAddressSize()) {
            case A16: dstOp = new PointerOp(ctx, seq.readUWord()); break;
            case A32: dstOp = new PointerOp(ctx, seq.readUDword()); break;
            default: throw new RuntimeException("invalid address size: " + ctx.getAddressSize());
            }

            switch(ctx.getOperandSize()) {
            case O16: srcOp = new RegisterOp(Register.AX); break;
            case O32: srcOp = new RegisterOp(Register.EAX); break;
            default: throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
            }
            break;
        }
        case 0xB8:
        case 0xB9:
        case 0xBA:
        case 0xBB:
        case 0xBC:
        case 0xBD:
        case 0xBE:
        case 0xBF: {
            dstOp = new RegisterOp(Register.fromByte((short) (opcode - 0xB8), ctx, true));
            switch(ctx.getOperandSize()) {
            case O16: srcOp = new ImmediateOp(seq.readSWord()); break;
            case O32: srcOp = new ImmediateOp(seq.readSDword()); break;
            default: throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
            }
            break;
        }
        case 0xC7: {
            ModRM modRM = new ModRM(seq, ctx);
            dstOp = modRM.getMemOp();
            switch(ctx.getOperandSize()) {
            case O16: srcOp = new ImmediateOp(seq.readSWord()); break;
            case O32: srcOp = new ImmediateOp(seq.readSDword()); break;
            default: throw new RuntimeException("invalid operand size: " + ctx.getOperandSize());
            }
            break;
        }
        default:
            throw new RuntimeException("Unknown mov opcode: " + opcode);
        }
    }

    @Override
    public String getMnemonic() {
        return "mov";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] {dstOp, srcOp};
    }

}
