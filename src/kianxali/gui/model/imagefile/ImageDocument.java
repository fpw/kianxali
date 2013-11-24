package kianxali.gui.model.imagefile;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.decoder.Operand;
import kianxali.disassembler.DataEntry;
import kianxali.image.ImageFile;
import kianxali.image.Section;
import kianxali.util.OutputFormatter;

public class ImageDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;
    public static final String LineElementName = "line";
    public static final String MnemonicElementName = "mnemonic";
    public static final String OperandElementName = "operand";
    public static final String InfoElementName = "info";
    public static final String CommentElementName = "comment";
    private final NavigableMap<Long, Element> offsetMap;
    private final MutableAttributeSet paragraphAttributes, sectionAttributes;
    private final MutableAttributeSet addressAttributes, mnemonicAttributes, operandAttributes, infoAttributes, commentAttributes;
    private final OutputFormatter formatter;

    public ImageDocument(OutputFormatter formatter) {
        this.formatter = formatter;
        // PriorityQueue:           ~ 128s
        // Hoechste Adresse zuerst  > 300s
        // FIFO                     > 300s
        this.offsetMap              = new TreeMap<Long, Element>();
        this.addressAttributes      = new SimpleAttributeSet();
        this.mnemonicAttributes     = new SimpleAttributeSet();
        this.operandAttributes      = new SimpleAttributeSet();
        this.paragraphAttributes    = new SimpleAttributeSet();
        this.sectionAttributes      = new SimpleAttributeSet();
        this.infoAttributes         = new SimpleAttributeSet();
        this.commentAttributes      = new SimpleAttributeSet();

        mnemonicAttributes.addAttribute(ElementNameAttribute, MnemonicElementName);
        operandAttributes.addAttribute(ElementNameAttribute, OperandElementName);
        sectionAttributes.addAttribute(ElementNameAttribute, LineElementName);
        infoAttributes.addAttribute(ElementNameAttribute, InfoElementName);
        commentAttributes.addAttribute(ElementNameAttribute, CommentElementName);
        setupStyles();
    }

    private void setupStyles() {
        StyleConstants.setFontFamily(paragraphAttributes, "Courier");
        StyleConstants.setForeground(infoAttributes, new Color(0x00, 0x00, 0xFF));
        StyleConstants.setForeground(addressAttributes, new Color(0x00, 0x99, 0x00));
        StyleConstants.setForeground(mnemonicAttributes, new Color(0x00, 0x00, 0xCC));
    }

    public synchronized void updateDataEntry(long memAddr, DataEntry data) {
        Element element = offsetMap.get(memAddr);
        int offset;
        boolean append = false, atStart = false;
        if(element == null) {
            Entry<Long, Element> entry = offsetMap.floorEntry(memAddr);
            if(entry == null) {
                // no floor element -> insert at beginning
                element = getDefaultRootElement();
                offset = element.getStartOffset();
                atStart = true;
            } else {
                // got a floor element -> insert after it
                element = entry.getValue();
                offset = element.getEndOffset();
                append = true;
            }
        } else {
            // remove old paragraph
            offset = element.getStartOffset();
            removeElement(element);
        }

        if(data == null) {
            return;
        }

        try {
            List<ElementSpec> specs = new LinkedList<>();
            specs.add(endTag());
            if(append || (atStart && getLength() > 0)) {
                specs.add(endTag());
            }
            specs.add(startTag(sectionAttributes, ElementSpec.OriginateDirection));
            int startSize = specs.size();
            addImageStart(memAddr, data.getStartImageFile(), specs);
            addSectionStart(memAddr, data.getStartSection(), specs);
            addEntity(memAddr, data.getEntity(), data.getComment(), specs);
            addSectionEnd(memAddr, data.getEndSection(), specs);

            if(specs.size() == startSize) {
                // nothing was actually added
                return;
            }
            specs.add(endTag());

            ElementSpec[] specArr = new ElementSpec[specs.size()];
            specs.toArray(specArr);
            insert(offset, specArr);
            Element section = getParagraphElement(offset).getParentElement();
            offsetMap.put(memAddr, section);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void startLine(long memAddr, List<ElementSpec> specs) {
        specs.add(startTag(paragraphAttributes, ElementSpec.OriginateDirection));
        String address = String.format("%08X\t", memAddr);
        specs.add(contentTag(addressAttributes, address, ElementSpec.OriginateDirection));
    }

    private void endLine(List<ElementSpec> specs) {
        specs.add(endTag());
    }

    private void addImageStart(long memAddr, ImageFile startImageFile, List<ElementSpec> specs) {
        if(startImageFile == null) {
            return;
        }
        startLine(memAddr, specs);
        specs.add(contentTag(infoAttributes, "; Image file start", ElementSpec.OriginateDirection));
        endLine(specs);
    }

    private void addSectionStart(long memAddr, Section startSection, List<ElementSpec> specs) {
        if(startSection == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Section '%s' start", startSection.getName());
        specs.add(contentTag(infoAttributes, line, ElementSpec.OriginateDirection));
        endLine(specs);
    }

    private void addSectionEnd(long memAddr, Section endSection, List<ElementSpec> specs) {
        if(endSection == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Section '%s' ends", endSection.getName());
        specs.add(contentTag(infoAttributes, line, ElementSpec.OriginateDirection));
        endLine(specs);
    }

    private void addEntity(long memAddr, DecodedEntity entity, String comment, List<ElementSpec> specs) {
        if(entity == null) {
            return;
        }
        startLine(memAddr, specs);
        if(entity instanceof Instruction) {
            Instruction inst = (Instruction) entity;
            List<Operand> operands = inst.getOperands();
            String mnemo = inst.getMnemonicString(formatter) + ((operands.size() > 0) ? " " : "");
            specs.add(contentTag(mnemonicAttributes, mnemo, ElementSpec.OriginateDirection));
            for(int i = 0; i < operands.size(); i++) {
                Operand op = operands.get(i);
                String opString = op.asString(formatter) + ((i < operands.size() - 1) ? ", " : "");
                specs.add(contentTag(mnemonicAttributes, opString, ElementSpec.OriginateDirection));
            }
        }
        if(comment != null) {
            specs.add(contentTag(commentAttributes, comment, ElementSpec.OriginateDirection));
        }
        endLine(specs);
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

}
