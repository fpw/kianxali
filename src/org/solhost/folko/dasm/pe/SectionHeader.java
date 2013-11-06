package org.solhost.folko.dasm.pe;

import org.solhost.folko.dasm.ByteSequence;

public class SectionHeader {
    private final String name;
    private final long virtualAddressRVA, rawSize, filePosition, characteristics;

    public SectionHeader(ByteSequence image) {
        name = image.readString(8);

        // ignore unreliable size
        image.readUDword();

        virtualAddressRVA = image.readUDword();
        rawSize = image.readUDword();
        filePosition = image.readUDword();

        // ignore object reloactions
        image.readUDword();
        image.readUDword();
        image.readUWord();
        image.readUWord();

        characteristics = image.readUDword();
    }

    public String getName() {
        return name;
    }

    public long getVirtualAddressRVA() {
        return virtualAddressRVA;
    }

    public long getRawSize() {
        return rawSize;
    }

    public long getFilePosition() {
        return filePosition;
    }

    public boolean isCode() {
        return (characteristics & 0x20) != 0;
    }

    public boolean isInitializedData() {
        return (characteristics & 0x40) != 0;
    }

    public boolean isUninitializedData() {
        return (characteristics & 0x80) != 0;
    }
}
