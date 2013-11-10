package org.solhost.folko.dasm.xml;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.solhost.folko.dasm.instructions.x86.CPUMode;
import org.solhost.folko.dasm.xml.OpcodeOpts.Extension;
import org.solhost.folko.dasm.xml.OpcodeOpts.OpcodeGroup;
import org.solhost.folko.dasm.xml.OpcodeOpts.Prefix;
import org.solhost.folko.dasm.xml.OpcodeOpts.Processor;
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
    private Processor inheritProcStart;
    private boolean inOneByte, inTwoByte, inSyntax, inMnem;
    private boolean inSrc, inDst, inA, inT, inOpcdExt, inGroup, inInstrExt;
    private boolean inProcStart, inProcEnd, in2ndOpcode, inPref;

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
                case "x86reference":
                    break;
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
                    fillEntry(currentModeOpts, atts);
                    break;
                case "opcd_ext":
                    inOpcdExt = true;
                    break;
                case "syntax":
                    // TODO: attribute mod: mem|nomem
                    currentSyntax = new Syntax();
                    inSyntax = true;
                    break;
                case "mnem":
                    // TODO: attribute sug: yes iff mnem is only suggested and no official
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
                case "sup": // superscript in <brief>, e.g. for x^2
                case "sub": // subscript in <brief>, e.g. for log_x
                    break;
                case "grp1":
                case "grp2":
                case "grp3":
                    inGroup = true;
                    break;
                case "instr_ext":
                    inInstrExt = true;
                    break;
                case "proc_start":
                    // TODO atts post, lat_step: unknown purpose
                    inProcStart = true;
                    break;
                case "proc_end":
                    inProcEnd = true;
                    break;
                case "sec_opcd":
                    // attribute escape: yes can be ignored because we already know if opcode is 2bytes
                    in2ndOpcode = true;
                    break;
                case "pref":
                    inPref = true;
                    break;
                case "def_f":
                case "f_vals":
                case "test_f":
                case "modif_f":
                case "undef_f":
                case "def_f_fpu":
                case "f_vals_fpu":
                case "modif_f_fpu":
                case "undef_f_fpu":
                    // TODO: flag support
                    break;
                case "note":
                case "det":
                case "brief":
                case "gen_note":
                case "gen_notes":
                case "ring_note":
                case "ring_notes":
                    // TODO: documentation
                    break;
                default:
                    System.err.println("Unhandled tag: " + localName);
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
                } else if(inProcStart) {
                    Processor proc = parseProcessor(val);
                    if(currentModeOpts != null) {
                        currentModeOpts.setStartProcessor(proc);
                    } else {
                        inheritProcStart = proc;
                    }
                } else if(inProcEnd) {
                    if(currentModeOpts.supportedProcessors.size() == 0) {
                        // there was no proc_start -> default is 8086
                        currentModeOpts.setStartProcessor(Processor.I8086);
                    }
                    currentModeOpts.setEndProcessor(parseProcessor(val));
                } else if(inOpcdExt) {
                    opcdExt = Short.parseShort(val);
                } else if(inGroup) {
                    currentModeOpts.addGroup(parseOpcodeGroup(currentModeOpts.instrExt, currentModeOpts.groups, val));
                } else if(inInstrExt) {
                    currentModeOpts.instrExt = parseInstrExt(val);
                } else if(in2ndOpcode) {
                    currentModeOpts.secondOpcode = Short.parseShort(val, 16);
                } else if(inPref) {
                    currentModeOpts.prefix = parsePrefix(val);
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
                            System.err.println(String.format("two-byte opcode 0F%02X already present", opcode));
                        } else {
                            twoByte[opcode] = currentHandler;
                        }
                    }
                    inheritProcStart = null;
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
                case "proc_start":
                    inProcStart = false;
                    break;
                case "proc_end":
                    inProcEnd = false;
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
                    if(currentModeOpts.supportedProcessors.size() == 0) {
                        if(inheritProcStart != null) {
                            currentModeOpts.setStartProcessor(inheritProcStart);
                        } else {
                            currentModeOpts.setStartProcessor(Processor.I8086);
                        }
                    }
                    currentModeOpts = null;
                    opcdExt = null;
                    break;
                case "sec_opcd":
                    in2ndOpcode = false;
                    break;
                case "pref":
                    inPref = false;
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
                case "grp1":
                case "grp2":
                case "grp3":
                    inGroup = false;
                    break;
                case "instr_ext":
                    inInstrExt = false;
                    break;
                }
            }
        });
        xmlReader.parse(source);
        reader.close();
    }

    private void fillEntry(OpcodeOpts currentModeOpts, Attributes atts) {
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

    private Processor parseProcessor(String val) {
        switch(val) {
        case "00": return Processor.I8086;
        case "01": return Processor.I80186;
        case "02": return Processor.I80286;
        case "03": return Processor.I80386;
        case "04": return Processor.I80486;
        case "05": return Processor.PENTIUM;
        case "06": return Processor.PENTIUM_MMX;
        case "07": return Processor.PENTIUM_MMX;
        case "08": return Processor.PENTIUM_II;
        case "09": return Processor.PENTIUM_III;
        case "10": return Processor.PENTIUM_IV;
        case "11": return Processor.CORE_1;
        case "12": return Processor.CORE_2;
        case "13": return Processor.CORE_I7;
        case "99": return Processor.ITANIUM;
        default:
            System.err.println("Unknown processor entry: " + val);
            return null;
        }
    }

    private Prefix parsePrefix(String val) {
        switch(val) {
        case "66": return Prefix.OPERAND_SIZE;
        case "F2": return Prefix.REP_NZ;
        case "F3": return Prefix.REP_Z;
        case "9B": return Prefix.WAIT;
        default:
            System.err.println("Unknown prefix: " + val);
            return null;
        }
    }

    private Extension parseInstrExt(String val) {
        switch(val) {
        case "mmx":     return Extension.MMX;
        case "sse1":    return Extension.SSE_1;
        case "sse2":    return Extension.SSE_2;
        case "sse3":    return Extension.SSE_3;
        case "sse41":   return Extension.SSE_4_1;
        case "sse42":   return Extension.SSE_4_2;
        case "ssse3":   return Extension.SSSE_3;
        case "vmx":     return Extension.VMX;
        case "smx":     return Extension.SMX;
        default:
            System.err.println("Unhandled instruction extension: " + val);
            return null;
        }
    }

    private OpcodeGroup parseOpcodeGroup(Extension ext, Set<OpcodeGroup> groups, String group) {
        switch (group) {
        case "prefix":
            return OpcodeGroup.PREFIX;
        case "segreg":
            if(groups.contains(OpcodeGroup.PREFIX)) {
                return OpcodeGroup.PREFIX_SEGREG;
            } else if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_SEGREGMANIPULATION;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "arith":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE1_SINGLE)) {
                return OpcodeGroup.SSE1_SINGLE_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE3_FLOAT)) {
                return OpcodeGroup.SSE3_FLOAT_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE41_INT)) {
                return OpcodeGroup.SSE41_INT_ARITHMETIC;
            } else if(groups.contains(OpcodeGroup.SSE41_FLOAT)) {
                return OpcodeGroup.SSE41_FLOAT_ARITHMETIC;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_ARITHMETIC;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "simdint":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_INT64;
            } else if(ext == Extension.SSE_2) {
                return OpcodeGroup.SSE2_INT128;
            } else if(ext == Extension.SSSE_3) {
                return OpcodeGroup.SSSE3_INT;
            } else if(ext == Extension.SSE_4_1) {
                return OpcodeGroup.SSE41_INT;
            } else if(ext == Extension.SSE_4_2) {
                return OpcodeGroup.SSE42_INT;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "shift":
            if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_SHIFT;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_SHIFT;
            } else if(ext == Extension.SSE_2) {
                // TODO: verify that this is correct
                return OpcodeGroup.SSE2_INT128_SHIFT;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "cachect":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_CACHE;
            } else if(ext == Extension.SSE_2) {
                return OpcodeGroup.SSE2_CACHE;
            } else if(ext == Extension.SSE_3) {
                return OpcodeGroup.SSE3_CACHE;
            } else if(ext == Extension.SSE_4_1) {
                return OpcodeGroup.SSE41_CACHE;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "logical":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_LOGICAL;
            } else if(groups.contains(OpcodeGroup.SSE1_SINGLE)) {
                return OpcodeGroup.SSE1_SINGLE_LOGICAL;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_LOGICAL;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_LOGICAL;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_LOGICAL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "conver":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_CONVERSION;
            } else if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_CONVERSION;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_CONVERSION;
            } else if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_CONVERSION;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_CONVERSION;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_CONVERSION;
            } else if(groups.contains(OpcodeGroup.SSE41_INT)) {
                return OpcodeGroup.SSE41_INT_CONVERSION;
            } else if(groups.contains(OpcodeGroup.SSE41_FLOAT)) {
                return OpcodeGroup.SSE41_FLOAT_CONVERSION;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "pcksclr":
            if(ext == Extension.SSE_2) {
                return OpcodeGroup.SSE2_DOUBLE;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "datamov":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE1_SINGLE)) {
                return OpcodeGroup.SSE1_SINGLE_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE3_FLOAT)) {
                return OpcodeGroup.SSE3_FLOAT_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE41_INT)) {
                return OpcodeGroup.SSE41_INT_DATAMOVE;
            } else if(groups.contains(OpcodeGroup.SSE41_FLOAT)) {
                return OpcodeGroup.SSE41_FLOAT_DATAMOVE;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_DATAMOV;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "binary":
            if(groups.contains(OpcodeGroup.GENERAL_ARITHMETIC)) {
                return OpcodeGroup.GENERAL_ARITHMETIC_BINARY;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "gen":
            return OpcodeGroup.GENERAL;
        case "shunpck":
            if(groups.contains(OpcodeGroup.SSE1_SINGLE)) {
                return OpcodeGroup.SSE1_SINGLE_SHUFFLEUNPACK;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_SHUFFLEUNPACK;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_SHUFFLEUNPACK;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "simdfp":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_SINGLE;
            } else if(ext == Extension.SSE_3) {
                return OpcodeGroup.SSE3_FLOAT;
            } else if(ext == Extension.SSE_4_1) {
                return OpcodeGroup.SSE41_FLOAT;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "compar":
            if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_COMPARISON;
            } else if(groups.contains(OpcodeGroup.SSE1_SINGLE)) {
                return OpcodeGroup.SSE1_SINGLE_COMPARISON;
            } else if(groups.contains(OpcodeGroup.SSE2_DOUBLE)) {
                return OpcodeGroup.SSE2_DOUBLE_COMPARISON;
            } else if(groups.contains(OpcodeGroup.SSE2_INT128)) {
                return OpcodeGroup.SSE2_INT128_COMPARISON;
            } else if(groups.contains(OpcodeGroup.SSE41_INT)) {
                return OpcodeGroup.SSE41_INT_COMPARISON;
            } else if(groups.contains(OpcodeGroup.SSE42_INT)) {
                return OpcodeGroup.SSE42_INT_COMPARISON;
            } else if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_COMPARISON;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "bit":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_BITMANIPULATION;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "system":
            return OpcodeGroup.SYSTEM;
        case "branch":
            if(groups.contains(OpcodeGroup.PREFIX)) {
                return OpcodeGroup.PREFIX_BRANCH;
            } else if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_BRANCH;
            } else if(groups.contains(OpcodeGroup.SYSTEM)) {
                return OpcodeGroup.SYSTEM_BRANCH;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "control":
            if(groups.contains(OpcodeGroup.PREFIX_FPU)) {
                return OpcodeGroup.PREFIX_FPU_CONTROL;
            } else if(groups.contains(OpcodeGroup.OBSOLETE)) {
                return OpcodeGroup.OBSOLETE_CONTROL;
            } else if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_CONTROL;
            } else if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_CONTROL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "stack":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_STACK;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "order":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_INSTRUCTIONORDER;
            } else if(ext == Extension.SSE_2) {
                return OpcodeGroup.SSE2_INSTRUCTIONORDER;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "sm":
            return OpcodeGroup.FPUSIMDSTATE;
        case "mxcsrsm":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_MXCSR;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "shftrot":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_SHIFTROT;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "cond":
            if(groups.contains(OpcodeGroup.GENERAL_BRANCH)) {
                return OpcodeGroup.GENERAL_BRANCH_CONDITIONAL;
            } else if(groups.contains(OpcodeGroup.PREFIX_BRANCH)) {
                return OpcodeGroup.PREFIX_BRANCH_CONDITIONAL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "unpack":
            if(ext == Extension.MMX) {
                return OpcodeGroup.MMX_UNPACK;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "x87fpu":
            if(groups.contains(OpcodeGroup.PREFIX)) {
                return OpcodeGroup.PREFIX_FPU;
            } else {
                return OpcodeGroup.FPU;
            }
        case "strtxt":
            if(ext == Extension.SSE_4_2) {
                return OpcodeGroup.SSE42_STRINGTEXT;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "pcksp":
            if(ext == Extension.SSE_2) {
                return OpcodeGroup.SSE2_SINGLE;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "fetch":
            if(ext == Extension.SSE_1) {
                return OpcodeGroup.SSE1_PREFETCH;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "trans":
            if(groups.contains(OpcodeGroup.SYSTEM_BRANCH)) {
                return OpcodeGroup.SYSTEM_BRANCH_TRANSITIONAL;
            } else if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_TRANSCENDENTAL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "flgctrl":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_FLAGCONTROL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "sync":
            if(ext == Extension.SSE_3) {
                return OpcodeGroup.SSE3_CACHE;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "string":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_STRING;
            } else if(groups.contains(OpcodeGroup.PREFIX)) {
                return OpcodeGroup.PREFIX_STRING;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "inout":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_IO;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "break":
            if(groups.contains(OpcodeGroup.GENERAL)) {
                return OpcodeGroup.GENERAL_BREAK;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "decimal":
            if(groups.contains(OpcodeGroup.GENERAL_ARITHMETIC)) {
                return OpcodeGroup.GENERAL_ARITHMETIC_DECIMAL;
            } else {
                System.err.println("invalid top group: " + group);
            }
        case "obsol":
            return OpcodeGroup.OBSOLETE;
        case "ldconst":
            if(groups.contains(OpcodeGroup.FPU)) {
                return OpcodeGroup.FPU_LOADCONST;
            } else {
                System.err.println("invalid top group: " + group);
            }
        default:
            System.err.println("Unknown opcode group: " + group);
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
