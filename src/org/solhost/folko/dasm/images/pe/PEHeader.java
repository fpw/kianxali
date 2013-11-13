package org.solhost.folko.dasm.images.pe;

import java.sql.Date;

import org.solhost.folko.dasm.images.ByteSequence;

public class PEHeader {
    public static final long PE_SIGNATURE = 0x4550;
    public enum Machine { I386 };

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
        case 0x14C: machine = Machine.I386; break;
        default:
            throw new RuntimeException("unknown machine in PE header");
        }

        numSections = image.readUWord();

        long timeStampEpoch = image.readUDword();
        timeStamp = new Date(timeStampEpoch * 1000);

        // skip unused information
        image.skip(12);
    }

    public Machine getMachine() {
        return machine;
    }

    public int getNumSections() {
        return numSections;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }
}
