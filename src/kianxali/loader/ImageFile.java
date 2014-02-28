package kianxali.loader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import kianxali.decoder.Context;

/**
 * An image file represents the main data structure that describes the
 * memory layout of an executable file.
 * @author fwi
 *
 */
public abstract class ImageFile {
    protected final ByteSequence imageFile;
    protected final long fileSize;
    protected final String fileName;

    protected ImageFile(Path path) throws IOException {
        this.imageFile = ByteSequence.fromFile(path);
        this.fileSize = imageFile.getRemaining();
        this.fileName = path.getFileName().toString();
    }

    /**
     * Returns the file size of the image file
     * @return the file size of the image file
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Returns all sections inside the file
     * @return a list of memory sections in the file
     */
    public abstract List<Section> getSections();

    /**
     * Creates a CPU context for the target CPU described in the file
     * @return the context matching the expectations of the image file
     */
    public abstract Context createContext();

    /**
     * Returns the virtual memory address of the code entry point
     * @return the memory address of the code entry point
     */
    public abstract long getCodeEntryPointMem();

    /**
     * Converts a virtual memory address into a file offset
     * @param memAddress the memory address to convert
     * @return the offset for that address in the file
     */
    public abstract long toFileAddress(long memAddress);

    /**
     * Converts a file offset to a virtual memory address
     * @param fileOffset the offset inside the file
     * @return the virtual memory address for the offset
     */
    public abstract long toMemAddress(long fileOffset);

    /**
     * Retrieve a map of imported functions
     * @return a map from memory address to imported function name
     */
    public abstract Map<Long, String> getImports();

    /**
     * Returns the file name of the image file
     * @return the file name of the image file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns a byte sequence for a given virtual memory location
     * @param memAddress the memory address where the byte sequence should point at
     * @param locked whether the sequence should be locked for exclusive access
     * @return a byte sequence pointing at the given memory address
     */
    public ByteSequence getByteSequence(long memAddress, boolean locked) {
        if(locked) {
            imageFile.lock();
        }
        try {
            imageFile.seek(toFileAddress(memAddress));
        } catch(Exception e) {
            imageFile.unlock();
            throw e;
        }
        return imageFile;
    }

    /**
     * Returns the section that covers a given memory address
     * @param memAddress the memory address to examine
     * @return the section that covers the memory address or null
     */
    public Section getSectionForMemAddress(long memAddress) {
        for(Section sec : getSections()) {
            if(memAddress >= sec.getStartAddress() && memAddress <= sec.getEndAddress()) {
                return sec;
            }
        }
        return null;
    }

    /**
     * Checks whether a given virtual memory address is valid in the application's memory layout
     * @param memAddress the memory address to examine
     * @return true iff there is a virtual memory section that covers the address
     */
    public boolean isValidAddress(long memAddress) {
        return getSectionForMemAddress(memAddress) != null;
    }

    /**
     * Checks whether a given virtual memory address could contain executable code
     * @param memAddress the memory address to examine
     * @return true iff the address is valid and its associated section is marked as executable
     */
    public boolean isCodeAddress(long memAddress) {
        Section sect = getSectionForMemAddress(memAddress);
        return sect != null && sect.isExecutable();
    }
}
