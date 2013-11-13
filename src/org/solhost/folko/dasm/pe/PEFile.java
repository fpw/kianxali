package org.solhost.folko.dasm.pe;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.ImageFile;
import org.solhost.folko.dasm.Section;
import org.solhost.folko.dasm.cpu.x86.X86Context;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;

public class PEFile implements AddressConverter, ImageFile {
    private final ByteSequence image;
    private DOSStub dosStub;
    private PEHeader peHeader;
    private OptionalHeader optionalHeader;
    private List<PESection> sections;
    private Imports imports;

    public PEFile(String path) throws IOException {
        image = ByteSequence.fromFile(path);
    }

    public X86Context createContext() {
        // TODO
        return new X86Context(Model.ANY, ExecutionMode.PROTECTED);
    }

    public void load() {
        loadHeaders();
        loadImports();
    }

    @Override
    public long rvaToMemory(long rva) {
        return rva + optionalHeader.getImageBase();
    }

    @Override
    public long rvaToFile(long rva) {
        for(PESection header : sections) {
            long memOffset = header.getVirtualAddressRVA();
            if(rva >= memOffset && rva < memOffset + header.getRawSize()) {
                return rva - memOffset + header.getFilePosition();
            }
        }
        throw new IllegalArgumentException("invalid rva: " + rva);
    }

    @Override
    public long fileToRVA(long offset) {
        for(PESection header : sections) {
            long fileOffset = header.getFilePosition();
            if(offset >= fileOffset && offset < fileOffset + header.getRawSize()) {
                return offset - header.getFilePosition() + header.getVirtualAddressRVA();
            }
        }
        throw new IllegalArgumentException("invalid offset: " + offset);
    }

    @Override
    public long fileToMemory(long offset) {
        return rvaToMemory(fileToRVA(offset));
    }

    @Override
    public long memoryToRVA(long mem) {
        return mem - optionalHeader.getImageBase();
    }

    @Override
    public long memoryToFile(long mem) {
        return rvaToFile(memoryToRVA(mem));
    }

    private void loadHeaders() {
        image.lock();
        image.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        image.seek(0);
        dosStub = new DOSStub(image);

        image.seek(dosStub.getPEPointer());
        peHeader = new PEHeader(image);

        optionalHeader = new OptionalHeader(image);

        sections = new ArrayList<>(peHeader.getNumSections());
        for(int i = 0; i< peHeader.getNumSections(); i++) {
            sections.add(i, new PESection(image, this));
        }
        image.unlock();
    }

    private void loadImports() {
        long importsRVA = optionalHeader.getDataDirectoryOffsetRVA(OptionalHeader.DATA_DIRECTORY_IMPORT);
        if(importsRVA != 0) {
            image.lock();
            image.seek(rvaToFile(importsRVA));
            imports = new Imports(image, this);
            image.unlock();
        } else {
            // no imports
            imports = new Imports();
            imports.getDLLName(0); // XXX: remove
        }
    }

    public long getFirstMemAddress() {
        return optionalHeader.getImageBase();
    }

    public long getMemorySize() {
        PESection last = sections.get(sections.size() - 1);
        long lastAddress = last.getVirtualAddressRVA() + last.getRawSize();
        return lastAddress;
    }

    @Override
    public List<Section> getSections() {
        List<Section> res = new ArrayList<>(sections.size());
        for(Section section : sections) {
            res.add(section);
        }
        return Collections.unmodifiableList(res);
    }

    @Override
    public Section getSectionForMemAddress(long memAddress) {
        for(Section sec : sections) {
            if(memAddress >= sec.getStartAddress() && memAddress < sec.getEndAddress()) {
                return sec;
            }
        }
        return null;
    }

    @Override
    public long memToFileAddress(long memAddress) {
        return memoryToFile(memAddress);
    }

    @Override
    public long fileToMemAddress(long fileOffset) {
        return fileToMemory(fileOffset);
    }

    @Override
    public long getCodeEntryPointMem() {
        return rvaToMemory(optionalHeader.getEntryPointRVA());
    }

    @Override
    public ByteSequence getByteSequence(long memAddress) {
        image.seek(memToFileAddress(memAddress));
        return image;
    }
}
