package kianxali.gui;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.gui.model.imagefile.ImageDocument;
import kianxali.gui.model.imagefile.ImageDocumentReader;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;

public class Controller {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private final ImageDocument document;
    private ImageFile currentImage;
    private KianxaliGUI gui;

    public Controller() {
        this.document = new ImageDocument();
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
    }

    private void prepareDocument() {
    }

    private void readImage() {
        ImageDocumentReader reader = new ImageDocumentReader(document);
        reader.read(currentImage);
    }
}
