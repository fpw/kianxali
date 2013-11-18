package kianxali.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.DecodedEntity;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.DisassemblyListener;
import kianxali.gui.model.imagefile.ImageDocument;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public class Controller implements DisassemblyListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private final ImageDocument document;
    private Disassembler disassembler;
    private DisassemblyData disassemblyData;
    private ImageFile imageFile;
    private final OutputFormatter formatter;
    private KianxaliGUI gui;

    public Controller() {
        this.document = new ImageDocument();
        this.formatter = new OutputFormatter();
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
            imageFile = new PEFile(file);

            disassemblyData = new DisassemblyData();
            disassembler = new Disassembler(imageFile, disassemblyData);
            disassembler.addListener(this);
            disassembler.startAnalyzer();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Couldn't load image: " + e.getMessage(), e);
            gui.showError("Couldn't load file", e.getMessage());
        }
    }

    @Override
    public void onAnalyzeStart() {
    }

    @Override
    public void onAnalyzeEntity(final DecodedEntity entity) {
        document.setOffsetLines(entity.getMemAddress(), new String[] {entity.asString(formatter)});
    }

    @Override
    public void onAnalyzeError(long memAddr) {
    }

    @Override
    public void onAnalyzeStop() {
    }
}
