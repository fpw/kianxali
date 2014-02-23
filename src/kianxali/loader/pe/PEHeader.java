package kianxali.loader.pe;

import java.sql.Date;

import kianxali.loader.ByteSequence;

public class PEHeader {
    public static final long PE_SIGNATURE = 0x4550;
    public enum Machine { X86_32, X86_64 };

    private Machine machine;
    private final int numSections;
    private final Date timeStamp;

    public PEHeader(ByteSequence image) {
        long signature = image.readUDword();
        if(signature != PE_SIGNATURE) {
            throw new RuntimeException("Invalid PE signature");
        }

        int machineCode = image.readUWord();
        switch(machineCode) {
        case 0x14C:     machine = Machine.X86_32; break;
        case 0x8664:    machine = Machine.X86_64; break;
        default:
            throw new RuntimeException("unknown machine in PE header: " + machineCode);
        }

        numSections = image.readUWord();

        long timeStampEpoch = image.readUDword();
        timeStamp = new Date(timeStampEpoch * 1000);

        // skip unused information
        image.skip(12);
    }

    public boolean is64BitCode() {
        return machine == Machine.X86_64;
    }

    public int getNumSections() {
        return numSections;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
}
