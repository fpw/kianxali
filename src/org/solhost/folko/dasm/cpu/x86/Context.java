package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeSyntax;

public class Context {
    private final Model model;
    private final ExecutionMode execMode;
    private List<Short> opcodePrefix;

    private long fileOffset, virtualOffset;

    // prefixes
    private Segment overrideSegment;
    private boolean lockPrefix, waitPrefix;
    private boolean repZPrefix, repNZPrefix, opSizePrefix, adrSizePrefix;
    private boolean rexWPrefix, rexRPrefix, rexBPrefix;

    public Context(Model model, ExecutionMode execMode) {
        this.model = model;
        this.execMode = execMode;
        reset();
    }

    public void setFileOffset(long offset) {
        this.fileOffset = offset;
    }

    public void setVirtualOffset(long offset) {
        this.virtualOffset = offset;
    }

    public boolean acceptsOpcode(OpcodeSyntax syntax) {
        if(!syntax.getOpcodeEntry().supportedProcessors.contains(model)) {
            return false;
        }
        return true;
    }

    public void applyPrefix(Instruction inst) {
        OpcodeEntry opcode = inst.getOpcode();
        if(!opcode.belongsTo(OpcodeGroup.PREFIX)) {
            throw new UnsupportedOperationException("not a prefix");
        }

        switch(opcode.opcode) {
        case 0xF0: lockPrefix = true; break;
        case 0xF2: repNZPrefix = true; break;
        case 0xF3: repZPrefix = true; break;
        case 0x2E: overrideSegment = Segment.CS; break;
        case 0x36: overrideSegment = Segment.SS; break;
        case 0x3E: overrideSegment = Segment.DS; break;
        case 0x26: overrideSegment = Segment.ES; break;
        case 0x64: overrideSegment = Segment.FS; break;
        case 0x65: overrideSegment = Segment.GS; break;
        case 0x66: opSizePrefix = true; break;
        case 0x67: adrSizePrefix = true; break;
        case 0x9B: waitPrefix = true; break;
        default:
            throw new UnsupportedOperationException("unknown prefix: " + opcode);
        }
    }

    public void addDecodedPrefix(short b) {
        opcodePrefix.add(b);
    }

    public List<Short> getDecodedPrefix() {
        return new ArrayList<Short>(opcodePrefix);
    }

    public void removeDecodedPrefixTop() {
        int size = opcodePrefix.size();
        if(size > 0) {
            opcodePrefix.remove(size - 1);
        }
    }

    public short getFromDecodedPrefix(int idx) {
        return opcodePrefix.get(idx);
    }

    public Model getModel() {
        return model;
    }

    public ExecutionMode getExecMode() {
        return execMode;
    }

    public Segment getOverrideSegment() {
        return overrideSegment;
    }

    public boolean hasLockPrefix() {
        return lockPrefix;
    }

    public boolean hasWaitPrefix() {
        return waitPrefix;
    }

    public boolean hasRepZPrefix() {
        return repZPrefix;
    }

    public boolean hasRepNZPrefix() {
        return repNZPrefix;
    }

    public boolean hasOpSizePrefix() {
        return opSizePrefix;
    }

    public boolean hasAdrSizePrefix() {
        return adrSizePrefix;
    }

    public boolean hasRexWPrefix() {
        return rexWPrefix;
    }

    public boolean hasRexBPrefix() {
        return rexBPrefix;
    }

    public boolean hasRexRPrefix() {
        return rexRPrefix;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public long getVirtualOffset() {
        return virtualOffset;
    }

    public void reset() {
        opcodePrefix = new ArrayList<>(5);
        overrideSegment = null;
        lockPrefix = false;
        waitPrefix = false;
        repZPrefix = false;
        repNZPrefix = false;
        opSizePrefix = false;
        adrSizePrefix = false;
    }
}
