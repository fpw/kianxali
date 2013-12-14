package kianxali.gui.models;

import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import kianxali.disassembler.DataEntry;
import kianxali.disassembler.DataListener;
import kianxali.disassembler.Function;

public class FunctionList extends AbstractListModel<Function> implements DataListener {
    private static final long serialVersionUID = 1L;
    private final NavigableSet<FunctionEntry> functions;

    private class FunctionEntry implements Comparable<FunctionEntry> {
        Function function;
        long address;

        public FunctionEntry(long memAddr) {
            this.address = memAddr;
        }

        public FunctionEntry(long memAddr, Function fun) {
            this.address = memAddr;
            this.function = fun;
        }

        @Override
        public int compareTo(FunctionEntry o) {
            return Long.compare(this.address, o.address);
        }
    }

    public FunctionList() {
        functions = new TreeSet<>();
    }

    public void clear() {
        int oldSize = functions.size();
        functions.clear();
        if(oldSize > 0) {
            fireContentsChanged(this, 0, oldSize - 1);
        }
    }

    private Integer getIndex(long memAddr) {
        int i = 0;
        for(FunctionEntry entry : functions) {
            if(entry.address == memAddr) {
                return i;
            }
        }
        return null;
    }

    @Override
    public synchronized void onAnalyzeChange(long memAddr, DataEntry entry) {
        Integer oldIndex = getIndex(memAddr);
        if(oldIndex != null) {
            // update entry
            if((entry == null || entry.getStartFunction() == null)) {
                // remove function
                functions.remove(new FunctionEntry(memAddr));
                fireIntervalRemoved(this, oldIndex, oldIndex);
            } else {
                // changing entry
                int index = getIndex(memAddr);
                functions.remove(new FunctionEntry(memAddr));
                functions.add(new FunctionEntry(memAddr, entry.getStartFunction()));
                fireContentsChanged(this, index, index);
            }
        } else {
            // add entry
            if(entry == null || entry.getStartFunction() == null) {
                return;
            }
            functions.add(new FunctionEntry(memAddr, entry.getStartFunction()));
            int index = getIndex(memAddr);
            fireIntervalAdded(this, index, index);
        }
    }

    @Override
    public synchronized int getSize() {
        return functions.size();
    }

    @Override
    public synchronized Function getElementAt(int index) {
        int i = 0;
        for(FunctionEntry entry : functions) {
            if(i == index) {
                return entry.function;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("invalid index: " + index);
    }
}
