package org.solhost.folko.dasm.pe;

import java.io.IOException;
import java.nio.ByteOrder;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.Context;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;

public class PEFile implements AddressConverter {
    private final ByteSequence image;
    private DOSStub dosStub;
    private PEHeader peHeader;
    private OptionalHeader optionalHeader;
    private SectionHeader[] sectionHeaders;
    private Imports imports;

    public PEFile(String path) throws IOException {
        image = ByteSequence.fromFile(path);
    }

    public Context createContext() {
        // TODO
        return new Context(Model.CORE_I7, ExecutionMode.PROTECTED, this);
    }

    public void load() {
        loadHeaders();
        loadImports();
        imports.equals(imports); // XXX remove
    }

    public ByteSequence getEntryPoint() {
        image.seek(rvaToFile(optionalHeader.getEntryPointRVA()));
        return image;
    }

    @Override
    public long rvaToMemory(long rva) {
        return rva + optionalHeader.getImageBase();
    }

    @Override
    public long rvaToFile(long rva) {
        for(SectionHeader header : sectionHeaders) {
            long memOffset = header.getVirtualAddressRVA();
            if(rva >= memOffset && rva < memOffset + header.getRawSize()) {
                return rva - memOffset + header.getFilePosition();
            }
        }
        throw new IllegalArgumentException("invalid rva: " + rva);
    }

    @Override
    public long fileToRVA(long offset) {
        for(SectionHeader header : sectionHeaders) {
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

        sectionHeaders = new SectionHeader[peHeader.getNumSections()];
        for(int i = 0; i< peHeader.getNumSections(); i++) {
            sectionHeaders[i] = new SectionHeader(image);
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
        }
    }

    public long getFirstMemAddress() {
        return optionalHeader.getImageBase();
    }

    public long getMemorySize() {
        SectionHeader last = sectionHeaders[sectionHeaders.length - 1];
        long lastAddress = last.getVirtualAddressRVA() + last.getRawSize();
        return lastAddress;
    }
}
