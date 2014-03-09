package kianxali.gui;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
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
import kianxali.gui.views.KianxaliGUI;
import kianxali.gui.views.StatusView;
import kianxali.loader.ByteSequence;
import kianxali.loader.ImageFile;
import kianxali.loader.elf.ELFFile;
import kianxali.loader.mach_o.FatFile;
import kianxali.loader.mach_o.MachOFile;
import kianxali.loader.pe.PEFile;
import kianxali.scripting.ScriptManager;
import kianxali.util.OutputFormatter;

/**
 * The controller creates the GUI and receives events from the views to change the models
 * and vice-versa.
 * @author fwi
 *
 */
public class Controller implements DisassemblyListener, DataListener {
    private static final Logger LOG = Logger.getLogger("kianxali.gui.controller");

    private ImageDocument imageDoc;
    private Disassembler disassembler;
    private DisassemblyData disassemblyData;
    private long beginDisassembleTime;
    private ImageFile imageFile;
    private FunctionList functionList;
    private StringList stringList;
    private final OutputFormatter formatter;
    private KianxaliGUI gui;
    private final ScriptManager scripts;
    private boolean initialAnalyzeDone;

    /**
     * Creates a new controller.
     */
    public Controller() {
        this.formatter = new OutputFormatter();
        this.scripts = new ScriptManager(this);
        formatter.setIncludeRawBytes(true);
    }

    /**
     * Creates and shows the GUI for this controller
     */
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

    private void loadPEFile(Path path) {
        LOG.fine("Loading as PE file");
        try {
            imageFile = new PEFile(path);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Invalid PE file: " + e.getMessage(), e);
            showError("Invalid PE file: " + e.getMessage());
        }
    }

    private void loadMachOFile(Path path) {
        LOG.fine("Loading as Mach-O file");
        try {
            imageFile = new MachOFile(path, 0);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Invalid Mach-O file: " + e.getMessage(), e);
            showError("Invalid Mach-O file: " + e.getMessage());
            return;
        }
    }

    private void loadFatFile(Path path) {
        // a fat file contains multiple mach headers for different architectures, let user choose
        LOG.fine("Loading as fat file");
        try {
            FatFile fatFile = new FatFile(path);
            Map<String, Long> archTypes = fatFile.getArchitectures();
            Object[] arches = new Object[archTypes.size()];
            int i = 0;
            for(String arch : archTypes.keySet()) {
                arches[i++] = arch;
            }
            Object arch = JOptionPane.showInputDialog(gui,
                    "Multiple architectures found in fat file.\nWhich one should be analyzed?", "Fat file detected",
                    JOptionPane.PLAIN_MESSAGE, null, arches, arches[0]);
            if(arch == null) {
                return;
            }
            long offset = archTypes.get(arch);
            imageFile = new MachOFile(path, offset);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Invalid Mach-O file: " + e.getMessage(), e);
            showError("Invalid Mach-O file: " + e.getMessage());
            return;
        }
    }

