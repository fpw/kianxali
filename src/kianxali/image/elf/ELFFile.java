package kianxali.image.elf;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.Model;
import kianxali.cpu.x86.X86Context;
import kianxali.decoder.Context;
import kianxali.image.ImageFile;
import kianxali.image.Section;

public class ELFFile extends ImageFile {
    private final ELFHeader header;
    private final Map<Long, String> stringTable;
    private final List<Section> sections;

    public ELFFile(Path path) throws IOException {
        super(path);
        this.header = new ELFHeader(imageFile);
        this.stringTable = new HashMap<>();
        this.sections = new ArrayList<>(header.getSectionHeaders().size());

        loadStrings();
        loadSections();
    }

    public static boolean isELFFile(Path path) throws IOException {
        FileInputStream fileIn = new FileInputStream(path.toFile());
        DataInputStream dataIn = new DataInputStream(fileIn);
        long magic = dataIn.readInt() & 0xFFFFFFFFL;
        dataIn.close();
        fileIn.close();
        return magic == ELFHeader.ELF_MAGIC;
    }

    private void loadStrings() {
        SectionHeader stringHeader = header.getStringSection();
        if(stringHeader == null) {
            return;
        }

        long startPos = stringHeader.getOffset();
        long endPos = stringHeader.getOffset() + stringHeader.getSize();
        imageFile.seek(startPos);
        while(imageFile.getPosition() < endPos) {
            long entryPos = imageFile.getPosition() - startPos;
            String entry = imageFile.readString();
            stringTable.put(entryPos, entry);
        }
    }

    private void loadSections() {
        for(SectionHeader section : header.getSectionHeaders()) {
            if(section.getAddress() == 0) {
                // only load sections that are actually loaded
                continue;
            }

            long nameIndex = section.getNameIndex();
            String name = stringTable.get(nameIndex);
            long start = section.getAddress();
            long end = start + section.getSize();
            long offset = section.getOffset();
            boolean executable = section.isExecutable();

            sections.add(new ELFSection(name, offset, start, end, executable));
        }
    }

    @Override
    public long getCodeEntryPointMem() {
        return header.getEntryPoint();
    }

    @Override
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public Context createContext() {
        if(header.has64BitCode()) {
            return new X86Context(Model.ANY, ExecutionMode.LONG);
        } else {
            return new X86Context(Model.ANY, ExecutionMode.PROTECTED);
        }
    }

    @Override
    public long toFileAddress(long memAddress) {
        ELFSection section = (ELFSection) getSectionForMemAddress(memAddress);
        long diff = memAddress - section.getStartAddress();
        return section.getOffset() + diff;
   }

    @Override
    public long toMemAddress(long fileOffset) {
        for(Section section_ : sections) {
            ELFSection section = (ELFSection) section_;
            long size = section.getEndAddress() - section.getStartAddress();
            if(fileOffset >= section.getOffset() && fileOffset <= section.getOffset() + size) {
                long diff = fileOffset - section.getOffset();
                return section.getStartAddress() + diff;
            }
        }
        throw new UnsupportedOperationException("invalid file offset: " + fileOffset);
    }

    @Override
    public Map<Long, String> getImports() {
        return new HashMap<Long, String>();
    }
}
