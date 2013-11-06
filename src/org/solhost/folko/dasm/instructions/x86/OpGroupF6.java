package org.solhost.folko.dasm.instructions.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.instructions.x86.operands.ImmediateOp;
import org.solhost.folko.dasm.instructions.x86.operands.Operand;
import org.solhost.folko.dasm.instructions.x86.operands.RegisterOp;

public class OpGroupF6 extends Instruction {
    private Operand op1, op2, op3, op4;
    private final short extID;

    public OpGroupF6(ByteSequence seq, Context ctx) {
        short opcode = seq.readUByte();
        if(opcode != 0xF6) {
            throw new UnsupportedOperationException("invalid F6 opcode: " + opcode);
        }
        extID = getOpcodeExtension(seq);
        switch(extID) {
        case 0:
        case 1: {
            ModRM modRM = new ModRM(seq, ctx);
            op1 = modRM.getMemOp();
            op2 = new ImmediateOp(seq.readUByte());
        } break;
        case 2:
        case 3: {
            ModRM modRM = new ModRM(seq, ctx);
            op1 = modRM.getMemOp();
        } break;
        case 4:
        case 5: {
            ModRM modRM = new ModRM(seq, ctx);
            op1 = new RegisterOp(Register.AX);
            op2 = new RegisterOp(Register.AL);
            op3 = modRM.getMemOp();
        } break;
        case 6:
        case 7:
            ModRM modRM = new ModRM(seq, ctx);
            op1 = new RegisterOp(Register.AL);
            op2 = new RegisterOp(Register.AH);
            op3 = new RegisterOp(Register.AX);
            op4 = modRM.getMemOp();
        default: throw new UnsupportedOperationException("unsupported F6 extension: " + extID);
        }
    }

    @Override
    public String getMnemonic() {
        switch(extID) {
        case 0: return "test";
        case 1: return "test";
        case 2: return "not";
        case 3: return "neg";
        case 4: return "mul";
        case 5: return "imul";
        case 6: return "div";
        case 7: return "idiv";
        default: throw new UnsupportedOperationException("unsupported F6 extension: " + extID);
        }
    }

    @Override
    public Operand[] getOperands() {
        switch(extID) {
        case 0: return new Operand[] {op1, op2};
        case 1: return new Operand[] {op1, op2};
        case 2: return new Operand[] {op1};
        case 3: return new Operand[] {op1};
        case 4: return new Operand[] {op1, op2, op3};
        case 5: return new Operand[] {op1, op2, op3};
        case 6: return new Operand[] {op1, op2, op3, op4};
        case 7: return new Operand[] {op1, op2, op3, op4};
        default: throw new UnsupportedOperationException("unsupported F6 extension: " + extID);
        }
    }

}
