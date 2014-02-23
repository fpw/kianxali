package kianxali.loader.mach_o;

import kianxali.loader.ByteSequence;
import kianxali.loader.Section;

public class MachSection implements Section {
    private final String name, segmentName;
    private final long virtualAddress, virtualSize, fileOffset, flags;

    public MachSection(ByteSequence seq, long startOffset, boolean mach64) {
        name = seq.readString(16);
        segmentName = seq.readString(16);

        if(mach64) {
            virtualAddress = seq.readSQword();
            virtualSize = seq.readSQword();
        } else {
            virtualAddress = seq.readUDword();
            virtualSize = seq.readUDword();
        }
        fileOffset = startOffset + seq.readUDword();

        seq.skip(3 * 4);
        flags = seq.readUDword();
        seq.skip(2 * 4);
        if(mach64) {
            seq.skip(4);
        }
    }

    public String getSegmentName() {
        return segmentName;
    }

    public String getName() {
        return name;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    @Override
    public long getStartAddress() {
        return virtualAddress;
    }

    @Override
    public long getEndAddress() {
        return getStartAddress() + getVirtualSize();
    }

    @Override
    public boolean isExecutable() {
        return ((flags & (1 << 31)) != 0);
    }
}
