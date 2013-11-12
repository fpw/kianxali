package org.solhost.folko.dasm;

import java.util.List;

public interface ImageFile extends AliasResolver {
    public List<Section> getSections();
    public Section getSectionForMemAddress(long memAddress);
    public Context createContext();
    public long memToFileAddress(long memAddress);
    public long fileToMemAddress(long fileOffset);
    public long getCodeEntryPointMem();
    public ByteSequence getByteSequence(long memAddress);
}
