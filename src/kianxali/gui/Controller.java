package kianxali.gui;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
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
import kianxali.image.ByteSequence;
import kianxali.image.ImageFile;
import kianxali.image.mach_o.MachOFile;
import kianxali.image.pe.PEFile;
import kianxali.scripting.ScriptManager;
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
    private final ScriptManager scripts;
    private boolean initialAnalyzeDone;

    public Controller() {
        this.formatter = new OutputFormatter();
        this.functionList = new FunctionList();
        this.stringList = new StringList();
        this.scripts = new ScriptManager(this);
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
        Logger.getLogger("kianxali").addHandler(gui.getLogView().getLogHandler());
    }

    public void onOpenFileRequest() {
        gui.showFileOpenDialog();
    }

    public void onSavePatchedRequest() {
        if(imageFile != null) {
            gui.showSavePatchedDialog();
        } else {
            gui.showError("Nothing to patch", "No image loaded");
        }
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
            initialAnalyzeDone = false;
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

        if(initialAnalyzeDone) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getImageView().setDocument(imageDoc);
                gui.getFunctionListView().setModel(functionList);
                gui.getStringListView().setModel(stringList);
            }
        });
        initialAnalyzeDone = true;
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

    public void onDisassemblyLeftClick(int index) {
        if(index < 0 || imageDoc == null) {
            return;
        }

        Element elem = imageDoc.getCharacterElement(index);
        if(elem == null) {
            return;
        }

        // for now, only handle left clicks on references
        if(elem.getName() != ImageDocument.ReferenceElementName) {
            return;
        }
        Object ref = elem.getAttributes().getAttribute(ImageDocument.RefAddressKey);
        if(ref instanceof Long) {
            long refAddr = (Long) ref;
            try {
                gui.getImageView().scrollTo(refAddr);
            } catch (BadLocationException e) {
                LOG.warning("Invalid scroll location when left clicking reference");
            }
        }
    }

    public void onConvertToNOP(int index) {
        Long addr = imageDoc.getAddressForOffset(index);
        if(addr == null) {
            return;
        }
        DecodedEntity entity = disassemblyData.getEntityOnExactAddress(addr);
        if(!(entity instanceof Instruction)) {
            return;
        }
        Instruction inst = (Instruction) entity;
        ByteSequence seq = imageFile.getByteSequence(addr, true);
        for(int i = 0; i < inst.getSize(); i++) {
            // TODO: 0x90 is x86 only
            seq.patch(imageFile.toFileAddress(addr + i), (byte) 0x90);
        }
        seq.unlock();
        disassembler.reanalyze(inst.getMemAddress());
    }

    public void onPatchedSave(Path path) {
        ByteSequence seq = imageFile.getByteSequence(imageFile.getCodeEntryPointMem(), true);
        try {
            seq.savePatched(path);
        } catch (IOException e) {
            gui.showError("Couldn't save file", e.getMessage());
        } finally {
            seq.unlock();
        }
    }

    public void onRunScriptRequest() {
        String script = gui.getScriptView().getScript();
        scripts.runScript(script);
    }

    public Writer getLogWindowWriter() {
        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                String msg = new String(cbuf, off, len);
                gui.getLogView().addLine(msg);
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    public void showError(String msg) {
        gui.showError("Error", msg);
    }

    public DisassemblyData getDisassemblyData() {
        return disassemblyData;
    }
}
