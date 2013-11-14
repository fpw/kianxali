package kianxali.cpu.x86;

import java.io.IOException;
import java.util.List;

import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.xml.OpcodeSyntax;
import kianxali.cpu.x86.xml.XMLParserX86;
import kianxali.decoder.Context;
import kianxali.decoder.DecodeTree;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ByteSequence;

import org.xml.sax.SAXException;

public final class X86Decoder implements Decoder {
    private static DecodeTree<OpcodeSyntax> xmlTree;
    private final DecodeTree<OpcodeSyntax> decodeTree;

    private X86Decoder(DecodeTree<OpcodeSyntax> tree) {
        this.decodeTree = tree;
    }

    public static synchronized X86Decoder fromXML(String xmlPath, String dtdPath) throws SAXException, IOException {
        if(xmlTree == null) {
            xmlTree = createDecodeTree(xmlPath, dtdPath);
        }
        return new X86Decoder(xmlTree);
    }

    private static DecodeTree<OpcodeSyntax> createDecodeTree(String xmlPath, String dtdPath) throws SAXException, IOException {
        XMLParserX86 parser = new XMLParserX86();
        DecodeTree<OpcodeSyntax> tree = new DecodeTree<>();

        parser.loadXML(xmlPath, dtdPath);

        // build decode tree
        for(final OpcodeSyntax entry : parser.getSyntaxEntries()) {
            short[] prefix = entry.getPrefix();
            if(entry.hasEncodedRegister()) {
                int regIndex = entry.getEncodedRegisterPrefixIndex();
                for(int i = 0; i < 8; i++) {
                    tree.addEntry(prefix, entry);
                    prefix[regIndex]++;
                }
            } else {
                tree.addEntry(prefix, entry);
            }
        }
        return tree;
    }

    @Override
    public Instruction decodeOpcode(Context context, ByteSequence seq) {
        X86Context ctx = (X86Context) context;
        ctx.reset();
        X86Instruction inst = decodeNext(seq, ctx, decodeTree);
        if(inst != null) {
            inst.decode(seq, ctx);
        }
        return inst;
    }

    private X86Instruction decodeNext(ByteSequence sequence, X86Context ctx, DecodeTree<OpcodeSyntax> tree) {
        short s = sequence.readUByte();
        ctx.getPrefix().pushPrefixByte(s);

        DecodeTree<OpcodeSyntax> subTree = tree.getSubTree(s);
        if(subTree != null) {
            X86Instruction res = decodeNext(sequence, ctx, subTree);
            if(res != null) {
                return res;
            }
        }

        // no success in sub tree -> could be in leaf
        List<OpcodeSyntax> leaves = tree.getLeaves(s);
        if(leaves == null) {
            sequence.skip(-1);
            ctx.getPrefix().popPrefixByte();
            return null;
        }

        OpcodeSyntax res = null;

        // first pass: check if there is a special 64 bit op if in 64 bit mode
        if(ctx.getExecMode() == ExecutionMode.LONG) {
            res = selectSyntax(ctx, leaves, sequence, true);
        }

        // if nothing found, do normal search in 2nd pass
        if(res == null) {
            res = selectSyntax(ctx, leaves, sequence, false);
        }

        if(res == null) {
            sequence.skip(-1);
            ctx.getPrefix().popPrefixByte();
            return null;
        }

        X86Instruction inst = new X86Instruction(ctx.getInstructionPointer(), res);
        if(inst.isPrefix()) {
            ctx.applyPrefix(inst);
            return decodeNext(sequence, ctx, decodeTree);
        } else {
            return inst;
        }
    }

    private OpcodeSyntax selectSyntax(X86Context ctx, List<OpcodeSyntax> syntaxes, ByteSequence sequence, boolean onlyLong) {
        OpcodeSyntax res = null;
        Short extension = null;

        for(OpcodeSyntax syntax : syntaxes) {
            if(onlyLong && syntax.getOpcodeEntry().mode != ExecutionMode.LONG) {
                continue;
            }
            if(syntax.isExtended()) {
                if(extension == null) {
                    if(syntax.getOpcodeEntry().secondOpcode != null) {
                        // TODO: verify that this is always correct
                        extension = (short) ((syntax.getOpcodeEntry().secondOpcode >> 3) & 0x07);
                    } else {
                        extension = (short) ((sequence.readUByte() >> 3) & 0x07);
                        sequence.skip(-1);
                    }
                }
                if(syntax.getExtension() == extension && ctx.acceptsOpcode(syntax)) {
                    if(res == null || fitsBetter(ctx, syntax, res)) {
                        res = syntax;
                    }
                }
            } else if(ctx.acceptsOpcode(syntax)) {
                if(res == null || fitsBetter(ctx, syntax, res)) {
                    res = syntax;
                }
            }
        }
        return res;
    }

    private boolean fitsBetter(X86Context ctx, OpcodeSyntax s, OpcodeSyntax old) {
        return true;
    }
}
