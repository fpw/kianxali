package org.solhost.folko.dasm.instructions.x86.operands;

public class ImmediateOp implements Operand {
    private final long value;

    public ImmediateOp(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String asString(int flags) {
        if(value >= 0) {
            return String.format("%Xh", value);
        } else {
            return String.format("-%Xh", -value);
        }
    }

    @Override
    public String toString() {
        return asString(0);
    }
}
