package org.solhost.folko.dasm.images.pe;

import org.solhost.folko.dasm.images.ByteSequence;
import org.solhost.folko.dasm.images.Section;

public class PESection implements Section {
    private final String name;
    private final AddressConverter addrConv;
    private final long virtualAddressRVA, rawSize, filePosition, characteristics;

    public PESection(ByteSequence image, AddressConverter conv) {
        this.addrConv = conv;
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

    @Override
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

    @Override
    public long getStartAddress() {
        return addrConv.rvaToMemory(virtualAddressRVA);
    }

    @Override
    public long getEndAddress() {
        return addrConv.rvaToMemory(virtualAddressRVA + rawSize);
    }
}
