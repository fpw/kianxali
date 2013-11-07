package org.solhost.folko.dasm.xml;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.solhost.folko.dasm.instructions.x86.CPUMode;
import org.solhost.folko.dasm.xml.OperandDesc.AddressType;
import org.solhost.folko.dasm.xml.OperandDesc.DirectGroup;
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
    private static XMLParser instance;
    private final OpcodeHandler[] oneByte, twoByte;
    private final Map<String, Set<OpcodeHandler>> mnemonics;

    // parsing stuff
    private OpcodeHandler currentHandler;
    private OpcodeOpts currentModeOpts;
    private Syntax currentSyntax;
    private OperandDesc currentOpDesc;
    private Short opcdExt;
    private boolean inOneByte, inTwoByte, inSyntax, inMnem;
    private boolean inSrc, inDst, inA, inT, inOpcdExt;

    private XMLParser() {
        this.mnemonics = new HashMap<>();
        this.oneByte = new OpcodeHandler[256];
        this.twoByte = new OpcodeHandler[256];
    }

    public static void init(String xmlPath, String dtdPath) throws SAXException, IOException {
        instance = new XMLParser();
        instance.parseXML(xmlPath, dtdPath);
    }

    public static OpcodeHandler get1ByteHandler(short opcode) {
        return instance.oneByte[opcode];
    }

    public static OpcodeHandler get2ByteHandler(short opcode) {
        return instance.twoByte[opcode];
    }

    public static Set<OpcodeHandler> getSyntaxes(String mnemonic) {
        Set<OpcodeHandler> ours = instance.mnemonics.get(mnemonic);
        if(ours != null) {
            return new HashSet<>(ours);
        } else {
            return null;
        }
    }

    private void parseXML(String xmlPath, String dtdPath) throws SAXException, IOException {
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
                    currentModeOpts = new OpcodeOpts();
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
                case "opcd_ext":
                    inOpcdExt = true;
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
                        currentOpDesc.usageType = UsageType.SOURCE;
                        parseOperandAtts(currentOpDesc, atts);
                        inSrc = true;
                    }
                    break;
                case "dst":
                    if(inSyntax) {
                        currentOpDesc = new OperandDesc();
                        currentOpDesc.usageType = UsageType.DEST;
                        parseOperandAtts(currentOpDesc, atts);
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
                    currentOpDesc.adrType = parseAddressType(val);
                } else if(inT) {
                    currentOpDesc.operType = parseOperandType(val);
                } else if(inOpcdExt) {
                    opcdExt = Short.parseShort(val);
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
                case "syntax":
                    Set<OpcodeHandler> syntaxes = mnemonics.get(currentSyntax.getMnemonic());
                    if(syntaxes == null) {
                        syntaxes = new HashSet<>();
                        mnemonics.put(currentSyntax.getMnemonic(), syntaxes);
                    }
                    syntaxes.add(currentHandler);

                    currentModeOpts.addSyntax(currentSyntax);
                    currentSyntax = null;
                    inSyntax = false;
                    break;
                case "opcd_ext":
                    inOpcdExt = false;
                    break;
                case "entry":
                    if(opcdExt == null) {
                        currentHandler.addBaseOptions(currentModeOpts.mode, currentModeOpts);
                    } else {
                        currentHandler.addExtensionOptions(currentModeOpts.mode, opcdExt, currentModeOpts);
                    }
                    currentModeOpts = null;
                    opcdExt = null;
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

    private AddressType parseAddressType(String val) {
        switch(val) {
        case "A":   return AddressType.DIRECT;
        case "BA":  return AddressType.DS_EAX_RAX;
        case "BB":  return AddressType.DS_EAX_AL_RBX;
        case "BD":  return AddressType.DS_EDI_RDI;
        case "C":   return AddressType.CONTROL;
        case "D":   return AddressType.DEBUG;
        case "E":   return AddressType.MOD_RM_M;
        case "F":   return AddressType.FLAGS;
        case "ES":  return AddressType.MOD_RM_M_FPU;
        case "EST": return AddressType.MOD_RM_R_FPU;
        case "G":   return AddressType.MOD_RM_R;
        case "H":   return AddressType.MOD_RM_R_FORCE;
        case "I":   return AddressType.IMMEDIATE;
        case "J":   return AddressType.RELATIVE;
        case "M":   return AddressType.MOD_RM_M_FORCE;
        case "N":   return AddressType.MOD_RM_M_MMX;
        case "O":   return AddressType.OFFSET;
        case "P":   return AddressType.MOD_RM_R_MMX;
        case "Q":   return AddressType.MOD_RM_MMX;
        case "R":   return AddressType.MOD_RM_R_FORCE2;
        case "S":   return AddressType.MOD_RM_R_SEG;
        case "S2":  return AddressType.SEGMENT2;
        case "S30": return AddressType.SEGMENT30;
        case "S33": return AddressType.SEGMENT33;
        case "SC":  return AddressType.STACK;
        case "T":   return AddressType.TEST;
        case "U":   return AddressType.MOD_RM_M_XMM;
        case "V":   return AddressType.MOD_RM_R_XMM;
        case "W":   return AddressType.MOD_RM_XMM;
        case "X":   return AddressType.DS_ESI_RSI;
        case "Y":   return AddressType.ES_EDI_RDI;
        case "Z":   return AddressType.LEAST_REG;
        default:
            System.err.println("Unknown address type: " + val);
            return null;
        }
    }

    private OperandType parseOperandType(String val) {
        switch(val) {
        case "a":   return OperandType.TWO_INDICES;
        case "b":   return OperandType.BYTE;
        case "bcd": return OperandType.BCD;
        case "bs":  return OperandType.BYTE_SGN;
        case "bss": return OperandType.BYTE_STACK;
        case "d":   return OperandType.DWORD;
        case "da":  return OperandType.DWORD_ADR;
        case "di":  return OperandType.DWORD_INT_FPU;
        case "do":  return OperandType.DWORD_OPS;
        case "dr":  return OperandType.DOUBLE_FPU;
        case "dq":  return OperandType.DQWORD;
        case "dqa": return OperandType.DWORD_QWORD_ADR;
        case "dq ": return OperandType.DQWORD;
        case "dqp": return OperandType.DWORD_QWORD;
        case "e":   return OperandType.FPU_ENV;
        case "er":  return OperandType.REAL_EXT_FPU;
        case "p":   return OperandType.POINTER;
        case "pi":  return OperandType.QWORD_MMX;
        case "pd":  return OperandType.DOUBLE_128;
        case "ptp": return OperandType.POINTER_REX;
        case "ps":  return OperandType.SINGLE_128;
        case "psq": return OperandType.SINGLE_64;
        case "q":   return OperandType.QWORD;
        case "qa":  return OperandType.QWORD_ADR;
        case "qi":  return OperandType.QWORD_FPU;
        case "qs":  return OperandType.QWORD_STACK;
        case "qp":  return OperandType.QWORD_REX;
        case "s":   return OperandType.PSEUDO_DESC;
        case "sr":  return OperandType.REAL_SINGLE_FPU;
        case "st":  return OperandType.FPU_STATE;
        case "sd":  return OperandType.SCALAR_DOUBLE;
        case "ss":  return OperandType.SCALAR_SINGLE;
        case "stx": return OperandType.FPU_SIMD_STATE;
        case "vds": return OperandType.WORD_DWORD_S64;
        case "vq":  return OperandType.QWORD_WORD;
        case "vqp": return OperandType.WORD_DWORD_64;
        case "v":   return OperandType.WORD_DWORD;
        case "va":  return OperandType.WORD_DWORD_ADR;
        case "vs":  return OperandType.WORD_DWORD_STACK;
        case "w":   return OperandType.WORD;
        case "wa":  return OperandType.WORD_ADR;
        case "ws":  return OperandType.WORD_STACK;
        case "wo":  return OperandType.WORD_OPS;
        case "wi":  return OperandType.WORD_FPU;
        default:
            System.err.println("Unknown operand type: " + val);
            return null;
        }
    }

    private void parseOperandAtts(OperandDesc currentOpDesc, Attributes atts) {
        boolean hasGroup = false;
        for(int i = 0; i < atts.getLength(); i++) {
            String key = atts.getLocalName(i);
            String val = atts.getValue(i);
            switch(key) {
            case "group":
                hasGroup = true;
                switch(val) {
                case "gen":     currentOpDesc.directGroup = DirectGroup.GENERIC; break;
                case "seg":     currentOpDesc.directGroup = DirectGroup.SEGMENT; break;
                case "x87fpu":  currentOpDesc.directGroup = DirectGroup.X87FPU; break;
                case "mmx":     currentOpDesc.directGroup = DirectGroup.MMX; break;
                case "xmm":     currentOpDesc.directGroup = DirectGroup.XMM; break;
                case "msr":     currentOpDesc.directGroup = DirectGroup.MSR; break;
                case "systabp": currentOpDesc.directGroup = DirectGroup.SYSTABP; break;
                case "ctrl":    currentOpDesc.directGroup = DirectGroup.CONTROL; break;
                case "debug":   currentOpDesc.directGroup = DirectGroup.DEBUG; break;
                case "xcr":     currentOpDesc.directGroup = DirectGroup.XCR; break;
                default:
                    System.err.println("unknown group: " + val);
                }
                break;
            case "type":
                currentOpDesc.operType = parseOperandType(val);
                break;
            case "displayed":
                if(val.equals("no")) {
                    currentOpDesc.indirect = true;
                }
                break;
            case "nr":
                currentOpDesc.numForGroup = Long.parseLong(val, 16);
                break;
            case "address":
                currentOpDesc.adrType = parseAddressType(val);
                break;
            case "depend":
                if(val.equals("no")) {
                    currentOpDesc.depends = false;
                } else {
                    // yes is default if not given
                    currentOpDesc.depends = true;
                }
                break;
            default:
                System.err.println("Unknown key: " + key);
            }
        }
        if(currentOpDesc.adrType == null && hasGroup) {
            currentOpDesc.adrType = AddressType.GROUP;
        }
    }

    public OpcodeHandler[] getOneByteHandlers() {
        return oneByte;
    }

    public OpcodeHandler[] getTwoByteHandlers() {
        return twoByte;
    }
}
