package org.solhost.folko.dasm.images;

import java.util.List;

import org.solhost.folko.dasm.decoder.Context;

public interface ImageFile {
    List<Section> getSections();
    Section getSectionForMemAddress(long memAddress);
    Context createContext();
    long memToFileAddress(long memAddress);
    long fileToMemAddress(long fileOffset);
    long getCodeEntryPointMem();
    ByteSequence getByteSequence(long memAddress);
}
