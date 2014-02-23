package kianxali.image.elf;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import kianxali.image.ByteSequence;

public class ELFHeader {
    public static final long ELF_MAGIC = 0x7F454C46;
    private enum Machine {X86, X86_64};
    private boolean elf64; // header format, not machine
    private Machine machine;
    private long entryPoint;
    private long programOffset, sectionOffset;
    private final int progHeaderCount;
    private final int sectHeaderCount, sectHeaderStringIndex;

    private List<ProgramHeader> programHeaders;
    private List<SectionHeader> sectionHeaders;

    public ELFHeader(ByteSequence seq) {
        seq.setByteOrder(ByteOrder.BIG_ENDIAN);
        long magic = seq.readUDword();
        if(magic != ELF_MAGIC) {
            throw new UnsupportedOperationException("invalid magic");
        }

        short eiClass = seq.readUByte();
        if(eiClass == 1) {
            elf64 = false;
        } else if(eiClass == 2) {
            elf64 = true;
        } else {
            throw new UnsupportedOperationException("invalid EI_CLASS: " + eiClass);
        }

        short eiData = seq.readUByte();
        if(eiData == 1) {
            seq.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else if(eiData == 2){
            seq.setByteOrder(ByteOrder.BIG_ENDIAN);
        } else {
            throw new UnsupportedOperationException("invalid EI_DATA: " + eiData);
        }

        short eiVersion = seq.readUByte();
        if(eiVersion != 1) {
            throw new UnsupportedOperationException("invalid EI_VERSION: " + eiVersion);
        }

        // skip uninteresting parts
        seq.skip(9);

        int eType = seq.readUWord();
        if(eType != 2 && eType != 3) {
            throw new UnsupportedOperationException("invalid e_type: " + eType);
        }

        int eMachine = seq.readUWord();
        if(eMachine == 3 || eMachine == 6) {
            machine = Machine.X86;
        } else if(eMachine == 62) {
            machine = Machine.X86_64;
        }

        long eVersion = seq.readUDword();
        if(eVersion != 1) {
            throw new UnsupportedOperationException("invalid e_version: " + eVersion);
        }

        if(elf64) {
            entryPoint = seq.readSQword(); // TODO: UQ
            programOffset = seq.readSQword();
            sectionOffset = seq.readSQword();
        } else {
            entryPoint = seq.readUDword();
            programOffset = seq.readUDword();
            sectionOffset = seq.readUDword();
        }

        seq.readUDword(); // unused machine flags
        seq.readUWord(); // unused ELF header size

        /* progHeaderSize = */ seq.readUWord();
        progHeaderCount = seq.readUWord();
        /* sectHeaderSize = */ seq.readUWord();
        sectHeaderCount = seq.readUWord();
        sectHeaderStringIndex = seq.readUWord();

        loadProgramHeaders(seq);
        loadSectionHeaders(seq);
    }

    private void loadProgramHeaders(ByteSequence seq) {
        if(progHeaderCount <= 0) {
            return;
        }
        programHeaders = new ArrayList<>(progHeaderCount);

        seq.seek(programOffset);
        for(int i = 0; i < progHeaderCount; i++) {
            programHeaders.add(new ProgramHeader(seq, elf64));
        }
    }

    private void loadSectionHeaders(ByteSequence seq) {
        if(sectHeaderCount <= 0) {
            return;
        }
        sectionHeaders = new ArrayList<>(sectHeaderCount);

        seq.seek(sectionOffset);
        for(int i = 0; i < sectHeaderCount; i++) {
            sectionHeaders.add(new SectionHeader(seq, elf64));
        }
    }

    public SectionHeader getStringSection() {
        return sectionHeaders.get(sectHeaderStringIndex);
    }

    public long getEntryPoint() {
        return entryPoint;
    }

    public boolean has64BitHeader() {
        return elf64;
    }

    public boolean has64BitCode() {
        return machine == Machine.X86_64;
    }

    public List<ProgramHeader> getProgramHeaders() {
        return programHeaders;
    }

    public List<SectionHeader> getSectionHeaders() {
        return sectionHeaders;
    }
}
