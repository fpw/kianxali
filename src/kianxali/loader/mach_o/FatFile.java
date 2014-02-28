package kianxali.loader.mach_o;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kianxali.decoder.Context;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

/**
 * Implements a parser for the fat file format that can include Mach-O files
 * for difference architectures.
 * @author fwi
 *
 */
public class FatFile extends ImageFile {
    private final FatHeader fatHeader;

    public FatFile(Path path) throws IOException {
        super(path);

        imageFile.seek(0);
        fatHeader = new FatHeader(imageFile);
    }

    public static boolean isFatFile(Path path) throws IOException {
        FileInputStream fileIn = new FileInputStream(path.toFile());
        DataInputStream dataIn = new DataInputStream(fileIn);
        // Java classes and OS X Fat binaries use the same magic
        // Hack: The 2nd field of Java specifies the file format version (starting at 43)
        // while the 2nd field of a fat header specifies the number of architectures (at most 19)
        // Source of information: http://www.puredarwin.org/developers/universal-binaries
        long magic1 = Integer.reverseBytes(dataIn.readInt()) & 0xFFFFFFFFL;
        long magic2 = dataIn.readInt() & 0xFFFFFFFFL;
        dataIn.close();
        fileIn.close();
        return magic1 == FatHeader.FAT_MAGIC && magic2 < 20;
    }

    public Map<String, Long> getArchitectures() {
        Map<Long, Long> archMap = fatHeader.getArchitectures();
        Map<String, Long> res = new HashMap<>();
        for(Long type : archMap.keySet()) {
            long offset = archMap.get(type);
            if(type == MachHeader.CPU_TYPE_X86) {
                res.put("x86", offset);
            } else if(type == MachHeader.CPU_TYPE_X86_64) {
                res.put("x86_64", archMap.get(type));
            } else {
                res.put(String.format("Unknown CPU type %d", type), offset);
            }
        }
        return res;
    }

    @Override
    public List<Section> getSections() {
        throw new UnsupportedOperationException("not a mach file");
    }

    @Override
    public Context createContext() {
        throw new UnsupportedOperationException("not a mach file");
    }

    @Override
    public long getCodeEntryPointMem() {
        throw new UnsupportedOperationException("not a mach file");
    }

    @Override
    public long toFileAddress(long memAddress) {
        throw new UnsupportedOperationException("not a mach file");
    }

    @Override
    public long toMemAddress(long fileOffset) {
        throw new UnsupportedOperationException("not a mach file");
    }

    @Override
    public Map<Long, String> getImports() {
        throw new UnsupportedOperationException("not a mach file");
    }

}
