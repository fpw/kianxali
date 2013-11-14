package kianxali.disassembler;

import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import kianxali.decoder.Context;
import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ImageFile;

public class Disassembler {
    private static final Logger LOG = Logger.getLogger("kianxali.disassembler");

    private final Set<DisassemblingListener> listeners;
    private Map<Long, DecodedEntity> decodedLocations;
    private Queue<Long> pendingInstructionAddresses;
    private ImageFile image;

    public Disassembler() {
        this.listeners = new CopyOnWriteArraySet<>();
    }

    public void reset() {
        this.decodedLocations = new ConcurrentSkipListMap<>();
        this.pendingInstructionAddresses = new PriorityQueue<>();
    }

    public void addDisassemblingListener(DisassemblingListener listener) {
        listeners.add(listener);
    }

    public void removeDisassemblingListener(DisassemblingListener listener) {
        listeners.remove(listener);
    }

    public void disassemble(ImageFile imageFile) throws Exception {
        this.image = imageFile;
        reset();

        // 1st pass: decode instructions
        LOG.fine("Disassembling: 1st pass...");
        Context ctx = image.createContext();
        Decoder decoder = ctx.createInstructionDecoder();
        pendingInstructionAddresses.add(image.getCodeEntryPointMem());
        while(pendingInstructionAddresses.size() > 0 && !Thread.currentThread().isInterrupted()) {
            long memAddr = pendingInstructionAddresses.poll();
            disassembleTrace(memAddr, ctx, decoder);
        }

        if(Thread.currentThread().isInterrupted()) {
            return;
        }

        // 2nd pass: decode data
        LOG.fine("Disassembling: 2nd pass...");
        for(DecodedEntity entity : decodedLocations.values()) {
            if(Thread.currentThread().isInterrupted()) {
                return;
            }
            if(!(entity instanceof Data)) {
                continue;
            }
            Data data = (Data) entity;
            data.analyze(image.getByteSequence(data.getMemAddress()));
        }
        LOG.fine("Disassembling: done");
    }

    public DecodedEntity getEntity(long memAddr) {
        return decodedLocations.get(memAddr);
    }

    public Map<Long, DecodedEntity> getEntities() {
        return Collections.unmodifiableMap(decodedLocations);
    }

    private void disassembleTrace(long memAddr, Context ctx, Decoder decoder) {
        while(!Thread.currentThread().isInterrupted()) {
            if(!isValidAddress(memAddr)) {
                break;
            }
            if(decodedLocations.containsKey(memAddr) && decodedLocations.get(memAddr) instanceof Instruction) {
                break;
            }

            ctx.setInstructionPointer(memAddr);
            Instruction inst = null;
            try {
                inst = decoder.decodeOpcode(ctx, image.getByteSequence(memAddr));
                if(inst != null) {
                    decodedLocations.put(memAddr, inst);
                    examineInstruction(inst);
                    tellEntityChange(memAddr);
                    memAddr += inst.getSize();
                }
            } catch(Exception e) {
                System.err.println(String.format("Disassemble error at %08X: %s", memAddr, inst));
                throw e;
            }

            if(inst == null || inst.stopsTrace()) {
                break;
            }
        }
    }

    private void tellEntityChange(long memAddr) {
        for(DisassemblingListener listener : listeners) {
            try {
                listener.onDisassembledAddress(memAddr);
            } catch(Exception e) {
                System.err.println("Error processing: " + decodedLocations.get(memAddr));
                throw e;
            }
        }
    }

    // checks whether the instruction's operands could start a new trace or data
    private void examineInstruction(Instruction inst) {
        for(long addr : inst.getBranchAddresses()) {
            if(decodedLocations.containsKey(addr)) {
                if(decodedLocations.get(addr) instanceof Instruction) {
                    continue;
                }
            }
            pendingInstructionAddresses.add(addr);
        }

        for(Data data : inst.getAssociatedData()) {
            long addr = data.getMemAddress();
            if(decodedLocations.containsKey(addr)) {
                // TODO: do something smart. for now, just take first
                continue;
            }
            if(isValidAddress(addr)) {
                decodedLocations.put(addr, data);
                tellEntityChange(addr);
            }
        }
    }

    private boolean isValidAddress(long memAddr) {
        return image.getSectionForMemAddress(memAddr) != null;
    }
}