    private void loadELFFile(Path path) {
        LOG.fine("Loading as ELF file");
        try {
            imageFile = new ELFFile(path);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, "Invalid ELF file: " + e.getMessage(), e);
            showError("Invalid ELF file: " + e.getMessage());
            return;
        }
    }

    public void onFileOpened(Path path) {
        try {
            ImageFile old = imageFile;
            if(PEFile.isPEFile(path)) {
                loadPEFile(path);
            } else if(MachOFile.isMachOFile(path)) {
                loadMachOFile(path);
            } else if(FatFile.isFatFile(path)) {
                loadFatFile(path);
            } else if(ELFFile.isELFFile(path)) {
                loadELFFile(path);
            } else {
                showError("<html>Unknown file type.<br>Supported are: <ul><li>PE (Windows .exe)</li><li>ELF (Unix, Linux)</li><li>MachO (OS X)</li><li>Fat MachO (OS X)</li></ul></html>");
            }

            if(imageFile == old) {
                return;
            }

            initialAnalyzeDone = false;

            // reset data structures and GUI if another file was previously loaded
            if(functionList != null) {
                functionList.clear();
            }
            if(stringList != null) {
                stringList.clear();
            }
            gui.getImageView().setDocument(null);
            gui.getImageView().getStatusView().initNewData(imageFile.getFileSize());

            imageDoc = new ImageDocument(formatter);
            functionList = new FunctionList();
            stringList = new StringList();

            disassemblyData = new DisassemblyData();
            disassemblyData.addListener(this);
            disassemblyData.addListener(functionList);
            disassemblyData.addListener(stringList);

            disassembler = new Disassembler(imageFile, disassemblyData);
            disassembler.addListener(this);
            formatter.setAddressNameResolve(disassembler);

            disassembler.startAnalyzer();
        } catch(Exception e) {
            LOG.warning("Couldn't load image: " + e.getMessage());
            e.printStackTrace();
            gui.showError("Couldn't load file", "Error loading file: " + e.getMessage());
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
        LOG.info("Please wait while the file is being analyzed...");
        beginDisassembleTime = System.currentTimeMillis();
    }

    @Override
    public void onAnalyzeChange(final long memAddr, final DataEntry entry) {
        if(gui.getImageView().getDocument() == imageDoc && !SwingUtilities.isEventDispatchThread()) {
            // if the document is visible already, do it in the EDT
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    changeAnalyzeRaw(memAddr, entry);
                }
            });
        } else {
            // not visible or already in EDT: edit directly
            changeAnalyzeRaw(memAddr, entry);
        }
    }

    private void changeAnalyzeRaw(long memAddr, DataEntry entry) {
        // entry can be null if an entry was deleted
        try {
            imageDoc.updateDataEntry(memAddr, entry);
        } catch(Exception e) {
            // this can fail if the error happens when generating the string representation after decoding
            String rawString  = "<no opcode>";
            if(entry.getEntity() instanceof Instruction) {
                Instruction inst = (Instruction) entry.getEntity();
                short[] rawOpcode = inst.getRawBytes();
                rawString = OutputFormatter.formatByteString(rawOpcode);
            }
            LOG.log(Level.WARNING, String.format("Couldn't convert instruction to string at %08X: %s (%s)", memAddr, e.getMessage(), rawString));
            e.printStackTrace();
        }

        // update status view
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
    public void onAnalyzeError(long memAddr, String reason) {
        LOG.warning(String.format("Analyze error at location %X: %s", memAddr, reason));
    }

    @Override
    public void onAnalyzeStop() {
        double duration = (System.currentTimeMillis() - beginDisassembleTime) / 1000.0;
        LOG.info(String.format(
                    "Analysis finished after %.2f seconds, got %d entities",
                    duration, disassemblyData.getEntryCount())
                );

        // only perform the following steps on the first run, i.e. not when a script calls reanalyze etc.
        if(initialAnalyzeDone) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getImageView().setDocument(imageDoc);
                try {
                    gui.getImageView().scrollTo(imageFile.getCodeEntryPointMem());
                } catch(BadLocationException e) {
                    // ignore if scrolling didn't work
                }
                gui.getFunctionListView().setModel(functionList);
                gui.getStringListView().setModel(stringList);
            }
        });
        initialAnalyzeDone = true;
    }

    public void onFunctionDoubleClick(Function fun) {
        try {
            gui.getImageView().scrollTo(fun.getStartAddress());
        } catch(BadLocationException e) {
            LOG.warning("Invalid scroll location when double clicking function");
        }
    }

    public void onStringDoubleClicked(Data data) {
        try {
            gui.getImageView().scrollTo(data.getMemAddress());
        } catch(BadLocationException e) {
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
            } catch(BadLocationException e) {
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
            seq.patchByte(imageFile.toFileAddress(addr + i), (byte) 0x90);
        }
        seq.unlock();
        disassembler.reanalyze(inst.getMemAddress());
    }

    public void onPatchedSave(Path path) {
        ByteSequence seq = imageFile.getByteSequence(imageFile.getCodeEntryPointMem(), true);
        try {
            seq.savePatched(path);
        } catch(IOException e) {
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

    public ImageFile getImageFile() {
        return imageFile;
    }

    public Disassembler getDisassembler() {
        return disassembler;
    }

    public void onFunctionRenameReq(Function fun, String newName) {
        if(newName.length() == 0) {
            showError("Function name cannot be empty");
            return;
        }

        fun.setName(newName);
    }

    public void onCommentChangeReq(DataEntry data, String comment) {
        disassemblyData.insertComment(data.getAddress(), comment);
    }

    public void onGotoRequest(String where) {
        if(imageFile == null) {
            showError("No image file loaded");
            return;
        }

        try {
            long addr = Long.parseLong(where, 16);
            gui.getImageView().scrollTo(addr);
        } catch(Exception e) {
            showError("Invalid address: " + e.getMessage());
        }
    }

    public void onGotoRequest(long addr) {
        try {
            gui.getImageView().scrollTo(addr);
        } catch(Exception e) {
            showError("Invalid address: " + e.getMessage());
        }
    }

    public void onExitRequest() {
        gui.dispose();
    }

    public void onClearLogRequest() {
        gui.getLogView().clear();
    }
}
