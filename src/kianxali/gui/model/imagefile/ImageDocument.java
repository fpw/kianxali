package kianxali.gui.model.imagefile;

import java.util.Enumeration;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.UndoableEdit;

public class ImageDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;
    public static final Object OFFSET_ATTRIBUTE = new Object();

    public void insertImageFile(int numSections, AttributeSet attr) {
        writeLock();
        try {
            BranchElement root = (BranchElement) getDefaultRootElement();

            StringBuilder ins = new StringBuilder();
            for(int i = 0; i < numSections; i++) {
                ins.append("\n");
            }
            UndoableEdit ue = getContent().insertString(0, ins.toString());
            DefaultDocumentEvent dde = new DefaultDocumentEvent(0, numSections, DocumentEvent.EventType.INSERT);
            dde.addEdit(ue);
            insertUpdate(dde, new SimpleAttributeSet());
            dde.end();
            fireInsertUpdate(dde);

            DefaultDocumentEvent ev = new DefaultDocumentEvent(0, numSections, DocumentEvent.EventType.INSERT);
            ev.addEdit(ue);
            ImageFileElement fileElem = new ImageFileElement(root, numSections, attr);

            Element[] newRoot = new Element[] {fileElem};
            root.replace(0, 1, newRoot);

            Element[] oldRoot = new Element[numSections];
            for(int i = 0; i < numSections; i++) {
                oldRoot[i] = root.getElement(i);
            }
            root.replace(0, numSections, newRoot);

            ElementEdit ee = new ElementEdit(root, 0, oldRoot, newRoot);
            ev.addEdit(ee);
            fireInsertUpdate(ev);
            ev.end();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        writeUnlock();
    }

    private SectionElement getSectionElement(int sectionNum) {
        BranchElement root = (BranchElement) getDefaultRootElement();
        Enumeration<?> children = root.children();
        SectionElement section = null;
        while(children.hasMoreElements()) {
            Element cur = (Element) children.nextElement();
            if(cur instanceof ImageFileElement) {
                children = ((ImageFileElement) cur).children();
            } else if(cur instanceof SectionElement) {
                SectionElement se = (SectionElement) cur;
                if(se.getSectionNum() == sectionNum) {
                    section = se;
                    break;
                }
            }
        }
        return section;
    }

    private SectionInfoElement getSectionInfoElement(int num) {
        SectionElement section = getSectionElement(num);
        if(section == null) {
            return null;
        }
        return section.getSectionInfoElement();
    }

    public void insertSection(int sectionNum, String name, AttributeSet atts) {
        writeLock();
        try {
            Element info = getSectionInfoElement(sectionNum);
            if(info == null) {
                return;
            }
            int start = info.getStartOffset();
            UndoableEdit ue = getContent().insertString(info.getStartOffset(), name);
            DefaultDocumentEvent dde = new DefaultDocumentEvent(start, name.length(), DocumentEvent.EventType.INSERT);
            dde.addEdit(ue);
            insertUpdate(dde, atts);
            dde.end();
            fireInsertUpdate(dde);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        writeUnlock();
    }

    public void insertEntity(int sectionNum, long memAddr, String info, AttributeSet atts) {
        writeLock();
        try {
            SectionElement section = getSectionElement(sectionNum);
            if(section == null) {
                System.err.println("no section");
                return;
            }
            int startOffset = section.getOffsetForMemory(memAddr);
            UndoableEdit ue = getContent().insertString(startOffset, info);
            DefaultDocumentEvent dde = new DefaultDocumentEvent(startOffset, info.length(), DocumentEvent.EventType.INSERT);
            dde.addEdit(ue);
            insertUpdate(dde, atts);
            dde.end();
            fireInsertUpdate(dde);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeUnlock();
    }

    public class ImageFileElement extends BranchElement {
        private static final long serialVersionUID = 1L;

        public ImageFileElement(Element parent, int numSections, AttributeSet attributes) {
            super(parent, attributes);
            Element[] sections = new Element[numSections];
            for(int i = 0; i < numSections; i++) {
                sections[i] = new SectionElement(this, new SimpleAttributeSet(), i, i, i);
            }
            replace(0, 0, sections);
        }

        @Override
        public String getName() {
            return "Image";
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }

    public class SectionElement extends BranchElement {
        private static final long serialVersionUID = 1L;
        private final int sectionNum;
        private final SectionInfoElement sectionInfoElement;
        private final SortedMap<Long, DecodedElement> decodedElements;

        public SectionElement(Element parent, AttributeSet attributes, int num, int startOff, int endOff) {
            super(parent, attributes);
            this.sectionNum = num;
            this.decodedElements = new TreeMap<>();
            this.sectionInfoElement = new SectionInfoElement(this, new SimpleAttributeSet(), startOff, endOff);

            replace(0, 0, new Element[] {sectionInfoElement});
        }

        public SectionInfoElement getSectionInfoElement() {
            return sectionInfoElement;
        }

        public int getOffsetForMemory(long memAddr) {
            if(decodedElements.size() == 0) {
                return sectionInfoElement.getStartOffset();
            }

            long last = decodedElements.firstKey();
            // find first address that's bigger so we can insert before it
            for(long addr : decodedElements.keySet()) {
                if(addr > memAddr) {
                    break;
                } else {
                    last = addr;
                }
            }
            if(memAddr > last) {
                return decodedElements.get(last).getEndOffset();
            } else {
                return decodedElements.get(last).getStartOffset();
            }
        }

        public int getSectionNum() {
            return sectionNum;
        }

        @Override
        public String getName() {
            return "ImageSection";
        }

        @Override
        public boolean isLeaf() {
            return false;
        }
    }

    public class DecodedElement extends LeafElement {
        private static final long serialVersionUID = 1L;

        public DecodedElement(Element parent, AttributeSet a, int startOff, int endOff) {
            super(parent, a, startOff, endOff);
        }

        @Override
        public String getName() {
            return "DecodedElement";
        }
    }

    public class SectionInfoElement extends LeafElement {
        private static final long serialVersionUID = 1L;

        public SectionInfoElement(Element parent, AttributeSet a, int startOff, int endOff) {
            super(parent, a, startOff, endOff);
        }

        @Override
        public String getName() {
            return "SectionInfo";
        }
    }
}
