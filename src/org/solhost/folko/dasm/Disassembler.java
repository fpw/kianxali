package org.solhost.folko.dasm;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.solhost.folko.dasm.decoder.Context;
import org.solhost.folko.dasm.decoder.Instruction;
import org.solhost.folko.dasm.decoder.InstructionDecoder;

public class Disassembler {
    private final ImageFile image;
    private final Set<DisassemblingListener> listeners;
    private final Set<Long> visitedAddresses;
    private final Queue<Long> pendingInstructionAddresses;

    public Disassembler(ImageFile image) {
        this.visitedAddresses = new HashSet<>();
        this.pendingInstructionAddresses = new PriorityQueue<>();
        this.listeners = new CopyOnWriteArraySet<>();
        this.image = image;
    }

    public void addDisassemblingListener(DisassemblingListener listener) {
        listeners.add(listener);
    }

    public void removeDisassemblingListener(DisassemblingListener listener) {
        listeners.remove(listener);
    }

    public void disassemble() throws Exception {
        Context ctx = image.createContext();
        InstructionDecoder decoder = ctx.createInstructionDecoder();
        pendingInstructionAddresses.add(image.getCodeEntryPointMem());
        while(pendingInstructionAddresses.size() > 0) {
            long memAddr = pendingInstructionAddresses.poll();
            disassembleTrace(memAddr, ctx, decoder);
        }
    }

    private void disassembleTrace(long memAddr, Context ctx, InstructionDecoder decoder) {
        while(true) {
            if(visitedAddresses.contains(memAddr)) {
                break;
            } else {
                visitedAddresses.add(memAddr);
            }
            ctx.setInstructionPointer(memAddr);
            Instruction inst = decoder.decodeOpcode(ctx, image.getByteSequence(memAddr));
            if(inst != null) {
                for(DisassemblingListener listener : listeners) {
                    try {
                        listener.onInstructionDecode(inst);
                    } catch(Exception e) {
                        System.err.println("Error processing: " + inst.toString());
                        throw e;
                    }
                }
            }

            if(inst == null || inst.stopsTrace()) {
                break;
            }
            memAddr += inst.getSize();

            checkNewTraces(inst);
        }
    }

    // checks whether the instruction's operands could start a new trace
    private void checkNewTraces(Instruction inst) {
        Long addr = inst.getBranchAddress();
        if(addr != null && !visitedAddresses.contains(addr)) {
            pendingInstructionAddresses.add(addr);
        }
    }
}
