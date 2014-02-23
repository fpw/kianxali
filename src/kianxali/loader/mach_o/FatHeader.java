package kianxali.loader.mach_o;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import kianxali.loader.ByteSequence;

public class FatHeader {
    public static final long FAT_MAGIC = 0xBEBAFECAL;
    private final Map<Long, Long> entries;

    public FatHeader(ByteSequence seq) {
        long magic = seq.readUDword();
        seq.setByteOrder(ByteOrder.BIG_ENDIAN);
        long nArch = seq.readUDword();

        if(magic != FAT_MAGIC || nArch >= 20) {
            System.out.println(String.format("%X %X", magic, nArch));
            throw new UnsupportedOperationException("Invalid fat_header");
        }

        entries = new HashMap<>();
        for(long i = 0; i < nArch; i++) {
            long type = seq.readUDword();
            seq.readUDword(); // subtype
            long offset = seq.readUDword();
            seq.readUDword(); // size
            seq.readUDword(); // align
            entries.put(type, offset);
        }
        seq.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    public Map<Long, Long> getArchitectures() {
        return entries;
    }
}
