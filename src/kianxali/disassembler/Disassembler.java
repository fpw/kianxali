package kianxali.disassembler;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.Context;
import kianxali.decoder.Data;
import kianxali.decoder.Data.DataType;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.decoder.JumpTable;
import kianxali.loader.ByteSequence;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;

/**
 * This class implements a recursive-traversal disassembler. It gets
 * an {@link ImageFile} and fills a {@link DisassemblyData} instance,
 * informing {@link DisassemblyListener} implementations during the analysis.
 * @author fwi
 *
 */
public class Disassembler implements AddressNameResolver, AddressNameListener {
    private static final Logger LOG = Logger.getLogger("kianxali.disassembler");

    // TODO: start at first address of the code segment, walking linear to the end
    //       while building the queue. Then iterate again until queue is empty

    private final Queue<WorkItem> workQueue;
    private final Set<DisassemblyListener> listeners;
    private final Map<Long, Function> functionInfo; // stores which trace start belongs to which function
    private final DisassemblyData disassemblyData;
    private final ImageFile imageFile;
    private final Context ctx;
    private final Decoder decoder;
    private Thread analyzeThread;
    private boolean unknownDiscoveryRan;

    private class WorkItem implements Comparable<WorkItem> {
        // determines whether the work should analyze code (data == null) or data (data has type set)
        public Data data;
        public Long address;
        // only add trace if it runs without decoder errors; used for unknown function detection etc.
        public boolean careful;

        public WorkItem(Long address, Data data) {
            this.address = address;
            this.data = data;
        }

        @Override
        public int compareTo(WorkItem o) {
            return address.compareTo(o.address);
        }
    }

    /**
     * Create a new disassembler that can analyze an image file to
     * fill a disassembly data object. The actual analysis can be started
     * by calling {@link Disassembler#startAnalyzer()}.
     * @param imageFile the image file to disassemble
     * @param data the data object to fill during the analysis
     */
    public Disassembler(ImageFile imageFile, DisassemblyData data) {
        this.imageFile = imageFile;
        this.disassemblyData = data;
        this.functionInfo = new TreeMap<Long, Function>();
        this.listeners = new CopyOnWriteArraySet<>();
        this.workQueue = new PriorityQueue<>();
        this.ctx = imageFile.createContext();
        this.decoder = ctx.createInstructionDecoder();
        this.unknownDiscoveryRan = false;

        disassemblyData.insertImageFileWithSections(imageFile);
        Map<Long, String> imports = imageFile.getImports();

        // add imports as functions
        for(Long memAddr : imports.keySet()) {
            detectFunction(memAddr, imports.get(memAddr));
        }

        long entry = imageFile.getCodeEntryPointMem();
        addCodeWork(entry, false);
    }

