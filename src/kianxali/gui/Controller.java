package kianxali.gui;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.disassembler.DataEntry;
import kianxali.disassembler.DataListener;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.DisassemblyListener;
import kianxali.gui.model.imagefile.ImageDocument;
import kianxali.image.ImageFile;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public class Controller implements DisassemblyListener, DataListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private ImageDocument imageDoc;
    private Disassembler disassembler;
    private DisassemblyData disassemblyData;
    private long beginDisassembleTime;
    private ImageFile imageFile;
    private final OutputFormatter formatter;
    private KianxaliGUI gui;

    public Controller() {
        this.formatter = new OutputFormatter();
    }

    public void showGUI() {
        gui = new KianxaliGUI(this);
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
    }

    public void onOpenFileRequest() {
        gui.showFileOpenDialog();
    }

    public void onFileOpened(Path path) {
        try {
            // TODO: add more file types and decide somehow
            imageFile = new PEFile(path);

            imageDoc = new ImageDocument(formatter);
            gui.getImageView().getStatusView().initNewData(imageFile.getFileSize());

            disassemblyData = new DisassemblyData();
            disassemblyData.addListener(this);

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
        beginDisassembleTime = System.currentTimeMillis();
    }

    @Override
    public void onAnalyzeChange(long memAddr) {
        DataEntry entry = disassemblyData.getInfoOnExactAddress(memAddr);
        imageDoc.updateDataEntry(memAddr, entry);
//        StatusView sv = gui.getImageView().getStatusView();
//        long offset = imageFile.toFileAddress(entity.getMemAddress());
//        if(entity instanceof Instruction) {
//            sv.onDiscoverCode(offset, entity.getSize());
//        } else if(entity instanceof Data) {
//            sv.onDiscoverData(offset, entity.getSize());
//        }
    }

    @Override
    public void onAnalyzeError(long memAddr) {
    }

    @Override
    public void onAnalyzeStop() {
        double duration = (System.currentTimeMillis() - beginDisassembleTime) / 1000.0;
        LOG.info(String.format("Initial auto-analysis finished after %.2f seconds, got %d entities",
                duration, disassemblyData.getEntityCount()));
        gui.getImageView().setDocument(imageDoc);
    }
}
