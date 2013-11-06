package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solhost.folko.dasm.instructions.x86.CPUMode;

public class OpcodeHandler {
    public enum Mode {R, P, E, S};
    private final boolean isTwoByte;
    private final short opcode;
    private final Map<CPUMode, OpcodeOpts> modeOpts;
    private final List<Syntax> syntaxes;

    public OpcodeHandler(boolean isTwoByte, short opcode) {
        this.syntaxes = new ArrayList<>(4);
        this.modeOpts = new HashMap<>();
        this.isTwoByte = isTwoByte;
        this.opcode = opcode;
    }

    public void addModeAttributes(CPUMode mode, OpcodeOpts opts) {
        modeOpts.put(mode, opts);
    }

    public void addSyntax(Syntax syntax) {
        syntaxes.add(syntax);
    }

    public boolean isTwoByte() {
        return isTwoByte;
    }

    public short getOpcode() {
        return opcode;
    }
}