    /**
     * Adds a listener that will be informed about the start, end and errors
     * of the analysis.
     * @param listener the listener to register
     */
    public void addListener(DisassemblyListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener
     */
    public void removeListener(DisassemblyListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts the actual disassembly. It will be run in a separate thread, i.e. this method
     * won't block. The listeners will be informed when the analysis is done or runs into
     * an error.
     */
    public synchronized void startAnalyzer() {
        if(analyzeThread != null) {
            throw new IllegalStateException("disassembler already running");
        }

        for(DisassemblyListener listener : listeners) {
            listener.onAnalyzeStart();
        }

        analyzeThread = new Thread(new Runnable() {
            public void run() {
                analyze();
            }
        });
        LOG.fine("Starting analyzer");
        analyzeThread.start();
    }

    /**
     * Stops the analysis thread. It can be started again with {@link Disassembler#startAnalyzer()}.
     */
    public synchronized void stopAnalyzer() {
        if(analyzeThread != null) {
            analyzeThread.interrupt();
            analyzeThread = null;
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeStop();
            }
            LOG.fine("Stopped analyzer");
        }
    }

    /**
     * Informs the disassembler that the given address should be analyzed again.
     * Subsequent addresses will also be analyzed.
     * @param addr the address to visit again
     */
    public synchronized void reanalyze(long addr) {
        disassemblyData.clearDecodedEntity(addr);

        addCodeWork(addr, false);
        if(analyzeThread == null) {
            startAnalyzer();
        }
    }

    private void workOnQueue() {
        while(!Thread.interrupted()) {
            WorkItem item = workQueue.poll();
            if(item == null) {
                // no more work
                break;
            }
            if(item.data == null) {
                disassembleTrace(item);
            } else {
                try {
                    analyzeData(item.data);
                } catch(Exception e) {
                    LOG.info(String.format("Couldn't parse data at %X: %s", item.data.getMemAddress(), e.getMessage()));
                }
            }
        }
    }

    private void analyze() {
        // Analyze code and data
        workOnQueue();

        // Propagate function information
        for(Function fun : functionInfo.values()) {
            disassemblyData.insertFunction(fun);

            // identify trampoline functions
            long start = fun.getStartAddress();
            DataEntry entry = disassemblyData.getInfoOnExactAddress(start);
            if(entry != null && entry.getEntity() instanceof Instruction) {
                Instruction inst = (Instruction) entry.getEntity();
                if(inst.isJump() && inst.getAssociatedData().size() == 1) {
                    // the function immediately jumps somewhere else, take name from there
                    Data data = inst.getAssociatedData().get(0);
                    long branch = data.getMemAddress();
                    Function realFun = functionInfo.get(branch);
                    if(realFun != null) {
                        fun.setName("!" + realFun.getName());
                        disassemblyData.tellListeners(branch);
                    }
                }
            }
        }

        // Now try to fill black holes by discovering functions that were not directly called
        if(!unknownDiscoveryRan) {
            discoverUncalledFunctions();
            workOnQueue();
            unknownDiscoveryRan = true;
        }

        stopAnalyzer();
    }

    private void addCodeWork(long address, boolean careful) {
        WorkItem itm = new WorkItem(address, null);
        itm.careful = careful;
        workQueue.add(itm);
    }

    private void addDataWork(Data data) {
        workQueue.add(new WorkItem(data.getMemAddress(), data));
    }

    private void disassembleTrace(WorkItem item) {
        long memAddr = item.address;
        Function function = functionInfo.get(memAddr);
        while(true) {
            DecodedEntity old = disassemblyData.getEntityOnExactAddress(memAddr);
            if(old instanceof Instruction) {
                // Already visited this trace
                // If it is data, now we'll overwrite it to code
                break;
            }

            DecodedEntity covering = disassemblyData.findEntityOnAddress(memAddr);
            if(covering != null) {
                LOG.warning(String.format("%08X already covered", memAddr));
                // TODO: covers other instruction or data
                break;
            }

            if(!imageFile.isValidAddress(memAddr)) {
                // TODO: Signal this somehow?
                break;
            }

            ctx.setInstructionPointer(memAddr);
            Instruction inst = null;
            ByteSequence seq = null;
            try {
                seq = imageFile.getByteSequence(memAddr, true);
                inst = decoder.decodeOpcode(ctx, seq);
            } catch(Exception e) {
                LOG.log(Level.WARNING, String.format("Disassemble error (%s) at %08X: %s", e, memAddr, inst), e);
                if(item.careful) {
                    // TODO: undo everything or something
                }
                break;
            } finally {
                if(seq != null) {
                    seq.unlock();
                }
            }

            if(inst == null) {
                // couldn't decode instruction
                // TODO: change to data
                for(DisassemblyListener listener : listeners) {
                    listener.onAnalyzeError(memAddr);
                }
                break;
            }

            disassemblyData.insertEntity(inst);

            examineInstruction(inst, function);

            if(inst.stopsTrace()) {
                break;
            }
            memAddr += inst.getSize();

            // Check if we are in a different function now. This can happen
            // if a function doesn't end with ret but just runs into a different function,
            // e.g. after a call to ExitProcess
            Function newFunction = functionInfo.get(memAddr);
            if(newFunction != null) {
                function = newFunction;
            }
        }
        if(function != null && function.getEndAddress() < memAddr) {
            disassemblyData.updateFunctionEnd(function, memAddr);
        }
    }

    private void analyzeData(Data data) {
        long memAddr = data.getMemAddress();
        DataEntry cover = disassemblyData.getInfoCoveringAddress(memAddr);
        if(cover != null) {
            if(cover.hasInstruction()) {
                // data should not overwrite instruction
                return;
            } else if(cover.hasData()) {
                // TODO: new information about data, e.g. DWORD also accessed byte-wise
                return;
            }
        }

        ByteSequence seq = imageFile.getByteSequence(memAddr, true);

        try {
            // jump tables are a special case: need to guess the number of entries
            if(data instanceof JumpTable) {
                analyzeJumpTable(seq, (JumpTable) data);
            } else {
                data.analyze(seq);
            }
            DataEntry entry = disassemblyData.insertEntity(data);

            // attach data information to entries that point to this data
            for(DataEntry ref : entry.getReferences()) {
                ref.attachData(data);
                disassemblyData.tellListeners(ref.getAddress());
            }
        } catch(Exception e) {
            LOG.log(Level.WARNING, String.format("Data decode error (%s) at %08X", e, data.getMemAddress()));
            // TODO: change to raw data
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeError(data.getMemAddress());
            }
            throw e;
        } finally {
            seq.unlock();
        }
    }

