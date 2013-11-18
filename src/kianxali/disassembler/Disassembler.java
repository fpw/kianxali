package kianxali.disassembler;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
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

public class Disassembler {
    private static final Logger LOG = Logger.getLogger("kianxali.disassembler");

    // TODO: start at first address of the code segment, walking linear to the end
    //       while building the queue. Then iterate again until queue is empty

    private final Queue<Entry<Long, DecodedEntity>> workQueue;
    private final Set<DisassemblyListener> listeners;
    private final DisassemblyData disassemblyData;
    private final ImageFile imageFile;
    private final Context ctx;
    private final Decoder decoder;
    private Thread analyzeThread;

    public Disassembler(ImageFile imageFile, DisassemblyData data) {
        this.imageFile = imageFile;
        this.disassemblyData = data;
        this.listeners = new CopyOnWriteArraySet<>();
        this.workQueue = new PriorityQueue<>(100, new Comparator<Entry<Long, DecodedEntity>>() {
            @Override
            public int compare(Entry<Long, DecodedEntity> o1, Entry<Long, DecodedEntity> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        this.ctx = imageFile.createContext();
        this.decoder = ctx.createInstructionDecoder();
        addCodeWork(imageFile.getCodeEntryPointMem());
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

    private void analyzeData(Data data) {
        ByteSequence seq = imageFile.getByteSequence(data.getMemAddress(), true);
        try {
            data.analyze(seq);
            disassemblyData.insertEntity(data);
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeEntity(data);
            }
        } catch(Exception e) {
            LOG.log(Level.WARNING, String.format("Data decode error (%s) at %08X", e, data.getMemAddress()), e);
            disassemblyData.clearAddress(data.getMemAddress());
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeError(data.getMemAddress());
            }
            throw e;
        } finally {
            seq.unlock();
        }
    }

    private void disassembleTrace(long memAddr) {
        while(true) {
            DecodedEntity old = disassemblyData.getEntityOnExactAddress(memAddr);
            if(old instanceof Instruction) {
                // Already visited this trace
                // If it is data, now we'll overwrite it to code
                break;
            }

            if(disassemblyData.findEntity(memAddr) != null) {
                // TODO: covers other instruction or data
                break;
            }

            ctx.setInstructionPointer(memAddr);
            Instruction inst = null;
            ByteSequence seq = imageFile.getByteSequence(memAddr, true);
            try {
                inst = decoder.decodeOpcode(ctx, seq);
            } catch(Exception e) {
                LOG.log(Level.WARNING, String.format("Disassemble error (%s) at %08X: %s", e, memAddr, inst), e);
                // TODO: signal error instead of throw
                throw e;
            } finally {
                seq.unlock();
            }

            if(inst == null) {
                disassemblyData.clearAddress(memAddr);
                for(DisassemblyListener listener : listeners) {
                    listener.onAnalyzeError(memAddr);
                }
                break;
            }

            disassemblyData.insertEntity(inst);
            for(DisassemblyListener listener : listeners) {
                listener.onAnalyzeEntity(inst);
            }

            examineInstruction(inst);

            if(inst.stopsTrace()) {
                break;
            }

            memAddr += inst.getSize();
        }
    }

    // checks whether the instruction's operands could start a new trace or data
    private void examineInstruction(Instruction inst) {
        // check if we have branch addresses to be analyzed later
        for(long addr : inst.getBranchAddresses()) {
            if(imageFile.isValidAddress(addr)) {
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
}
