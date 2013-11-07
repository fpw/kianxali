package org.solhost.folko.dasm;

import org.solhost.folko.dasm.xml.XMLParser;

public class Disassembler {
    public static void main(String[] args) throws Exception {
        System.out.print("Loading opcodes from XML... ");
        XMLParser.init("x86reference.xml", "x86reference.dtd");
        System.out.println("done");

        // PEFile image = new PEFile("client.exe");
        // image.disassemble();
    }
}
