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
import java.util.logging.Logger;

import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.Model;
import kianxali.cpu.x86.X86Context;
import kianxali.decoder.Context;
import kianxali.image.ImageFile;
import kianxali.image.Section;

public class ELFFile extends ImageFile {
    private static final Logger LOG = Logger.getLogger("kianxali.image.elf");
    private final ELFHeader header;
    private final Map<Long, String> imports;
    private final Map<Long, String> stringTable;
    private List<Section> loadedSections;

    public ELFFile(Path path) throws IOException {
        super(path);
        this.imports = new HashMap<>();

        header = new ELFHeader(imageFile);
        stringTable = loadStringTable(header.getStringSection());
        loadSections();
        loadSymbols();
        loadRelocations();
    }

    public static boolean isELFFile(Path path) throws IOException {
        FileInputStream fileIn = new FileInputStream(path.toFile());
        DataInputStream dataIn = new DataInputStream(fileIn);
        long magic = dataIn.readInt() & 0xFFFFFFFFL;
        dataIn.close();
        fileIn.close();
        return magic == ELFHeader.ELF_MAGIC;
    }

    private Map<Long, String> loadStringTable(SectionHeader stringHeader) {
        Map<Long, String> res = new HashMap<>();
        if(stringHeader == null) {
            return res;
        }

        long startPos = stringHeader.getOffset();
        long endPos = stringHeader.getOffset() + stringHeader.getSize();
        imageFile.seek(startPos);
        while(imageFile.getPosition() < endPos) {
            long entryPos = imageFile.getPosition() - startPos;
            String entry = imageFile.readString();
            res.put(entryPos, entry);
        }
        return res;
    }

    private void loadSections() {
        loadedSections = new ArrayList<>(header.getSectionHeaders().size());
        for(SectionHeader section : header.getSectionHeaders()) {
            if(section.getAddress() == 0) {
                // only analyze sections that are actually loaded
                continue;
            }

            long nameIndex = section.getNameIndex();
            String name = stringTable.get(nameIndex);
            long start = section.getAddress();
            long end = start + section.getSize();
            long offset = section.getOffset();
            boolean executable = section.isExecutable();

            loadedSections.add(new ELFSection(name, offset, start, end, executable));
        }
    }

    private List<ELFSymbol> readSymbols(SectionHeader symSection) {
        List<ELFSymbol> res = new ArrayList<>();
        Map<Long, String> symStrTab = loadStringTable(header.getSectionHeaders().get((int) symSection.getLink()));
        imageFile.seek(symSection.getOffset());
        for(int i = 0; i < symSection.getSize() / symSection.getEntrySize(); i++) {
            ELFSymbol sym = new ELFSymbol(imageFile, header.has64BitHeader());
            String name = symStrTab.get(sym.getNameIndex());
            sym.attachName(name);
            res.add(sym);
        }
        return res;
    }

    private void loadSymbols() {
        for(SectionHeader section : header.getSectionHeaders()) {
            if(section.getType() != SectionHeader.Type.SHT_SYMTAB) {
                // we only want the symbol table here
                continue;
            }
            for(ELFSymbol sym : readSymbols(section)) {
                if(sym.getValue() != 0 && sym.getAttachedName() != null && sym.getType() == ELFSymbol.Type.STT_FUNC) {
                    imports.put(sym.getValue(), sym.getAttachedName());
                }
            }
        }
    }

    private void loadRelocations() {
        for(SectionHeader section : header.getSectionHeaders()) {
            if(section.getType() != SectionHeader.Type.SHT_REL && section.getType() != SectionHeader.Type.SHT_RELA) {
                // only analyze relocation tables
                continue;
            }
            SectionHeader linkedSection = header.getSectionHeaders().get(((int) section.getLink()));
            if(linkedSection.getType() != SectionHeader.Type.SHT_DYNSYM) {
                // not sure if that can happen, hopefully not
                LOG.warning("ELF SHT_REL/A with link not being SHT_DYNSYM");
                continue;
            }

            // parse linked DynSym table
            List<ELFSymbol> symbols = readSymbols(linkedSection);

            imageFile.seek(section.getOffset());
            for(int i = 0; i < section.getSize() / section.getEntrySize(); i++) {
                boolean addend = false;
                if(section.getType() == SectionHeader.Type.SHT_RELA) {
                    addend = true;
                }
                ELFRelocation rel = new ELFRelocation(imageFile, addend, header.has64BitHeader());
                if(rel.getType() != ELFRelocation.Type.JUMP_SLOT) {
                    // only analyze jump slots for now
                    continue;
                }
                String name = symbols.get((int) rel.getInfoIndex()).getAttachedName();
                imports.put(rel.getAddress(), name);
            }
        }
    }

    @Override
    public long getCodeEntryPointMem() {
        return header.getEntryPoint();
    }

    @Override
    public List<Section> getSections() {
        return Collections.unmodifiableList(loadedSections);
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
        for(Section sec : loadedSections) {
            ELFSection section = (ELFSection) sec;
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
        return Collections.unmodifiableMap(imports);
    }
}
