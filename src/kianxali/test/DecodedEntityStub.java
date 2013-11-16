package kianxali.test;

import kianxali.decoder.DecodableEntity;
import kianxali.util.OutputFormatter;

public class DecodedEntityStub implements DecodableEntity {
    private final long addr;
    private final int size;
    private final String name;

    public DecodedEntityStub(long addr, int size, String name) {
        this.addr = addr;
        this.size = size;
        this.name = name;
    }

    @Override
    public long getMemAddress() {
        return addr;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String asString(OutputFormatter format) {
        return "<stub " + name + ">";
    }

    @Override
    public String toString() {
        return name;
    }
}
