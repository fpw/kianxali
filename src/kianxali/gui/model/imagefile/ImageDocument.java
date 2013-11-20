package kianxali.gui.model.imagefile;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import kianxali.decoder.DecodedEntity;

public class ImageDocument extends DefaultStyledDocument {
    public static final String FORMATTER_KEY = "formatter";
    private static final long serialVersionUID = 1L;
    private final NavigableMap<Long, Element> offsetMap;
    private final MutableAttributeSet entityAttributes;
    private final MutableAttributeSet paragraphAttributes, sectionAttributes;

    public ImageDocument() {
        // PriorityQueue:           ~ 128s
        // Hoechste Adresse zuerst  > 300s
        // FIFO                     > 300s
        this.offsetMap              = new TreeMap<Long, Element>();
        this.paragraphAttributes    = new SimpleAttributeSet();
        this.sectionAttributes      = new SimpleAttributeSet();

        sectionAttributes.addAttribute(ElementNameAttribute, SectionElementName);

        entityAttributes       = new SimpleAttributeSet();
        entityAttributes.addAttribute(ElementNameAttribute, ImageViewFactory.ENTITY_NAME);
    }

    public synchronized void insertEntity(DecodedEntity entity) {
        long memAddr = entity.getMemAddress();
        Element element = offsetMap.get(memAddr);
        int offset;
        if(element == null) {
            Entry<Long, Element> entry = offsetMap.floorEntry(memAddr);
            if(entry == null) {
                // no floor element -> insert at beginning
                element = getDefaultRootElement();
                offset = element.getStartOffset();
            } else {
                // got a floor element -> insert after it
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
            specs.add(startTag(paragraphAttributes, ElementSpec.OriginateDirection));
            MutableAttributeSet attr = new SimpleAttributeSet(entityAttributes);
            attr.addAttribute(ImageViewFactory.ENTITY_KEY, entity);
            specs.add(contentTag(attr, "e", ElementSpec.OriginateDirection));
            specs.add(endTag());

            ElementSpec[] specArr = new ElementSpec[specs.size()];
            specs.toArray(specArr);
            insert(offset, specArr);
            Element section = getParagraphElement(offset);
            offsetMap.put(memAddr, section);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    public class EntityElement extends BranchElement {
        private static final long serialVersionUID = 1L;

        public EntityElement(Element parent, AttributeSet a) {
            super(parent, a);
        }

        @Override
        public String getName() {
            return "Entity";
        }
    }
}
