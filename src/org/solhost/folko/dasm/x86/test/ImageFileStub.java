package org.solhost.folko.dasm.x86.test;

import java.util.ArrayList;
import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.Context;
import org.solhost.folko.dasm.ImageFile;
import org.solhost.folko.dasm.Section;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;
import org.solhost.folko.dasm.cpu.x86.X86Context;

public class ImageFileStub implements ImageFile {
    @Override
    public String getAddressAlias(long memAddress) {
        return null;
    }

    @Override
    public List<Section> getSections() {
        return new ArrayList<>(0);
    }

    @Override
    public Section getSectionForMemAddress(long memAddress) {
        return null;
    }

    @Override
    public Context createContext() {
        return new X86Context(this, Model.ANY, ExecutionMode.PROTECTED);
    }

    @Override
    public long memToFileAddress(long memAddress) {
        return memAddress;
    }

    @Override
    public long fileToMemAddress(long fileOffset) {
        return fileOffset;
    }

    @Override
    public long getCodeEntryPointMem() {
        return 0;
    }

    @Override
    public ByteSequence getByteSequence(long memAddress) {
        return null;
    }
}