    private void analyzeJumpTable(ByteSequence seq, JumpTable table) {
        // strategy: evaluate entries until either an invalid memory address is found
        // or something that is already covered by code but not the start of an instruction
        int entrySize = table.getTableScaling();
        boolean badEntry = false;
        int i = 0;
        do {
            long entryAddr;
            switch(entrySize) {
            case 1: entryAddr = seq.readUByte(); break;
            case 2: entryAddr = seq.readUWord(); break;
            case 4: entryAddr = seq.readUDword(); break;
            case 8: entryAddr = seq.readSDword(); break; // FIXME
            default: throw new UnsupportedOperationException("invalid jump table entry size: " + entrySize);
            }

            if(!imageFile.isCodeAddress(entryAddr)) {
                // invalid address -> can't be a valid entry, i.e. table ended
                badEntry = true;
            } else {
                DataEntry entry = disassemblyData.getInfoCoveringAddress(entryAddr);
                if(entry != null) {
                    DecodedEntity entity = entry.getEntity();
                    if((entity instanceof Instruction && entry.getAddress() != entryAddr) || entity instanceof Data) {
                        // the entry points to a code location but not to a start of an instruction -> bad entry
                        badEntry = true;
                    }
                }
            }
            if(!badEntry) {
                table.addEntry(entryAddr);
                disassemblyData.insertComment(entryAddr, String.format("Entry %d of jump table %08X", i, table.getMemAddress()));
                addCodeWork(entryAddr, true);
                i++;
            }
        } while(!badEntry);
    }

    private Function detectFunction(long addr, String name) {
        if(!functionInfo.containsKey(addr)) {
            Function fun = new Function(addr, this);
            functionInfo.put(addr, fun);
            disassemblyData.insertFunction(fun);
            if(name != null) {
                fun.setName(name);
            }
            onFunctionNameChange(fun);
            return fun;
        }
        return null;
    }

