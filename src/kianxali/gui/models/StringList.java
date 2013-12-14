package kianxali.gui.models;

import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import kianxali.decoder.Data;
import kianxali.decoder.Data.DataType;
import kianxali.disassembler.DataEntry;
import kianxali.disassembler.DataListener;

public class StringList extends AbstractListModel<Data> implements DataListener {
    private static final long serialVersionUID = 1L;
    private final NavigableSet<StringEntry> strings;

    private class StringEntry implements Comparable<StringEntry> {
        Data data;
        long address;

        public StringEntry(long addr) {
            this.address = addr;
        }

        public StringEntry(long addr, Data data) {
            this.address = addr;
            this.data = data;
        }

        @Override
        public int compareTo(StringEntry o) {
            return Long.compare(this.address, o.address);
        }
    }

    public StringList() {
        this.strings = new TreeSet<>();
    }

    public void clear() {
        int oldSize = strings.size();
        strings.clear();
        if(oldSize > 0) {
            fireContentsChanged(this, 0, oldSize - 1);
        }
    }

    private Integer getIndex(long memAddr) {
        int i = 0;
        for(StringEntry entry : strings) {
            if(entry.address == memAddr) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void onAnalyzeChange(long memAddr, DataEntry entry) {
        Integer oldIndex = getIndex(memAddr);
        Data data = null;
        if(entry != null && entry.getEntity() instanceof Data) {
            data = (Data) entry.getEntity();
        }
        if(oldIndex != null) {
            // update entry
            if(data == null || data.getType() != DataType.STRING) {
                // remove entry
                strings.remove(new StringEntry(memAddr));
                fireIntervalRemoved(this, oldIndex, oldIndex);
            } else {
                // changing entry
                int index = getIndex(memAddr);
                strings.remove(new StringEntry(memAddr));
                strings.add(new StringEntry(memAddr, data));
                fireContentsChanged(this, index, index);
            }
        } else {
            // add entry
            if(data == null || data.getType() != DataType.STRING) {
                return;
            }
            strings.add(new StringEntry(memAddr, data));
            int index = getIndex(memAddr);
            fireIntervalAdded(this, index, index);
        }
    }

    @Override
    public int getSize() {
        return strings.size();
    }

    @Override
    public Data getElementAt(int index) {
        int i = 0;
        for(StringEntry entry : strings) {
            if(i == index) {
                return entry.data;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("invalid index: " + index);
    }
}
