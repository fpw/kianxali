package org.solhost.folko.dasm.cpu.x86;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.xml.OpcodeEntry;
import org.solhost.folko.dasm.xml.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeSyntax;
import org.solhost.folko.dasm.xml.OpcodeOperand;

public class Instruction implements DecodedEntity {
    private final OpcodeSyntax syntax;

    public Instruction(OpcodeSyntax syntax) {
        this.syntax = syntax;
    }

    public boolean isPrefix() {
        return syntax.getOpcodeEntry().belongsTo(OpcodeGroup.PREFIX);
    }

    public OpcodeSyntax getSyntax() {
        return syntax;
    }

    public OpcodeEntry getOpcode() {
        return syntax.getOpcodeEntry();
    }

    // the prefix has been read from seq already
    public void decode(ByteSequence seq, Context ctx) {
        OpcodeEntry entry = syntax.getOpcodeEntry();
        if(entry.modRM) {
            // TODO: parse mod r/m
            short modRM = seq.readUByte();
        }
        for(OpcodeOperand op : syntax.getOperands()) {

        }
    }

    @Override
    public String toString() {
        return syntax.toString();
    }
}
