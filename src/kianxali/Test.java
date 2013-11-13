package kianxali;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.disassembler.Disassembler;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public final class Test {
    // Utility class, no constructor
    private Test() {

    }

    public static void main(String[] args) throws Exception {
        PEFile image = new PEFile("targets/swap.exe");

        Disassembler dasm = new Disassembler();
        dasm.disassemble(image);

        OutputFormatter format = new OutputFormatter();
        for(DecodedEntity entity : dasm.getEntities().values()) {
            try {
                if(entity instanceof Instruction) {
                    System.out.println(String.format("%08X: %s", entity.getMemAddress(), entity.asString(format)));
                }
            } catch(Exception e) {
                System.out.println(String.format("%08X: ERROR %s", entity.getMemAddress(), entity.toString()));
                e.printStackTrace();
                break;
            }
        }
    }
}
