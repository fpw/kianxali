package org.solhost.folko.dasm.images;

import java.util.List;

import org.solhost.folko.dasm.decoder.Context;

public interface ImageFile {
    public List<Section> getSections();
    public Section getSectionForMemAddress(long memAddress);
    public Context createContext();
    public long memToFileAddress(long memAddress);
    public long fileToMemAddress(long fileOffset);
    public long getCodeEntryPointMem();
    public ByteSequence getByteSequence(long memAddress);
}