    // checks whether the instruction's operands could start a new trace or data
    private void examineInstruction(Instruction inst, Function function) {
        DataEntry srcEntry = disassemblyData.getInfoCoveringAddress(inst.getMemAddress());

        // check if we have branch addresses to be analyzed later
        for(long addr : inst.getBranchAddresses()) {
            if(imageFile.isValidAddress(addr)) {
                if(inst.isFunctionCall()) {
                    disassemblyData.insertReference(srcEntry, addr);
                    detectFunction(addr, null);
                } else if(function != null) {
                    // if the branch is not a function call, it should belong to the current function
                    functionInfo.put(addr, function);
                }
                addCodeWork(addr, false);
                return;
            } else {
                // TODO: Issue warning event about invalid code address
                LOG.warning(String.format("Code at %08X references invalid address %08X", inst.getMemAddress(), addr));
                for(DisassemblyListener listener : listeners) {
                    listener.onAnalyzeError(inst.getMemAddress());
                }
            }
        }

        // check if we have associated data to be analyzed later
        for(Data data : inst.getAssociatedData()) {
            long addr = data.getMemAddress();
            if(!imageFile.isValidAddress(addr)) {
                continue;
            }

            if(inst.isJump() && !imageFile.getImports().containsKey(addr)) {
                // a jump into a dereferenced data pointer means that the data is a table with jump destinations
                // imports are a trivial single-entry jump table, hence they are discarded
                LOG.finer(String.format("Probable jump table: %08X into %08X", inst.getMemAddress(), addr));

                // contents of the jump table will be evaluated in the data analyze pass
                JumpTable table = new JumpTable(addr);
                if(data.getTableScaling() == 0) {
                    table.setTableScaling(ctx.getDefaultAddressSize());
                } else {
                    table.setTableScaling(data.getTableScaling());
                }
                disassemblyData.insertReference(srcEntry, addr);
                addDataWork(table);
            } else {
                disassemblyData.insertReference(srcEntry, addr);
                addDataWork(data);
            }
        }

        // Check for probable pointers
        for(long addr : inst.getProbableDataPointers()) {
            if(imageFile.isValidAddress(addr)) {
                if(disassemblyData.getEntityOnExactAddress(addr) != null) {
                    continue;
                }
                disassemblyData.insertReference(srcEntry, addr);

                if(imageFile.isCodeAddress(addr)) {
                    addCodeWork(addr, true);
                } else {
                    Data data = new Data(addr, DataType.UNKNOWN);
                    addDataWork(data);
                }
            }
        }
    }

    private void discoverUncalledFunctions() {
        LOG.fine("Discovering uncalled functions...");
        for(Section section : imageFile.getSections()) {
            if(!section.isExecutable()) {
                continue;
            }

            // searching for signature 55 8B EC or 55 89 EF (both are push ebp; mov ebp, esp)
            boolean got55 = false, got558B = false, got5589 = false;
            long startAddr = section.getStartAddress();
            ByteSequence seq = imageFile.getByteSequence(startAddr, false);
            long size = section.getEndAddress() - startAddr;
            for(long i = 0; i < size; i++) {
                short s = seq.readUByte();
                if(disassemblyData.getInfoCoveringAddress(startAddr + i) != null) {
                    continue;
                }

                if(s == 0x55) {
                    got55 = true;
                    got558B = false;
                    got5589 = false;
                } else if(s == 0x8B && got55) {
                    got55 = false;
                    got558B = true;
                    got5589 = false;
                } else if(s == 0x89 && got55) {
                    got55 = false;
                    got558B = false;
                    got5589 = true;
                } else if((s == 0xEC && got558B) || (s == 0xEF && got5589)) {
                    // found signature
                    got55 = false;
                    got558B = false;
                    got5589 = false;
                    long funAddr = startAddr + i - 2;
                    LOG.finer(String.format("Discovered indirect function %08X", funAddr));
                    Function fun = detectFunction(funAddr, null);
                    if(fun != null) {
                        fun.setName(fun.getName() + "_i"); // mark as indirectly called
                    }
                    addCodeWork(funAddr, true);
                } else {
                    got55 = false;
                    got558B = false;
                }
            }
        }
    }

    @Override
    public String resolveAddress(long memAddr) {
        Function fun = functionInfo.get(memAddr);
        if(fun != null) {
            if(fun.getStartAddress() == memAddr) {
                return fun.getName();
            }
        }
        return null;
    }

    @Override
    public void onFunctionNameChange(Function fun) {
        DataEntry entry = disassemblyData.getInfoOnExactAddress(fun.getStartAddress());
        if(entry == null) {
            LOG.warning("Unkown function renamed: " + fun.getName());
            return;
        }

        disassemblyData.tellListeners(fun.getStartAddress());
        disassemblyData.tellListeners(fun.getEndAddress());
        for(DataEntry ref : entry.getReferences()) {
            disassemblyData.tellListeners(ref.getAddress());
        }
    }
}
