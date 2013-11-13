package kianxali.image;

import java.util.List;

import kianxali.decoder.Context;

public interface ImageFile {
    List<Section> getSections();
    Section getSectionForMemAddress(long memAddress);
    Context createContext();
    long memToFileAddress(long memAddress);
    long fileToMemAddress(long fileOffset);
    long getCodeEntryPointMem();
    ByteSequence getByteSequence(long memAddress);
}
