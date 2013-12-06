package kianxali.gui;

import java.nio.file.Path;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import kianxali.decoder.Data;
import kianxali.decoder.Instruction;
import kianxali.disassembler.DataEntry;
import kianxali.disassembler.DataListener;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.DisassemblyListener;
import kianxali.gui.model.imagefile.ImageDocument;
import kianxali.gui.model.imagefile.StatusView;
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
        formatter.setIncludeRawBytes(true);
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
            imageFile = new PEFile(path);

            imageDoc = new ImageDocument(formatter);
            gui.getImageView().getStatusView().initNewData(imageFile.getFileSize());

            disassemblyData = new DisassemblyData();
            disassemblyData.addListener(this);

            disassembler = new Disassembler(imageFile, disassemblyData);
            disassembler.addListener(this);

            disassembler.startAnalyzer();
        } catch (Exception e) {
            // TODO: add more file types and decide somehow
            LOG.warning("Couldn't load image: " + e.getMessage());
            e.printStackTrace();
            gui.showError("Couldn't load file", e.getMessage() + "\nCurrently, only PE files (.exe) are supported.");
        }
    }

    public void onScrollChange(long memAddr) {
        if(imageFile.isValidAddress(memAddr)) {
            long offset = imageFile.toFileAddress(memAddr);
            gui.getImageView().getStatusView().setCursorAddress(offset);
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
        if(entry != null && entry.getEntity() != null) {
            StatusView sv = gui.getImageView().getStatusView();
            long offset = imageFile.toFileAddress(memAddr);
            if(entry.getEntity() instanceof Instruction) {
                sv.onDiscoverCode(offset, entry.getEntity().getSize());
            } else if(entry.getEntity() instanceof Data) {
                sv.onDiscoverData(offset, entry.getEntity().getSize());
            }
        }
    }

    @Override
    public void onAnalyzeError(long memAddr) {
    }

    @Override
    public void onAnalyzeStop() {
        double duration = (System.currentTimeMillis() - beginDisassembleTime) / 1000.0;
        LOG.info(String.format(
                    "Initial auto-analysis finished after %.2f seconds, got %d entities",
                    duration, disassemblyData.getEntityCount())
                );
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getImageView().setDocument(imageDoc);
                try {
                    gui.getImageView().scrollTo(imageFile.getCodeEntryPointMem());
                } catch (BadLocationException e) {
                    // scrolling didn't work, but this is not severe
                    LOG.warning("Couldn't scroll to code entry point");
                }
            }
        });
    }
}
