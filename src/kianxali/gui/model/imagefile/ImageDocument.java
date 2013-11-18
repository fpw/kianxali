package kianxali.gui.model.imagefile;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

public class ImageDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;
    private final NavigableMap<Long, Element> offsetMap;

    public ImageDocument() {
        this.offsetMap = new TreeMap<>();
    }

    private ElementSpec startTag(MutableAttributeSet attr, short direction) {
        ElementSpec res = new ElementSpec(attr, ElementSpec.StartTagType);
        res.setDirection(direction);
        return res;
    }

    private ElementSpec contentTag(MutableAttributeSet attr, String str, short direction) {
        char[] line = str.toCharArray();
        ElementSpec res = new ElementSpec(attr, ElementSpec.ContentType, line, 0, line.length);
        res.setDirection(direction);
        return res;
    }

    private ElementSpec endTag() {
        return new ElementSpec(null, ElementSpec.EndTagType);
    }

    public synchronized void setOffsetLines(long memAddr, String[] lines) {
        Element element = offsetMap.get(memAddr);
        int offset;
        if(element == null) {
            Entry<Long, Element> entry = offsetMap.floorEntry(memAddr);
            if(entry == null) {
                element = getDefaultRootElement();
                offset = element.getStartOffset();
            } else {
                element = entry.getValue();
                offset = element.getEndOffset();
            }
        } else {
            // remove old paragraph
            offset = element.getStartOffset();
            removeElement(element);
        }

        try {
            List<ElementSpec> specs = new LinkedList<>();
            specs.add(endTag());
            for(String line : lines) {
                String content = String.format("%08X: %s", memAddr, line);
                specs.add(startTag(new SimpleAttributeSet(), ElementSpec.OriginateDirection));
                specs.add(contentTag(new SimpleAttributeSet(), content, ElementSpec.OriginateDirection));
                specs.add(endTag());
            }
            ElementSpec[] specArr = new ElementSpec[specs.size()];
            specs.toArray(specArr);
            insert(offset, specArr);
            offsetMap.put(memAddr, getParagraphElement(offset));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
