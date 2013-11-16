package kianxali.disassembler;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.Context;
import kianxali.decoder.Data;
import kianxali.decoder.DecodableEntity;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ByteSequence;
import kianxali.image.ImageFile;
import kianxali.image.LoadedImage;

public class Disassembler {
    private static final Logger LOG = Logger.getLogger("kianxali.disassembler");

    private final Queue<Entry<Long, DecodableEntity>> workQueue;
    private final LoadedImage memoryMap;
    private final ImageFile imageFile;
    private final Context ctx;
    private final Decoder decoder;
    private Thread analyzeThread;

    public Disassembler(ImageFile imageFile) {
        this.imageFile = imageFile;
        this.memoryMap = new LoadedImage();
        this.workQueue = new PriorityQueue<>(100, new Comparator<Entry<Long, DecodableEntity>>() {
            @Override
            public int compare(Entry<Long, DecodableEntity> o1, Entry<Long, DecodableEntity> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        this.ctx = imageFile.createContext();
        this.decoder = ctx.createInstructionDecoder();
        addCodeWork(imageFile.getCodeEntryPointMem());
    }

    public synchronized void startAnalyzer() {
        if(analyzeThread != null) {
            throw new IllegalStateException("disassembler already running");
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
        }
    }

    private void analyze() {
        while(!Thread.interrupted()) {
            Entry<Long, DecodableEntity> entry = workQueue.poll();
            if(entry == null) {
                // no more work
                break;
            }
            if(entry.getValue() == null) {
                LOG.finest(String.format("Analyzing code %08X", entry.getKey()));
                disassembleTrace(entry.getKey());
            } else if(entry.getValue() instanceof Data) {
                Data data = (Data) entry.getValue();
                LOG.finest(String.format("Analyzing data %08X", data.getMemAddress()));
                analyzeData(data);
            }
        }
        LOG.fine("Stopped analyzer");
        stopAnalyzer();
    }

    private void addCodeWork(long address) {
        SimpleEntry<Long, DecodableEntity> entry;
        entry = new AbstractMap.SimpleEntry<Long, DecodableEntity>(address, null);
        workQueue.add(entry);
    }

    private void addDataWork(Data data) {
        SimpleEntry<Long, DecodableEntity> entry;
        entry = new AbstractMap.SimpleEntry<Long, DecodableEntity>(data.getMemAddress(), data);
        workQueue.add(entry);
    }

    private void analyzeData(Data data) {
        ByteSequence seq = imageFile.getByteSequence(data.getMemAddress(), true);
        try {
            data.analyze(seq);
        } catch(Exception e) {
            LOG.log(Level.WARNING, String.format("Data decode error (%s) at %08X", e, data.getMemAddress()), e);
            // TODO: signal error instead of throw
            throw e;
        } finally {
            seq.unlock();
        }
    }

    private void disassembleTrace(long memAddr) {
        while(true) {
            if(!isValidAddress(memAddr)) {
                break;
            }

            DecodableEntity old = memoryMap.getEntityOnExactAddress(memAddr);
            if(old instanceof Instruction) {
                // Already visited this trace
                // If it is data, now we'll overwrite it to code
                break;
            }

            // TODO: could cover other instruction, need to issue warning

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
                // TODO: couldn't decode, use special error entity above
                LOG.warning(String.format("Unknown opcode at %08X", memAddr));
                break;
            }

            examineInstruction(inst);
            memoryMap.insert(inst);
            memAddr += inst.getSize();
            if(inst.stopsTrace()) {
                break;
            }
        }
    }

    // checks whether the instruction's operands could start a new trace or data
    private void examineInstruction(Instruction inst) {
        // check if we have branch addresses to be analyzed later
        for(long addr : inst.getBranchAddresses()) {
            if(isValidAddress(addr)) {
                addCodeWork(addr);
            } else {
                // TODO: Issue warning event about invalid code address
                LOG.warning(String.format("Code at %08X references invalid address %08X", inst.getMemAddress(), addr));
            }
        }

        // check if we have associated data to be analyzed later
        for(Data data : inst.getAssociatedData()) {
            long addr = data.getMemAddress();
            if(!isValidAddress(addr)) {
                continue;
            }
            addDataWork(data);
        }
    }

    private boolean isValidAddress(long memAddr) {
        return imageFile.getSectionForMemAddress(memAddr) != null;
    }
}
