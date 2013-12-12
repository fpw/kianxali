package kianxali.image.mach_o;

import kianxali.image.ByteSequence;
import kianxali.image.Section;

public class MachSection implements Section {
    private final String name, segmentName;
    private final long virtualAddress, virtualSize, fileOffset;

    public MachSection(ByteSequence seq, boolean mach64) {
        name = seq.readString(16);
        segmentName = seq.readString(16);

        if(mach64) {
            virtualAddress = seq.readSQword();
            virtualSize = seq.readSQword();
        } else {
            virtualAddress = seq.readUDword();
            virtualSize = seq.readUDword();
        }
        fileOffset = seq.readUDword();

        seq.skip(6 * 4);
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
}
