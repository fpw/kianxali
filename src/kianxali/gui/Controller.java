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
import kianxali.disassembler.Function;
import kianxali.gui.models.FunctionList;
import kianxali.gui.models.ImageDocument;
import kianxali.gui.models.StringList;
import kianxali.image.ImageFile;
import kianxali.image.mach_o.MachOFile;
import kianxali.image.pe.PEFile;
import kianxali.util.OutputFormatter;

public class Controller implements DisassemblyListener, DataListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private ImageDocument imageDoc;
    private Disassembler disassembler;
    private DisassemblyData disassemblyData;
    private long beginDisassembleTime;
    private ImageFile imageFile;
    private final FunctionList functionList;
    private final StringList stringList;
    private final OutputFormatter formatter;
    private KianxaliGUI gui;

    public Controller() {
        this.formatter = new OutputFormatter();
        this.functionList = new FunctionList();
        this.stringList = new StringList();
        formatter.setIncludeRawBytes(true);
    }

    public FunctionList getFunctionList() {
        return functionList;
    }

    public StringList getStringList() {
        return stringList;
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
            try {
                imageFile = new PEFile(path);
                LOG.fine("Loaded as PE file");
            } catch(Exception e) {
                LOG.fine("Not a PE file: " + e.getMessage());
            }

            if(imageFile == null) {
                try {
                    imageFile = new MachOFile(path);
                    LOG.fine("Loaded as Mach-O file");
                } catch(Exception e) {
                    LOG.fine("Not a Mach-O file: " + e.getMessage());
                }
            }

            if(imageFile == null) {
                throw new UnsupportedOperationException("Unknown file type.");
            }
            functionList.clear();
            stringList.clear();

            imageDoc = new ImageDocument(formatter);
            gui.getImageView().getStatusView().initNewData(imageFile.getFileSize());

            disassemblyData = new DisassemblyData();
            disassemblyData.addListener(this);
            disassemblyData.addListener(functionList);
            disassemblyData.addListener(stringList);

            disassembler = new Disassembler(imageFile, disassemblyData);
            disassembler.addListener(this);
            formatter.setAddressNameResolve(disassembler);

            disassembler.startAnalyzer();
        } catch (Exception e) {
            // TODO: add more file types and decide somehow
            LOG.warning("Couldn't load image: " + e.getMessage());
            e.printStackTrace();
            gui.showError("Couldn't load file", e.getMessage() + "\nCurrently, only PE (.exe) and Mach-O files are supported.");
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
    public void onAnalyzeChange(long memAddr, DataEntry entry) {
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
                gui.getFunctionListView().setModel(functionList);
                gui.getStringListView().setModel(stringList);
                try {
                    gui.getImageView().scrollTo(imageFile.getCodeEntryPointMem());
                } catch (BadLocationException e) {
                    // scrolling didn't work, but this is not severe
                    LOG.warning("Couldn't scroll to code entry point");
                }
            }
        });
    }

    public void onFunctionDoubleClick(Function fun) {
        try {
            gui.getImageView().scrollTo(fun.getStartAddress());
        } catch (BadLocationException e) {
            LOG.warning("Invalid scroll location when double clicking function");
        }
    }

    public void onStringDoubleClicked(Data data) {
        try {
            gui.getImageView().scrollTo(data.getMemAddress());
        } catch (BadLocationException e) {
            LOG.warning("Invalid scroll location when double clicking string");
        }
    }
}
