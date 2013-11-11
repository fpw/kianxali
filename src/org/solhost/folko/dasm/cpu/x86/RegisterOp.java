package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.OutputOptions;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Register;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public class RegisterOp implements Operand {
    private final UsageType usage;
    private final Register register;

    public RegisterOp(UsageType usage, Register register) {
        this.usage = usage;
        this.register = register;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputOptions options) {
        return register.toString().toLowerCase();
    }
}
