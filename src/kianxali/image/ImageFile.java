package kianxali.image;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import kianxali.decoder.Context;

public abstract class ImageFile {
    protected final ByteSequence imageFile;
    protected final long fileSize;
    protected final String fileName;

    public ImageFile(Path path) throws IOException {
        this.imageFile = ByteSequence.fromFile(path);
        this.fileSize = imageFile.getRemaining();
        this.fileName = path.getFileName().toString();
    }

    public abstract List<Section> getSections();
    public abstract Context createContext();
    public abstract long getCodeEntryPointMem();
    public abstract long toFileAddress(long memAddress);
    public abstract long toMemAddress(long fileOffset);
    public abstract Map<Long, String> getImports();

    public String getFileName() {
        return fileName;
    }

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

    public Section getSectionForMemAddress(long memAddress) {
        for(Section sec : getSections()) {
            if(memAddress >= sec.getStartAddress() && memAddress <= sec.getEndAddress()) {
                return sec;
            }
        }
        return null;
    }

    public boolean isValidAddress(long memAddress) {
        return getSectionForMemAddress(memAddress) != null;
    }

    public boolean isCodeAddress(long memAddress) {
        Section sect = getSectionForMemAddress(memAddress);
        return sect != null && sect.isExecutable();
    }

    public long getFileSize() {
        return fileSize;
    }
}
