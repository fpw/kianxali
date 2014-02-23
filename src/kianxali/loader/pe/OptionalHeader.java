package kianxali.loader.pe;

import kianxali.loader.ByteSequence;

public class OptionalHeader {
    public static final int HEADER_MAGIC_PE32 = 0x010B;
    public static final int HEADER_MAGIC_PE32PLUS = 0x020B; // means some fields are 64 bit, but doesn't imply 64 bit code
    public enum SubSystem { DLL, CONSOLE, GUI };
    public static final int DATA_DIRECTORY_EXPORT = 0;
    public static final int DATA_DIRECTORY_IMPORT = 1;
    public static final int DATA_DIRECTORY_RESOURCES = 2;
    public static final int DATA_DIRECTORY_RELOC = 5;
    public static final int DATA_DIRECTORY_BOUND_IMPORT = 11;
    public static final int DATA_DIRECTORY_IAT = 12;

    private final long entryPointRVA, imageBase;
    private final long sectionAlignment, fileAlignment;
    private final long requiredMemory;
    private SubSystem subSystem;
    private final DataDirectory[] dataDirectories;
    private boolean pe32plus;

    private class DataDirectory {
        long offset, size;
    }

    public OptionalHeader(ByteSequence image) {
        int magic = image.readUWord();
        if(magic == HEADER_MAGIC_PE32) {
            pe32plus = false;
        } else if(magic == HEADER_MAGIC_PE32PLUS) {
            pe32plus = true;
        } else {
            throw new RuntimeException("invalid optional header magic: " + magic);
        }

        // ignore linker version
        image.readUWord();

        // ignore unreliable section sizes
        image.readUDword();
        image.readUDword();
        image.readUDword();

        entryPointRVA = image.readUDword();

        // ignore uninteresting offsets
        image.readUDword();

        if(pe32plus) {
            imageBase = image.readSQword();
        } else {
            image.readUDword();
            imageBase = image.readUDword();
        }
        sectionAlignment = image.readUDword();
        fileAlignment = image.readUDword();

        // ignore expected OS version
        image.readUWord();
        image.readUWord();

        // ignore binary version
        image.readUWord();
        image.readUWord();

        // ignore subsystem version
        image.readUWord();
        image.readUWord();

        // ignore win32 version
        image.readUDword();

        requiredMemory = image.readUDword();

        // ignore size of headers
        image.readUDword();

        // ignore checksum
        image.readUDword();

        // ignore subsystem
        int subSys = image.readUWord();
        switch (subSys) {
        case 0: subSystem = SubSystem.DLL; break;
        case 2: subSystem = SubSystem.GUI; break;
        case 3: subSystem = SubSystem.CONSOLE; break;
        default:
            throw new RuntimeException("invalid subsystem in optional header: " + subSys);
        }

        // ignore unused DLL stuff
        image.readUWord();

        // ignore stack sizes
        if(pe32plus) {
            image.readSQword();
            image.readSQword();
            image.readSQword();
            image.readSQword();
        } else {
            image.readUDword();
            image.readUDword();
            image.readUDword();
            image.readUDword();
        }

        // ignore loader flags
        image.readUDword();

        int numberOfRVAs = (int) Math.min(16, image.readUDword());
        dataDirectories = new DataDirectory[numberOfRVAs];
        for(int i = 0; i < numberOfRVAs; i++) {
            dataDirectories[i] = new DataDirectory();
            dataDirectories[i].offset = image.readUDword();
            dataDirectories[i].size = image.readUDword();
        }
    }

    public long getEntryPointRVA() {
        return entryPointRVA;
    }

    public long getImageBase() {
        return imageBase;
    }

    public long getFileAlignment() {
        return fileAlignment;
    }

    public long getSectionAlignment() {
        return sectionAlignment;
    }

    public SubSystem getSubSystem() {
        return subSystem;
    }

    public long getRequiredMemory() {
        return requiredMemory;
    }

    public long getDataDirectoryOffsetRVA(int index) {
        return dataDirectories[index].offset;
    }

    public long getDataDirectorySize(int index) {
        return dataDirectories[index].size;
    }
}
