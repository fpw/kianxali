package kianxali.image.mach_o;

import java.util.ArrayList;
import java.util.List;

import kianxali.image.ByteSequence;

public class MachHeader {
    public static final long MH_MAGIC           = 0xFEEDFACEL;
    public static final long MH_MAGIC_64        = 0xFEEDFACFL;
    public static final long CPU_TYPE_X86       = 7L;
    public static final long CPU_TYPE_X86_64    = 0x01000007L;
    public static final long LC_SEGMENT         = 0x1L;
    public static final long LC_DYLD_INFO_ONLY  = 0x80000022L;
    public static final long LC_SEGMENT_64      = 0x19L;
    public static final long LC_MAIN            = 0x80000028L;

    private boolean mach64;
    private final long startOffset;
    private long entryPoint;
    private final List<MachSegment> segments;
    private final List<MachSection> sections;
    private SymbolTable symbolTable;

    public MachHeader(ByteSequence seq, long offset) {
        startOffset = offset;
        segments = new ArrayList<>();
        sections = new ArrayList<>();

        seq.seek(offset);
        long magic = seq.readUDword();
        if(magic == MH_MAGIC) {
            mach64 = false;
        } else if(magic == MH_MAGIC_64) {
            mach64 = true;
        } else {
            throw new UnsupportedOperationException(String.format("Invalid Mach-O magic: %8X",  magic));
        }

        long cpuType = seq.readSDword();
        if(cpuType != CPU_TYPE_X86 && cpuType != CPU_TYPE_X86_64) {
            throw new UnsupportedOperationException("Invalid Mach-O CPU type: " + cpuType);
        }

        // subtype
        seq.readSDword();

        // filetype
        seq.readUDword();

        long numLoadCommands = seq.readUDword();

        // number of bytes
        seq.readUDword();

        // flags
        seq.readUDword();

        if(mach64) {
            // reserved
            seq.readUDword();
        }

        // process load commands
        for(int i = 0; i < numLoadCommands; i++) {
            long cmd = seq.readUDword();
            long size = seq.readUDword();

            if(cmd == LC_SEGMENT_64 || cmd == LC_SEGMENT) {
                MachSegment segment = new MachSegment(seq, startOffset, mach64);
                for(MachSection section : segment.getSections()) {
                    sections.add(section);
                }
                segments.add(segment);
            } else if(cmd == LC_DYLD_INFO_ONLY) {
                symbolTable = new SymbolTable(seq, startOffset, mach64);
            } else if(cmd == LC_MAIN) {
                if(mach64) {
                    entryPoint = startOffset + seq.readSQword();
                    seq.readSQword();
                } else {
                    entryPoint = startOffset + seq.readUDword();
                    seq.readUDword();
                }
            } else {
                seq.skip(size - 8);
            }
        }

        if(symbolTable != null) {
            symbolTable.load(segments, seq, mach64);
        }
    }

    public long getStartOffset() {
        return startOffset;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public boolean isMach64() {
        return mach64;
    }

    public long getEntryPoint() {
        return entryPoint;
    }

    public List<MachSection> getSections() {
        return sections;
    }
}
