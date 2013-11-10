package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.decoder.Operand;
import org.solhost.folko.dasm.xml.OpcodeOperand.UsageType;

public class OffsetOp implements Operand {
    private final UsageType usage;
    private final long offset;
    private final Segment segmentOverride;

    public OffsetOp(UsageType usage, long offset, Segment segment) {
        this.usage = usage;
        this.offset = offset;
        this.segmentOverride = segment;
    }

    @Override
    public UsageType getUsage() {
        return usage;
    }

    @Override
    public String asString(Object options) {
        String seg;
        if(segmentOverride == null) {
            seg = "";
        } else {
            seg = segmentOverride.toString().toLowerCase();
        }
        if(offset < 0) {
            return String.format("%s:[-%Xh]", seg, -offset);
        } else if(offset > 0) {
            return String.format("%s:[%Xh]", seg, offset);
        } else {
            return String.format("%s:[0]", seg);
        }
    }
}
