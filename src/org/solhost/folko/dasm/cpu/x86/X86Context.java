package org.solhost.folko.dasm.cpu.x86;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.Context;
import org.solhost.folko.dasm.ImageFile;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Segment;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeSyntax;

public class X86Context implements Context {
    private final Model model;
    private final ExecutionMode execMode;
    private final ImageFile image;
    private List<Short> opcodePrefix;

    private long fileOffset;

    // prefixes
    private Segment overrideSegment;
    private boolean lockPrefix, waitPrefix;
    private boolean repZPrefix, repNZPrefix, opSizePrefix, adrSizePrefix;
    private boolean rexWPrefix, rexRPrefix, rexBPrefix, rexXPrefix;

    public X86Context(ImageFile image, Model model, ExecutionMode execMode) {
        this.model = model;
        this.execMode = execMode;
        this.image = image;
        reset();
    }

    public boolean acceptsOpcode(OpcodeSyntax syntax) {
        if(model != Model.ANY) {
            if(!syntax.getOpcodeEntry().supportedProcessors.contains(model)) {
                return false;
            }
        }

        switch(syntax.getOpcodeEntry().mode) {
        case LONG:
            if(execMode != ExecutionMode.LONG) {
                return false;
            } break;
        case PROTECTED:
            if(execMode == ExecutionMode.REAL) {
                return false;
            } break;
        case REAL:
        case SMM:
            break;
        default:        throw new UnsupportedOperationException("invalid execution mode: " + syntax.getOpcodeEntry().mode);
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
        case 0x40:
        case 0x41:
        case 0x42:
        case 0x43:
        case 0x44:
        case 0x45:
        case 0x46:
        case 0x48:
        case 0x49:
        case 0x4A:
        case 0x4B:
        case 0x4C:
        case 0x4D:
        case 0x4E:
        case 0x4F:
                rexWPrefix = (opcode.opcode & 8) != 0;
                rexRPrefix = (opcode.opcode & 4) != 0;
                rexXPrefix = (opcode.opcode & 2) != 0;
                rexBPrefix = (opcode.opcode & 1) != 0;
                break;
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

    public boolean hasRexXPrefix() {
        return rexXPrefix;
    }

    public void setFileOffset(long offset) {
        this.fileOffset = offset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public long getVirtualAddress() {
        return image.fileToMemAddress(getFileOffset());
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
