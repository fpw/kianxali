package kianxali.gui.models;
import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import kianxali.decoder.Data;
import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.decoder.Operand;
import kianxali.disassembler.DataEntry;
import kianxali.disassembler.Function;
import kianxali.loader.ImageFile;
import kianxali.loader.Section;
import kianxali.util.OutputFormatter;

public class ImageDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;

    public static final String LineElementName          = "line";
    public static final String ReferenceElementName     = "reference";
    public static final String AddressElementName       = "address";
    public static final String RawBytesElementName      = "rawBytes";
    public static final String MnemonicElementName      = "mnemonic";
    public static final String OperandElementName       = "operand";
    public static final String InfoElementName          = "info";
    public static final String CommentElementName       = "comment";

    public static final String MemAddressKey            = "memAddress";
    public static final String RefAddressKey            = "refAddress";
    public static final String InstructionKey           = "instruction";

    private final MutableAttributeSet addressAttributes, lineAttributes, referenceAttributes;
    private final MutableAttributeSet rawBytesAttributes, mnemonicAttributes, operandAttributes;
    private final MutableAttributeSet infoAttributes, commentAttributes;
    private final OutputFormatter formatter;

    public ImageDocument(OutputFormatter formatter) {
        this.formatter = formatter;
        this.lineAttributes         = new SimpleAttributeSet();
        this.referenceAttributes    = new SimpleAttributeSet();
        this.addressAttributes      = new SimpleAttributeSet();
        this.rawBytesAttributes     = new SimpleAttributeSet();
        this.mnemonicAttributes     = new SimpleAttributeSet();
        this.operandAttributes      = new SimpleAttributeSet();
        this.infoAttributes         = new SimpleAttributeSet();
        this.commentAttributes      = new SimpleAttributeSet();

        // root -> {address} -> {line}

        lineAttributes.addAttribute(ElementNameAttribute,       LineElementName);
        referenceAttributes.addAttribute(ElementNameAttribute,  ReferenceElementName);
        rawBytesAttributes.addAttribute(ElementNameAttribute,   RawBytesElementName);
        addressAttributes.addAttribute(ElementNameAttribute,    AddressElementName);
        mnemonicAttributes.addAttribute(ElementNameAttribute,   MnemonicElementName);
        operandAttributes.addAttribute(ElementNameAttribute,    OperandElementName);
        infoAttributes.addAttribute(ElementNameAttribute,       InfoElementName);
        commentAttributes.addAttribute(ElementNameAttribute,    CommentElementName);
        setupStyles();
    }

    private void setupStyles() {
        StyleConstants.setFontFamily(addressAttributes,     Font.MONOSPACED);
        StyleConstants.setFontSize(addressAttributes,       12);
        StyleConstants.setForeground(referenceAttributes,   new Color(0x00, 0x64, 0x00));
        StyleConstants.setForeground(infoAttributes,        new Color(0x00, 0x00, 0xFF));
        StyleConstants.setForeground(rawBytesAttributes,    new Color(0x40, 0x40, 0x40));
        StyleConstants.setForeground(mnemonicAttributes,    new Color(0x00, 0x00, 0xCC));
        StyleConstants.setForeground(operandAttributes,     new Color(0x00, 0x00, 0xCC));
        StyleConstants.setForeground(commentAttributes,     new Color(0xAA, 0xAA, 0xAA));
    }

    private Element findFloorElement(long memAddr) {
        Element root = getDefaultRootElement();
        Element res = root;
        int first = 0;
        Integer idx = null;
        int last = root.getElementCount() - 2;

        // do a binary search to find the floor element
        while(first <= last) {
            idx = first + (last - first) / 2;
            res = root.getElement(idx);
            long curAdr = (long) res.getAttributes().getAttribute(MemAddressKey);
            if(memAddr < curAdr) {
                last = idx - 1;
            } else if(memAddr > curAdr) {
                first = idx + 1;
            } else {
                break;
            }
        }

        if(res != root) {
            long curAdr = (long) res.getAttributes().getAttribute(MemAddressKey);
            if(curAdr > memAddr) {
                if(idx > 0) {
                    return root.getElement(idx - 1);
                } else {
                    return root;
                }
            }
        }
        return res;
    }

    public Integer getOffsetForAddress(long memAddr) {
        Element floorElem = findFloorElement(memAddr);
        if(floorElem != null) {
            return floorElem.getStartOffset();
        } else {
            return null;
        }
    }

    public Long getAddressForOffset(int offset) {
        Element root = getDefaultRootElement();
        int idx = root.getElementIndex(offset);
        Element elem = root.getElement(idx);
        Long addr = (Long) elem.getAttributes().getAttribute(MemAddressKey);
        return addr;
    }

    public synchronized void updateDataEntry(long memAddr, DataEntry data) {
        Element floorElem = findFloorElement(memAddr);
        boolean isRoot = (floorElem == getDefaultRootElement());

        if(data == null) {
            // remove element if exists
            if(!isRoot) {
                long addr = (long) floorElem.getAttributes().getAttribute(MemAddressKey);
                if(addr == memAddr) {
                    removeElement(floorElem);
                }
            }
            return;
        }

        try {
            List<ElementSpec> specs = new LinkedList<>();
            specs.add(endTag());
            MutableAttributeSet adrAttributes = new SimpleAttributeSet(addressAttributes);
            adrAttributes.addAttribute(MemAddressKey, memAddr);
            specs.add(startTag(adrAttributes));

            int startSize = specs.size();
            addImageStart(memAddr, data.getStartImageFile(), specs);
            addSectionEnd(memAddr, data.getEndSection(), specs);
            addSectionStart(memAddr, data.getStartSection(), specs);
            addFunctionStart(memAddr, data.getStartFunction(), specs);

            // only display references to functions or data
            if(data.getStartFunction() != null || !(data.getEntity() instanceof Instruction)) {
                addReferences(memAddr, data.getReferences(), specs);
            }
            addEntity(memAddr, data.getEntity(), data.getComment(), data.getAttachedData(), specs);
            addFunctionEnd(memAddr, data.getEndFunction(), specs);

            if(specs.size() == startSize) {
                // nothing was actually added
                return;
            }
            specs.add(endTag());

            if(isRoot) {
                ElementSpec[] specArr = new ElementSpec[specs.size()];
                specs.toArray(specArr);
                insert(0, specArr);
            } else {
                long addr = (long) floorElem.getAttributes().getAttribute(MemAddressKey);
                int offset = floorElem.getEndOffset();
                if(addr == memAddr) {
                    offset = floorElem.getStartOffset();
                    removeElement(floorElem);
                }
                if(getLength() > 0) {
                    // end current section if there was content
                    specs.add(0, endTag());
                }
                ElementSpec[] specArr = new ElementSpec[specs.size()];
                specs.toArray(specArr);
                insert(offset, specArr);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void startLine(long memAddr, List<ElementSpec> specs) {
        String address = String.format("%08X\t", memAddr);
        specs.add(startTag(lineAttributes));

        SimpleAttributeSet attr = new SimpleAttributeSet(infoAttributes);
        StyleConstants.setForeground(attr, new Color(0x00, 0x99, 0x00));
        specs.add(contentTag(attr, address));
    }

    private void endLine(List<ElementSpec> specs) {
        specs.add(contentTag(infoAttributes, "\n"));
        specs.add(endTag());
    }

    private void addImageStart(long memAddr, ImageFile imageFile, List<ElementSpec> specs) {
        if(imageFile == null) {
            return;
        }
        startLine(memAddr, specs);
        specs.add(contentTag(infoAttributes, "; Image file start"));
        endLine(specs);
        startLine(memAddr, specs);
        specs.add(contentTag(infoAttributes, "; Image name: " + imageFile.getFileName()));
        endLine(specs);
        startLine(memAddr, specs);
        specs.add(contentTag(infoAttributes, "; Entry point: " + formatter.formatAddress(imageFile.getCodeEntryPointMem())));
        endLine(specs);
    }

    private void addSectionStart(long memAddr, Section startSection, List<ElementSpec> specs) {
        if(startSection == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Section '%s' starts", startSection.getName());
        specs.add(contentTag(infoAttributes, line));
        endLine(specs);
    }

    private void addSectionEnd(long memAddr, Section endSection, List<ElementSpec> specs) {
        if(endSection == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Section '%s' ends", endSection.getName());
        specs.add(contentTag(infoAttributes, line));
        endLine(specs);
    }

    private void addFunctionStart(long memAddr, Function fun, List<ElementSpec> specs) {
        if(fun == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Function %s starts", fun.getName());
        specs.add(contentTag(infoAttributes, line));
        endLine(specs);
    }

    private void addReferences(long memAddr, Set<DataEntry> references, List<ElementSpec> specs) {
        if(references.size() == 0) {
            return;
        }
        startLine(memAddr, specs);
        specs.add(contentTag(referenceAttributes, "; Referenced by: "));
        for(DataEntry ref : references) {
            MutableAttributeSet attr = new SimpleAttributeSet(referenceAttributes);
            attr.addAttribute(RefAddressKey, ref.getAddress());
            specs.add(contentTag(attr, String.format("%08X ", ref.getAddress())));
        }
        endLine(specs);
    }

    private void addFunctionEnd(long memAddr, Function fun, List<ElementSpec> specs) {
        if(fun == null) {
            return;
        }
        startLine(memAddr, specs);
        String line = String.format("; Function %s ends", fun.getName());
        specs.add(contentTag(infoAttributes, line));
        endLine(specs);
    }

    private void addEntity(long memAddr, DecodedEntity entity, String comment, Data dataRef, List<ElementSpec> specs) {
        if(entity == null) {
            return;
        }

        startLine(memAddr, specs);
        if(entity instanceof Instruction) {
            Instruction inst = (Instruction) entity;
            List<Operand> operands = inst.getOperands();
            if(formatter.shouldIncludeRawBytes()) {
                String raw = String.format("%-20s ", OutputFormatter.formatByteString(inst.getRawBytes()));
                specs.add(contentTag(rawBytesAttributes, raw));
            }

            String mnemo = formatter.formatMnemonic(inst.getMnemonic()) + ((operands.size() > 0) ? " " : "");
            SimpleAttributeSet attr = new SimpleAttributeSet(mnemonicAttributes);
            attr.addAttribute(InstructionKey, inst);
            specs.add(contentTag(attr, mnemo));
            for(int i = 0; i < operands.size(); i++) {
                Operand op = operands.get(i);
                String opString = op.asString(formatter) + ((i < operands.size() - 1) ? ", " : "");
                specs.add(contentTag(operandAttributes, opString));
            }
        } else if(entity instanceof Data) {
            String dataLine = entity.asString(formatter);
            specs.add(contentTag(operandAttributes, dataLine));
        }

        if(dataRef != null) {
            specs.add(contentTag(commentAttributes, " -> " + dataRef.asString(formatter)));
        }

        if(comment != null) {
            specs.add(contentTag(commentAttributes, " ; " + comment));
        }

        endLine(specs);
    }

    private ElementSpec startTag(MutableAttributeSet attr) {
        ElementSpec res = new ElementSpec(attr, ElementSpec.StartTagType);
        res.setDirection(ElementSpec.OriginateDirection);
        return res;
    }

    private ElementSpec contentTag(MutableAttributeSet attr, String str) {
        char[] line = str.toCharArray();
        ElementSpec res = new ElementSpec(attr, ElementSpec.ContentType, line, 0, line.length);
        res.setDirection(ElementSpec.OriginateDirection);
        return res;
    }

    private ElementSpec endTag() {
        return new ElementSpec(null, ElementSpec.EndTagType);
    }
}
