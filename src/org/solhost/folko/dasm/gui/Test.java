package org.solhost.folko.dasm.gui;

import org.solhost.folko.dasm.Disassembler;
import org.solhost.folko.dasm.DisassemblingListener;
import org.solhost.folko.dasm.OutputFormatter;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.images.pe.PEFile;

/*
 * TODO:
 *  - create fuzzer by iterating syntaxes and operands
 *  - string prefixes, wait prefix, lock prefix
 *  - sib64
 *  - verify SEGMENT2 encoding
 */

public class Test {
    public static void main(String[] args) throws Exception {
        PEFile image = new PEFile("targets/test.exe");
        image.load();

        final OutputFormatter format = new OutputFormatter();
        Disassembler dasm = new Disassembler(image);
        dasm.addDisassemblingListener(new DisassemblingListener() {
            @Override
            public void onInstructionDecode(Instruction inst) {
                System.out.println(String.format("%08X: %s", inst.getMemAddress(), inst.asString(format)));
            }
        });
        dasm.disassemble();
    }
}
