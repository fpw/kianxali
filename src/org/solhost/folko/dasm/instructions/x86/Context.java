package org.solhost.folko.dasm.instructions.x86;

public class Context {
    public enum OperandSize {O8, O16, O32};
    public enum AddressSize {A16, A32};

    private final CPUMode mode;
    private Segment segmentOverride;
    private boolean lockPrefix, repZPrefix, repNZPrefix;
    private boolean opSizePrefix, adrSizePrefix;
    private OperandSize opSizeOverride;

    public Context(CPUMode mode) {
        this.mode = mode;
    }

    public void reset() {
        segmentOverride = null;
        lockPrefix = false;
        repZPrefix = false;
        repNZPrefix = false;
        opSizePrefix = false;
        adrSizePrefix = false;
        opSizeOverride = null;
    }

    public CPUMode getMode() {
        return mode;
    }

    public void setOpSizeOverride(OperandSize pointerOpSize) {
        this.opSizeOverride = pointerOpSize;
    }

    public OperandSize getOpSizeOverride() {
        return opSizeOverride;
    }

    public void setSegmentOverride(Segment segmentOverride) {
        this.segmentOverride = segmentOverride;
    }

    public Segment getSegmentOverride() {
        return segmentOverride;
    }

    public void setLockPrefix(boolean lockPrefix) {
        this.lockPrefix = lockPrefix;
    }

    public boolean hasLockPrefix() {
        return lockPrefix;
    }

    public void setRepZPrefix(boolean repZPrefix) {
        this.repZPrefix = repZPrefix;
    }

    public boolean hasRepZPrefix() {
        return repZPrefix;
    }

    public void setRepNZPrefix(boolean repNZPrefix) {
        this.repNZPrefix = repNZPrefix;
    }

    public boolean hasRepNZPrefix() {
        return repNZPrefix;
    }

    public void setAdrSizePrefix(boolean adrSizePrefix) {
        this.adrSizePrefix = adrSizePrefix;
    }

    public void setOpSizePrefix(boolean opSizePrefix) {
        this.opSizePrefix = opSizePrefix;
    }

    public AddressSize getAddressSize() {
        switch(mode) {
        case REAL:
            return AddressSize.A16;
        case PROTECTED:
            if(adrSizePrefix) {
                return AddressSize.A16;
            } else {
                return AddressSize.A32;
            }
        default:
            throw new RuntimeException("invalid cpu mode: " + mode);
        }
    }

    public OperandSize getOperandSize() {
        switch(mode) {
        case REAL:
            return OperandSize.O16;
        case PROTECTED:
            if(opSizePrefix) {
                return OperandSize.O16;
            } else {
                return OperandSize.O32;
            }
        default:
            throw new RuntimeException("invalid cpu mode: " + mode);
        }
    }
}
