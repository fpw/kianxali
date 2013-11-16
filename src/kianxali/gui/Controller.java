package kianxali.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.DecodedEntity;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblingListener;
import kianxali.gui.model.imagefile.ImageDocument;
import kianxali.gui.model.imagefile.ImageDocumentReader;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public class Controller implements DisassemblingListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private final OutputFormatter formatter;
    private final Disassembler disassembler;
    private final ImageDocument document;
    private Thread dasmThread;
    private ImageFile currentImage;
    private KianxaliGUI gui;

    public Controller() {
        this.disassembler = new Disassembler();
        this.formatter = new OutputFormatter();
        this.document = new ImageDocument();

        disassembler.addDisassemblingListener(this);
        prepareDocument();
    }

    public void showGUI() {
        gui = new KianxaliGUI(this);
        gui.getImageView().setDocument(document);
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
            readImage();
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

    private void prepareDocument() {
    }

    private void readImage() {
        ImageDocumentReader reader = new ImageDocumentReader(document);
        reader.read(currentImage);
    }

    @Override
    public void onDisassemblyStart() {
    }

    @Override
    public void onDisassembledAddress(long memAddr) {
        DecodedEntity entity = disassembler.getEntity(memAddr);
        if(entity != null) {
            LOG.finest(String.format("%08X: %s", memAddr, entity.asString(formatter)));
        }
    }

    @Override
    public void onDisassemblyFinish() {
    }
}
