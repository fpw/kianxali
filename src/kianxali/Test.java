package kianxali;

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

public final class Test {
    private static final Logger LOG = Logger.getLogger("kianxali");
    private X86Decoder decoder;

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

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        test.checkSyntaxes();
    }
}
