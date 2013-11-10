package org.solhost.folko.dasm;

import org.solhost.folko.dasm.cpu.x86.Decoder;
import org.solhost.folko.dasm.decoder.DecodeListener;
import org.solhost.folko.dasm.decoder.DecodeTree;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.pe.PEFile;
import org.solhost.folko.dasm.xml.OpcodeSyntax;
import org.solhost.folko.dasm.xml.XMLParser;

public class Disassembler {
    public static void main(String[] args) throws Exception {
        XMLParser parser = new XMLParser();
        DecodeTree<OpcodeSyntax> decodeTree = new DecodeTree<>();

        System.out.print("Loading opcodes from XML... ");
        parser.loadXML("x86reference.xml", "x86reference.dtd");
        System.out.println("done");

        System.out.print("Building decode tree...");
        for(final OpcodeSyntax entry : parser.getSyntaxEntries()) {
            short[] prefix = entry.getPrefix();
            if(entry.hasEncodedRegister()) {
                int regIndex = entry.getEncodedRegisterPrefixIndex();
                for(int i = 0; i < 8; i++) {
                    decodeTree.addEntry(prefix, entry);
                    prefix[regIndex]++;
                }
            } else {
                decodeTree.addEntry(prefix, entry);
            }
        }
        System.out.println("done");

        Decoder decoder = new Decoder(decodeTree);
        PEFile image = new PEFile("client.exe");
        image.load();

        decoder.decode(image.getEntryPoint(), new DecodeListener() {
            @Override
            public void onDecode(long offset, int length, DecodedEntity entity) {
                System.out.println(String.format("%08X: %s", offset, entity));
            }
        });
    }
}
