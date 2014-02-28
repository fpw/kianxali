package kianxali.loader.mach_o;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kianxali.decoder.Context;
import kianxali.decoder.arch.x86.X86Context;
import kianxali.decoder.arch.x86.X86CPU.ExecutionMode;
import kianxali.decoder.arch.x86.X86CPU.Model;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

/**
 * Implements a loader for the Mach-O format. See package info for
 * the used references.
 * @author fwi
 *
 */
public class MachOFile extends ImageFile {
    private MachHeader machHeader;

    public MachOFile(Path path, long offset) throws IOException {
        super(path);
        loadHeaders(offset);
    }

    public static boolean isMachOFile(Path path) throws IOException {
        FileInputStream fileIn = new FileInputStream(path.toFile());
        DataInputStream dataIn = new DataInputStream(fileIn);
        long magic = Integer.reverseBytes(dataIn.readInt()) & 0xFFFFFFFFL;
        dataIn.close();
        fileIn.close();
        return magic == MachHeader.MH_MAGIC || magic == MachHeader.MH_MAGIC_64;
    }

    private void loadHeaders(long offset) {
        imageFile.lock();
        imageFile.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        machHeader = new MachHeader(imageFile, offset);
        imageFile.unlock();
    }

    @Override
    public List<Section> getSections() {
        List<MachSection> sections = machHeader.getSections();
        List<Section> res = new ArrayList<Section>(sections.size());
        for(Section sec : sections) {
            res.add(sec);
        }
        return res;
    }

    @Override
    public Context createContext() {
        if(machHeader.isMach64()) {
            return new X86Context(Model.ANY, ExecutionMode.LONG);
        } else {
            return new X86Context(Model.ANY, ExecutionMode.PROTECTED);
        }
    }

    @Override
    public long getCodeEntryPointMem() {
        return toMemAddress(machHeader.getEntryPoint());
    }

    @Override
    public long toFileAddress(long memAddress) {
        MachSection section = (MachSection) getSectionForMemAddress(memAddress);
        long diff = memAddress - section.getStartAddress();
        return section.getFileOffset() + diff;
    }

    @Override
    public long toMemAddress(long fileOffset) {
        for(MachSection section : machHeader.getSections()) {
            if(fileOffset >= section.getFileOffset() && fileOffset <= section.getFileOffset() + section.getVirtualSize()) {
                long diff = fileOffset - section.getFileOffset();
                return section.getStartAddress() + diff;
            }
        }
        throw new UnsupportedOperationException("invalid file offset: " + fileOffset);
    }

    @Override
    public Map<Long, String> getImports() {
        if(machHeader.getSymbolTable() != null) {
            Map<Long, String> res = machHeader.getSymbolTable().getSymbols();
            if(res != null) {
                return res;
            }
        }
        return new HashMap<Long, String>();
    }
}
