package org.solhost.folko.dasm;

import org.solhost.folko.dasm.pe.PEFile;

public class Disassembler {
    public static void main(String[] args) throws Exception {
        PEFile image = new PEFile("client.exe");
        image.disassemble();
    }
}
