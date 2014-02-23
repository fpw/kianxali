package kianxali.image.elf;

import kianxali.image.ByteSequence;

public class ELFRelocation {
    public enum Type { JUMP_SLOT, UNKNOWN };
    private long address;
    private long entryIndex;
    private long addend;
    private Type type;

    public ELFRelocation(ByteSequence seq, boolean hasAddend, boolean elf64) {
        long typ;

        if(elf64) {
            address = seq.readSQword();
            long info = seq.readSQword();
            if(hasAddend) {
                addend = seq.readSQword();
            }
            typ = (info & 0xFFFFFFFFL);
            entryIndex = info >> 32;
        } else {
            address = seq.readUDword();
            long info = seq.readUDword();
            if(hasAddend) {
                addend = seq.readSDword();
            }
            typ = (info & 0xFF);
            entryIndex = info >> 8;
        }

        if(typ == 7) {
            type = Type.JUMP_SLOT;
        } else {
            type = Type.UNKNOWN;
        }
    }

    public long getAddress() {
        return address;
    }

    public long getAddend() {
        return addend;
    }

    public long getInfoIndex() {
        return entryIndex;
    }

    public Type getType() {
        return type;
    }
}
