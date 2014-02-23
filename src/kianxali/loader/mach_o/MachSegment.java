package kianxali.loader.mach_o;

import kianxali.loader.ByteSequence;

public class MachSegment {
    private final String name;
    private long virtualAddress, virtualSize, fileOffset, fileSize;
    private final MachSection[] sections;

    public MachSegment(ByteSequence seq, long startOffset, boolean mach64) {
        name = seq.readString(16);
        if(mach64) {
            virtualAddress = seq.readSQword();
            virtualSize = seq.readSQword();
            fileOffset = startOffset + seq.readSQword();
            fileSize = seq.readSQword();
        } else {
            virtualAddress = seq.readUDword();
            virtualSize = seq.readUDword();
            fileOffset = startOffset + seq.readUDword();
            fileSize = seq.readUDword();
        }
        // memory protection
        seq.skip(2 * 4);
        long numSections = seq.readUDword();
        // flags
        seq.skip(4);
        sections = new MachSection[(int) numSections];
        for(int i = 0; i < numSections; i++) {
            sections[i] = new MachSection(seq, startOffset, mach64);
        }
    }

    public String getName() {
        return name;
    }

    public MachSection[] getSections() {
        return sections;
    }

    public long getVirtualAddress() {
        return virtualAddress;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public long getFileSize() {
        return fileSize;
    }
}
