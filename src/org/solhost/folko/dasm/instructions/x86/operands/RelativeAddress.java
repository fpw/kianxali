package org.solhost.folko.dasm.instructions.x86.operands;

public class RelativeAddress implements Operand {
    private final long relAddr;

    public RelativeAddress(long relAddr) {
        this.relAddr = relAddr;
    }

    public long getRelativeAddress() {
        return relAddr;
    }

    @Override
    public String asString(int flags) {
        throw new UnsupportedOperationException("can't show relative address");
    }

    @Override
    public String toString() {
        return asString(0);
    }
}
