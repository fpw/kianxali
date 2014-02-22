package kianxali.image.elf;

import kianxali.image.ByteSequence;

public class SectionHeader {
    public enum Type {
        SHT_NULL,       // ignored
        SHT_PROGBITS,   // program data
        SHT_SYMTAB,     // symbol table
        SHT_STRTAB,     // string table
        SHT_RELA,       // relocation entries
        SHT_HASH,       // symbol hash table
        SHT_DYNA,       // dynamic linking information
        SHT_NOTE,       // notes
        SHT_NOBITS,     // null-initialized data (BSS)
        SHT_REL,        // relocation entries without addends
        SHT_DYNSYM,     // dynamic linker symbol table
        UNKNOWN
    };

    private final long nameIndex;
    private Type type;
    private long flags;
    private long address, offset, size, info;
    private long link; // link to other section
    private long align, entrySize;

    public SectionHeader(ByteSequence seq, boolean elf64) {
        nameIndex = seq.readUDword();

        int eType = (int) seq.readUDword();
        switch(eType) {
        case 0: type = Type.SHT_NULL; break;
        case 1: type = Type.SHT_PROGBITS; break;
        case 2: type = Type.SHT_SYMTAB; break;
        case 3: type = Type.SHT_STRTAB; break;
        case 4: type = Type.SHT_RELA; break;
        case 5: type = Type.SHT_HASH; break;
        case 6: type = Type.SHT_DYNA; break;
        case 7: type = Type.SHT_NOTE; break;
        case 8: type = Type.SHT_NOBITS; break;
        case 9: type = Type.SHT_REL; break;
        case 11: type = Type.SHT_DYNSYM; break;
        default: type = Type.UNKNOWN;
        }

        if(elf64) {
            flags = seq.readSQword();
            address = seq.readSQword();
            offset = seq.readSQword();
            size = seq.readSQword();
            link = seq.readUDword();
            info = seq.readUDword();
            align = seq.readSQword();
            entrySize = seq.readSQword();
        } else {
            flags = seq.readUDword();
            address = seq.readUDword();
            offset = seq.readUDword();
            size = seq.readUDword();
            link = seq.readUDword();
            info = seq.readUDword();
            align = seq.readUDword();
            entrySize = seq.readUDword();
        }
    }

    public long getNameIndex() {
        return nameIndex;
    }

    public Type getType() {
        return type;
    }

    public boolean isWritable() {
        return (flags & 1) != 0;
    }

    public boolean isExecutable() {
        return (flags & 4) != 0;
    }

    // virtual address for this section
    public long getAddress() {
        return address;
    }

    // file offset for this section
    public long getOffset() {
        return offset;
    }

    public long getSize() {
        return size;
    }

    public long getInfo() {
        return info;
    }

    public long getLink() {
        return link;
    }

    public long getAlign() {
        return align;
    }

    public long getEntrySize() {
        return entrySize;
    }
}
