package kianxali.loader.elf;

import kianxali.loader.Section;

public class ELFSection implements Section {
    private final String name;
    private final long offset, start, end;
    private final boolean executable;

    public ELFSection(String name, long offset, long start, long end, boolean executable) {
        this.name = name;
        this.offset = offset;
        this.start = start;
        this.end = end;
        this.executable = executable;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isExecutable() {
        return executable;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public long getStartAddress() {
        return start;
    }

    @Override
    public long getEndAddress() {
        return end;
    }
}
