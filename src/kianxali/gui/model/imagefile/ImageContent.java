package kianxali.gui.model.imagefile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

public class ImageContent implements Content {
    // translate addresses to string lines
    private final NavigableMap<Long, List<String>> addressLines;

    private ImagePosition nextInsertPosition;
    private int length;

    private class ImagePosition implements Position {
        private long address;
        private int line, lineOffset;

        public ImagePosition(long address, int line, int lineOffset) {
            this.address = address;
            this.line = line;
            this.lineOffset = lineOffset;
        }

        @Override
        public int getOffset() {
            int offset = 0;
            // TODO

            return offset;
        }
    }

    public ImageContent() {
        this.addressLines = new TreeMap<>();
        this.length = 0;
    }

    public void prepareInsert(long address, int line, int lineOffset) {
        nextInsertPosition = new ImagePosition(address, line, lineOffset);
    }

    @Override
    public UndoableEdit insertString(int where, String str) throws BadLocationException {
        if(nextInsertPosition == null) {
            throw new BadLocationException("unprepared insert", where);
        }

        Entry<Long, List<String>> floorEntry = addressLines.floorEntry(nextInsertPosition.address);
        List<String> lines;
        if(floorEntry == null) {
            if(where != 0) {
                throw new IllegalStateException("entry at beginning with offset != 0");
            }
            lines = new ArrayList<String>();
            addressLines.put(nextInsertPosition.address, lines);
        } else {
            lines = floorEntry.getValue();
        }
        while(nextInsertPosition.line >= lines.size()) {
            lines.add("");
        }
        String line = lines.get(nextInsertPosition.line);
        String newLine = line.substring(0, nextInsertPosition.lineOffset) + str + line.substring(nextInsertPosition.lineOffset);
        lines.set(nextInsertPosition.line, newLine);
        length += str.length();

        nextInsertPosition = null;
        return null;
    }

    @Override
    public UndoableEdit remove(int where, int nitems) throws BadLocationException {
        throw new BadLocationException("remove not implemented", where);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public ImagePosition createPosition(int offset) throws BadLocationException {
        ImagePosition res = new ImagePosition(0, 0, offset);
        int remain = offset;

        for(Entry<Long, List<String>> entry : addressLines.entrySet()) {
            res.address = entry.getKey();
            res.line = 0;
            for(String line : entry.getValue()) {
                if(remain - line.length() >= 0) {
                    remain -= line.length();
                    res.line++;
                } else {
                    res.lineOffset = remain;
                    remain = 0;
                    break;
                }
            }
            if(remain == 0) {
                break;
            }
        }

        return res;
    }

    @Override
    public String getString(int where, int len) throws BadLocationException {
        Segment seg = new Segment();
        getChars(where, len, seg);
        return new String(seg.toString());
    }

    @Override
    public void getChars(int where, int len, Segment txt) throws BadLocationException {
//        ImagePosition pos = createPosition(where);
//        StringBuilder res = new StringBuilder();
//        int line = pos.line, doneLen = 0;
//        while(doneLen < len) {
//            // TODO
//        }

        return;
    }
}
