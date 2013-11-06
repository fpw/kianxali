package org.solhost.folko.dasm.pe;

import org.solhost.folko.dasm.ByteSequence;

public class DOSStub {
    public static final int DOS_MAGIC = 0x5A4D;
    private final long pePointer;

    public DOSStub(ByteSequence image) {
        int magic = image.readUWord();
        if(magic != DOS_MAGIC) {
            throw new RuntimeException("Invalid magic");
        }
        image.skip(58);
        pePointer = image.readUDword();
    }

    public long getPEPointer() {
        return pePointer;
    }
}
