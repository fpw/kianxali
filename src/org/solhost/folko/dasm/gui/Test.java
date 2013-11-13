package org.solhost.folko.dasm.gui;

import org.solhost.folko.dasm.Disassembler;
import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.images.pe.PEFile;

/*
 * TODO:
 *  - create fuzzer by iterating syntaxes and operands
 *  - sib64
 *  - verify SEGMENT2 encoding
 * Document:
 *  - trie / tree approach to decoding with principle of longest match
 *
 */

public final class Test {
    // Utility class, no constructor
    private Test() {

    }

    public static void main(String[] args) throws Exception {
        PEFile image = new PEFile("targets/putty.exe");
        image.load();

        Disassembler dasm = new Disassembler(image);
        dasm.disassemble();

        OutputFormatter format = new OutputFormatter();
        for(DecodedEntity entity : dasm.getEntities().values()) {
            try {
                System.out.println(String.format("%08X: %s", entity.getMemAddress(), entity.asString(format)));
            } catch(Exception e) {
                System.out.println(String.format("%08X: ERROR %s", entity.getMemAddress(), entity.toString()));
                e.printStackTrace();
                break;
            }
        }
    }
}
