package kianxali.decoder.arch.x86;

import kianxali.decoder.Operand;
import kianxali.decoder.UsageType;
import kianxali.decoder.arch.x86.X86CPU.X86Register;
import kianxali.util.OutputFormatter;

/**
 * This class is used to describe operands that represent
 * direct register access.
 * @author fwi
 *
 */
public class RegisterOp implements Operand {
    private final UsageType usage;
    private final X86Register register;

    RegisterOp(UsageType usage, X86Register register) {
        this.usage = usage;
        this.register = register;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(OutputFormatter formatter) {
        return formatter.formatRegister(register.toString());
    }

    @Override
    public Number asNumber() {
        return null;
    }

    @Override
    public Short getPointerDestSize() {
        return null;
    }
}
