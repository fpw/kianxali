package kianxali.disassembler;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.Context;
import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ByteSequence;
import kianxali.image.ImageFile;

public class Disassembler implements AddressNameResolver, AddressNameListener {
    private static final Logger LOG = Logger.getLogger("kianxali.disassembler");

    // TODO: start at first address of the code segment, walking linear to the end
    //       while building the queue. Then iterate again until queue is empty

    private final Queue<Entry<Long, DecodedEntity>> workQueue;
    private final Set<DisassemblyListener> listeners;
    private final Map<Long, Function> functionInfo; // stores which trace start belongs to which function
    private final DisassemblyData disassemblyData;
    private final ImageFile imageFile;
    private final Context ctx;
    private final Decoder decoder;
    private Thread analyzeThread;

    public Disassembler(ImageFile imageFile, DisassemblyData data) {
        this.imageFile = imageFile;
        this.disassemblyData = data;
        this.functionInfo = new TreeMap<Long, Function>();
        this.listeners = new CopyOnWriteArraySet<>();
        this.workQueue = new PriorityQueue<>(100, new Comparator<Entry<Long, DecodedEntity>>() {
            @Override
            public int compare(Entry<Long, DecodedEntity> o1, Entry<Long, DecodedEntity> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        this.ctx = imageFile.createContext();
        this.decoder = ctx.createInstructionDecoder();

        disassemblyData.insertImageFileWithSections(imageFile);
        Map<Long, String> imports = imageFile.getImports();

        // add imports as functions
        for(Long memAddr : imports.keySet()) {
            Function imp = new Function(memAddr, this);
            functionInfo.put(memAddr, imp);
            disassemblyData.insertFunction(imp);
            imp.setName(imports.get(memAddr));
            onFunctionNameChange(imp);
        }

        long entry = imageFile.getCodeEntryPointMem();
        addCodeWork(entry);
    }

    public void addListener(DisassemblyListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DisassemblyListener listener) {
        listeners.remove(listener);
    }

    public synchronized void startAnalyzer() {
        if(analyzeThread != null) {
            throw new IllegalStateException("disassembler already running");
        }

        for(DisassemblyListener listener : listeners) {
            listener.onAnalyzeStart();
        }

        analyzeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                analyze();
            }
        });
        LOG.fine("Starting analyzer");
        analyzeThread.start();
    }

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

    private void analyze() {
        // 1st pass: Analyze code and data
        while(!Thread.interrupted()) {
            Entry<Long, DecodedEntity> entry = workQueue.poll();
            if(entry == null) {
                // no more work
                break;
            }
            if(entry.getValue() == null) {
                disassembleTrace(entry.getKey());
            } else if(entry.getValue() instanceof Data) {
                Data data = (Data) entry.getValue();
                analyzeData(data);
            }
        }

        // 2nd pass: propagate function information
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

        stopAnalyzer();
    }

    private void addCodeWork(long address) {
        SimpleEntry<Long, DecodedEntity> entry;
        entry = new AbstractMap.SimpleEntry<Long, DecodedEntity>(address, null);
        workQueue.add(entry);
    }

    private void addDataWork(Data data) {
        SimpleEntry<Long, DecodedEntity> entry;
        entry = new AbstractMap.SimpleEntry<Long, DecodedEntity>(data.getMemAddress(), data);
        workQueue.add(entry);
    }

    private void disassembleTrace(long memAddr) {
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
                // TODO: signal error
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
        }

        if(function != null && function.getEndAddress() < memAddr) {
            function.setEndAddress(memAddr);
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
            data.analyze(seq);
            disassemblyData.insertEntity(data);
        } catch(Exception e) {
            LOG.log(Level.WARNING, String.format("Data decode error (%s) at %08X", e, data.getMemAddress()), e);
            // TODO: change to raw data
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeError(data.getMemAddress());
            }
            throw e;
        } finally {
            seq.unlock();
        }
    }

    // checks whether the instruction's operands could start a new trace or data
    private void examineInstruction(Instruction inst, Function function) {
        // check if we have branch addresses to be analyzed later
        for(long addr : inst.getBranchAddresses()) {
            if(imageFile.isValidAddress(addr)) {
                if(inst.isFunctionCall()) {
                    DataEntry srcEntry = disassemblyData.getInfoCoveringAddress(inst.getMemAddress());
                    disassemblyData.insertReference(srcEntry, addr);
                    if(!functionInfo.containsKey(addr)) {
                        Function fun = new Function(addr, this);
                        functionInfo.put(addr, new Function(addr, this));
                        disassemblyData.insertFunction(fun);
                        onFunctionNameChange(fun);
                    }
                } else if(function != null) {
                    // if the branch is not a function call, it should belong to the current function
                    functionInfo.put(addr, function);
                }
                addCodeWork(addr);
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
            addDataWork(data);
        }
    }

    @Override
    public String resolveAddress(long memAddr) {
        Function fun;
        if((fun = functionInfo.get(memAddr)) != null) {
            if(fun.getStartAddress() == memAddr) {
                return functionInfo.get(memAddr).getName();
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
        for(DataEntry ref : entry.getReferences()) {
            disassemblyData.tellListeners(ref.getAddress());
        }
    }
}
