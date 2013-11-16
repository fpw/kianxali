package kianxali;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import kianxali.cpu.x86.X86Decoder;
import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.Model;
import kianxali.cpu.x86.xml.OperandDesc;
import kianxali.cpu.x86.xml.OpcodeSyntax;
import kianxali.decoder.DecodedEntity;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.DisassemblyListener;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public final class Test implements DisassemblyListener {
    private static final Logger LOG = Logger.getLogger("kianxali");
    private DisassemblyData data;
    private Disassembler dasm;
    private long timeBegin, timeEnd;
    private X86Decoder decoder;
    private OutputFormatter formatter;

    private Test() {
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINEST);
        consoleHandler.setFormatter(new LogFormatter());
        LOG.setUseParentHandlers(false);
        LOG.addHandler(consoleHandler);
        LOG.setLevel(Level.FINEST);

        try {
            decoder = X86Decoder.fromXML(Model.ANY, ExecutionMode.LONG, "./xml/x86/x86reference.xml", "./xml/x86/x86reference.dtd");
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private void checkSyntaxes() {
        for(OpcodeSyntax syntax : decoder.getAllSyntaxes()) {
            for(OperandDesc op : syntax.getOperands()) {
                if(!op.indirect && op.operType == null) {
                    LOG.finest("Missing operType in " + syntax);
                    continue; // only report each syntax once
                }
            }
        }
    }

    private void testDisassembler() throws IOException {
        ImageFile image = new PEFile(new File("./targets/client.exe"));
        data = new DisassemblyData();
        dasm = new Disassembler(image, data);
        formatter = new OutputFormatter();
        dasm.addListener(this);
        dasm.startAnalyzer();
    }

    @Override
    public void onAnalyzeStart() {
        timeBegin = System.currentTimeMillis();
    }

    @Override
    public void onAnalyzeEntity(DecodedEntity entity) {
        System.out.println(String.format("%08X: %s", entity.getMemAddress(), entity.asString(formatter)));
    }

    @Override
    public void onAnalyzeError(long memAddr) {
        System.err.println(String.format("%08X: Error", memAddr));
    }

    @Override
    public void onAnalyzeStop() {
        timeEnd = System.currentTimeMillis();
        double dur = (timeEnd - timeBegin) / 1000.0;
        System.out.println(String.format("Done, took %.2f seconds and got %d entities", dur, data.getEntityCount()));
    }

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        test.checkSyntaxes();
        test.testDisassembler();
    }
}
