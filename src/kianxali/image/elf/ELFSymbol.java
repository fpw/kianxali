package kianxali.image.elf;

import kianxali.image.ByteSequence;

public class ELFSymbol {
    public enum Type {
        STT_NOTYPE,     // unspecified
        STT_OBJECT,     // data object
        STT_FUNC,       // code object
        STT_SECTION,    // symbol associated with a section
        STT_FILE,       // symbol name is a file name
        UNKNOWN
    }
    private Type type;
    private final long nameIndex, value, size;
    private int sectionIndex;
    private String attachedName;

    public ELFSymbol(ByteSequence seq, boolean elf64) {
        short symType;

        nameIndex = seq.readUDword();

        if(elf64) {
            symType = seq.readUByte();
            seq.readUByte(); // skip "other"
            sectionIndex = seq.readUWord();
            value = seq.readSQword();
            size = seq.readSQword();
        } else {
            value = seq.readUDword();
            size = seq.readUDword();
            symType = seq.readUByte();
            seq.readUByte(); // skip "other"
            sectionIndex = seq.readUWord();
        }

        switch(symType & 0xF) {
        case 0: type = Type.STT_NOTYPE; break;
        case 1: type = Type.STT_OBJECT; break;
        case 2: type = Type.STT_FUNC; break;
        case 3: type = Type.STT_SECTION; break;
        case 4: type = Type.STT_FILE; break;
        default: type = Type.UNKNOWN;
        }
    }

    public Type getType() {
        return type;
    }

    public long getNameIndex() {
        return nameIndex;
    }

    public long getValue() {
        return value;
    }

    public long getSize() {
        return size;
    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public void attachName(String name) {
        this.attachedName = name;
    }

    public String getAttachedName() {
        return attachedName;
    }
}
