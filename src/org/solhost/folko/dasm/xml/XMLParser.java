package org.solhost.folko.dasm.xml;

import java.io.FileReader;
import java.io.IOException;

import org.solhost.folko.dasm.instructions.x86.CPUMode;
import org.solhost.folko.dasm.xml.OperandDesc.AddressType;
import org.solhost.folko.dasm.xml.OperandDesc.OperandType;
import org.solhost.folko.dasm.xml.OperandDesc.UsageType;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLParser {
    private final OpcodeHandler[] oneByte, twoByte;
    private OpcodeHandler currentHandler;
    private ModeOpts currentModeOpts;
    private Syntax currentSyntax;
    private OperandDesc currentOpDesc;
    private boolean inOneByte, inTwoByte, inSyntax, inMnem, inSrc, inDst, inA, inT;

    public XMLParser() {
        this.oneByte = new OpcodeHandler[256];
        this.twoByte = new OpcodeHandler[256];
    }

    public void parseXML(String xmlPath, String dtdPath) throws SAXException, IOException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        FileReader reader = new FileReader(xmlPath);
        InputSource source = new InputSource(reader);
        source.setSystemId(dtdPath);
        xmlReader.setContentHandler(new ContentHandler() {
            public void setDocumentLocator(Locator locator) { }
            public void startPrefixMapping(String prefix, String uri) throws SAXException { }
            public void endPrefixMapping(String prefix) throws SAXException { }
            public void skippedEntity(String name) throws SAXException { }
            public void processingInstruction(String target, String data) throws SAXException { }
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { }
            public void startDocument() throws SAXException { }
            public void endDocument() throws SAXException { }

            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                switch(localName) {
                case "one-byte":
                    inOneByte = true;
                    inTwoByte = false;
                    break;
                case "two-byte":
                    inOneByte = false;
                    inTwoByte = true;
                    break;
                case "pri_opcd":
                    short opcode = Short.parseShort(atts.getValue("value"), 16);
                    currentHandler = new OpcodeHandler(inTwoByte, opcode);
                    break;
                case "entry":
                    currentModeOpts = new ModeOpts();
                    String modeStr = atts.getValue("mode");
                    if(modeStr == null) {
                        modeStr = "r";
                    }
                    switch(modeStr) {
                    case "r": currentModeOpts.mode = CPUMode.REAL; break;
                    case "p": currentModeOpts.mode = CPUMode.PROTECTED; break;
                    case "e": currentModeOpts.mode = CPUMode.LONG; break;
                    case "s": currentModeOpts.mode = CPUMode.SMM; break;
                    default: throw new UnsupportedOperationException("invalid mode: " + modeStr);
                    }

                    if("1".equals(atts.getValue("direction"))) {
                        currentModeOpts.direction = true;
                    }

                    if("1".equals(atts.getValue("sign-ext"))) {
                        currentModeOpts.sgnExt = true;
                    }

                    if("1".equals(atts.getValue("op_size"))) {
                        currentModeOpts.opSize = true;
                    }

                    if("yes".equals(atts.getValue("r"))) {
                        currentModeOpts.modRM = true;
                    }

                    if("yes".equals(atts.getValue("lock"))) {
                        currentModeOpts.lock = true;
                    }

                    String attr = atts.getValue("attr");
                    if(attr == null) {
                        attr = "";
                    }
                    if(attr.contains("invd")) {
                        currentModeOpts.invalid = true;
                    }
                    if(attr.contains("undef")) {
                        currentModeOpts.undefined = true;
                    }

                    String tttnStr = atts.getValue("tttn");
                    if(tttnStr != null) {
                        currentModeOpts.tttn = Byte.parseByte(tttnStr, 2);
                    }
                    break;
                case "syntax":
                    currentSyntax = new Syntax();
                    inSyntax = true;
                    break;
                case "mnem":
                    if(inSyntax) {
                        inMnem = true;
                    }
                    break;
                case "src":
                    if(inSyntax) {
                        currentOpDesc = new OperandDesc();
                        currentOpDesc.opType = UsageType.SOURCE;
                        inSrc = true;
                    }
                    break;
                case "dst":
                    if(inSyntax) {
                        currentOpDesc = new OperandDesc();
                        currentOpDesc.opType = UsageType.DEST;
                        inDst = true;
                    }
                    break;
                case "a":
                    if(inSrc || inDst) {
                        // addressing mode
                        inA = true;
                    }
                    break;
                case "t":
                    if(inSrc || inDst) {
                        // operand type
                        inT = true;
                    }
                    break;
                }
            }

            public void characters(char[] ch, int start, int length) throws SAXException {
                String val = new String(ch, start, length);
                if(inMnem) {
                    currentSyntax.setMnemonic(val);
                } else if(inA) {
                    switch(val) {
                    case "A":   currentOpDesc.adrType = AddressType.DIRECT; break;
                    case "C":   currentOpDesc.adrType = AddressType.CONTROL; break;
                    case "D":   currentOpDesc.adrType = AddressType.DEBUG; break;
                    case "E":   currentOpDesc.adrType = AddressType.MOD_RM_M; break;
                    case "ES":  currentOpDesc.adrType = AddressType.MOD_RM_M_FPU; break;
                    case "EST": currentOpDesc.adrType = AddressType.MOD_RM_R_FPU; break;
                    case "G":   currentOpDesc.adrType = AddressType.MOD_RM_R; break;
                    case "H":   currentOpDesc.adrType = AddressType.MOD_RM_R_FORCE; break;
                    case "I":   currentOpDesc.adrType = AddressType.IMMEDIATE; break;
                    case "J":   currentOpDesc.adrType = AddressType.RELATIVE; break;
                    case "M":   currentOpDesc.adrType = AddressType.MOD_RM_M_FORCE; break;
                    case "N":   currentOpDesc.adrType = AddressType.MOD_RM_M_MMX; break;
                    case "O":   currentOpDesc.adrType = AddressType.OFFSET; break;
                    case "P":   currentOpDesc.adrType = AddressType.MOD_RM_R_MMX; break;
                    case "Q":   currentOpDesc.adrType = AddressType.MOD_RM_MMX; break;
                    case "R":   currentOpDesc.adrType = AddressType.MOD_RM_R_FORCE2; break;
                    case "S":   currentOpDesc.adrType = AddressType.MOD_RM_R_SEG; break;
                    case "T":   currentOpDesc.adrType = AddressType.TEST; break;
                    case "U":   currentOpDesc.adrType = AddressType.MOD_RM_M_XMM; break;
                    case "V":   currentOpDesc.adrType = AddressType.MOD_RM_R_XMM; break;
                    case "W":   currentOpDesc.adrType = AddressType.MOD_RM_XMM; break;
                    case "Z":   currentOpDesc.adrType = AddressType.LEAST_REG; break;
                    default:
                        System.err.println("Unknown address type: " + val);
                    }
                } else if(inT) {
                    switch(val) {
                    case "a":   currentOpDesc.operType = OperandType.TWO_INDICES; break;
                    case "b":   currentOpDesc.operType = OperandType.BYTE; break;
                    case "bcd": currentOpDesc.operType = OperandType.BCD; break;
                    case "bs":  currentOpDesc.operType = OperandType.BYTE_SGN; break;
                    case "bss": currentOpDesc.operType = OperandType.BYTE_STACK; break;
                    case "d":   currentOpDesc.operType = OperandType.DWORD; break;
                    case "di":  currentOpDesc.operType = OperandType.DWORD_INT_FPU; break;
                    case "dr":  currentOpDesc.operType = OperandType.DOUBLE_FPU; break;
                    case "dq":  currentOpDesc.operType = OperandType.DQWORD; break;
                    case "dq ": currentOpDesc.operType = OperandType.DQWORD; break;
                    case "dqp": currentOpDesc.operType = OperandType.DWORD_QWORD; break;
                    case "e":   currentOpDesc.operType = OperandType.FPU_ENV; break;
                    case "er":  currentOpDesc.operType = OperandType.REAL_EXT_FPU; break;
                    case "p":   currentOpDesc.operType = OperandType.POINTER; break;
                    case "pi":  currentOpDesc.operType = OperandType.QWORD_MMX; break;
                    case "pd":  currentOpDesc.operType = OperandType.DOUBLE_128; break;
                    case "ptp": currentOpDesc.operType = OperandType.POINTER_REX; break;
                    case "ps":  currentOpDesc.operType = OperandType.SINGLE_128; break;
                    case "psq": currentOpDesc.operType = OperandType.SINGLE_64; break;
                    case "q":   currentOpDesc.operType = OperandType.QWORD; break;
                    case "qi":  currentOpDesc.operType = OperandType.QWORD_FPU; break;
                    case "qp":  currentOpDesc.operType = OperandType.QWORD_REX; break;
                    case "s":   currentOpDesc.operType = OperandType.PSEUDO_DESC; break;
                    case "sr":  currentOpDesc.operType = OperandType.REAL_SINGLE_FPU; break;
                    case "st":  currentOpDesc.operType = OperandType.FPU_STATE; break;
                    case "sd":  currentOpDesc.operType = OperandType.SCALAR_DOUBLE; break;
                    case "ss":  currentOpDesc.operType = OperandType.SCALAR_SINGLE; break;
                    case "stx": currentOpDesc.operType = OperandType.FPU_SIMD_STATE; break;
                    case "vds": currentOpDesc.operType = OperandType.WORD_DWORD_S64; break;
                    case "vq":  currentOpDesc.operType = OperandType.QWORD_WORD; break;
                    case "vqp": currentOpDesc.operType = OperandType.WORD_DWORD_64; break;
                    case "v":   currentOpDesc.operType = OperandType.WORD_DWORD; break;
                    case "vs":  currentOpDesc.operType = OperandType.WORD_DWORD_STACK; break;
                    case "w":   currentOpDesc.operType = OperandType.WORD; break;
                    case "wi":  currentOpDesc.operType = OperandType.WORD_FPU; break;
                    default:
                        System.err.println("Unknown operand type: " + val);
                    }
                }
            }

            public void endElement(String uri, String localName, String qName) throws SAXException {
                switch(localName) {
                case "one-byte":
                    inTwoByte = false;
                    break;
                case "two-byte":
                    inOneByte = false;
                    break;
                case "pri_opcd":
                    short opcode = currentHandler.getOpcode();
                    if(inOneByte) {
                        if(oneByte[opcode] != null) {
                            System.err.println(String.format("one-byte opcode %02X already present", opcode));
                        } else {
                            oneByte[opcode] = currentHandler;
                        }
                    } else if(inTwoByte) {
                        if(twoByte[opcode] != null) {
                            System.err.println(String.format("two-byte opcode %02X already present", opcode));
                        } else {
                            twoByte[opcode] = currentHandler;
                        }
                    }
                    currentHandler = null;
                    break;
                case "entry":
                    currentHandler.addModeAttributes(currentModeOpts.mode, currentModeOpts);
                    currentModeOpts = null;
                    break;
                case "syntax":
                    currentHandler.addSyntax(currentSyntax);
                    currentSyntax = null;
                    inSyntax = false;
                    break;
                case "mnem":
                    inMnem = false;
                    break;
                case "src":
                    currentSyntax.addOperand(currentOpDesc);
                    currentOpDesc = null;
                    inSrc = false;
                    break;
                case "dst":
                    currentSyntax.addOperand(currentOpDesc);
                    currentOpDesc = null;
                    inDst = false;
                    break;
                case "a":
                    inA = false;
                    break;
                case "t":
                    inT = false;
                    break;
                }
            }
        });
        xmlReader.parse(source);
        reader.close();
    }

    public OpcodeHandler[] getOneByteHandlers() {
        return oneByte;
    }

    public OpcodeHandler[] getTwoByteHandlers() {
        return twoByte;
    }
}
