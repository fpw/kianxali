package kianxali.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblingListener;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;

public class Controller implements DisassemblingListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private final Disassembler disassembler;
    private Thread dasmThread;
    private ImageFile currentImage;
    private KianxaliGUI gui;

    public Controller() {
        disassembler = new Disassembler();
        disassembler.addDisassemblingListener(this);
    }

    public void showGUI() {
        gui = new KianxaliGUI(this);
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
    }

    public void onOpenFileRequest() {
        gui.showFileOpenDialog();
    }

    public void onFileOpened(File file) {
        try {
            // TODO: add more file types and decide somehow
            currentImage = new PEFile(file);
            startDisassembling();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Couldn't load image: " + e.getMessage(), e);
            gui.showError("Couldn't load file", e.getMessage());
        }
    }

    private void startDisassembling() {
        if(dasmThread != null) {
            dasmThread.interrupt();
        }
        dasmThread = new Thread() {
            @Override
            public void run() {
                try {
                    disassembler.disassemble(currentImage);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Disassembly error: " + e.getMessage(), e);
                }
            };
        };
        dasmThread.start();
    }

    @Override
    public void onDisassemblyStart() {
    }

    @Override
    public void onDisassembledAddress(long memAddr) {
    }

    @Override
    public void onDisassemblyFinish() {
    }
}
