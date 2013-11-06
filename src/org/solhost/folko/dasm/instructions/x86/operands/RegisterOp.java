package org.solhost.folko.dasm.instructions.x86.operands;

import org.solhost.folko.dasm.instructions.x86.Register;

public class RegisterOp implements Operand {
    private final Register reg;

    public RegisterOp(Register reg) {
        this.reg = reg;
    }

    @Override
    public String asString(int flags) {
        return reg.toString();
    }

    @Override
    public String toString() {
        return asString(0);
    }
}
