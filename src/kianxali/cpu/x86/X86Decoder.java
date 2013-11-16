package kianxali.cpu.x86;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.Model;
import kianxali.cpu.x86.xml.OpcodeSyntax;
import kianxali.cpu.x86.xml.XMLParserX86;
import kianxali.decoder.Context;
import kianxali.decoder.DecodeTree;
import kianxali.decoder.Decoder;
import kianxali.decoder.Instruction;
import kianxali.image.ByteSequence;

import org.xml.sax.SAXException;

public final class X86Decoder implements Decoder {
    private static final Logger LOG = Logger.getLogger("kianxali.x86.decoder");
    private static XMLParserX86 parser;
    private final DecodeTree<OpcodeSyntax> decodeTree;

    private X86Decoder(DecodeTree<OpcodeSyntax> tree) {
        this.decodeTree = tree;
    }

    public static synchronized X86Decoder fromXML(Model cpu, ExecutionMode mode, String xmlPath, String dtdPath) throws SAXException, IOException {
        DecodeTree<OpcodeSyntax> tree = createDecodeTree(cpu, mode, xmlPath, dtdPath);
        return new X86Decoder(tree);
    }

    private static DecodeTree<OpcodeSyntax> createDecodeTree(Model cpu, ExecutionMode mode, String xmlPath, String dtdPath) throws SAXException, IOException {
        if(parser == null) {
            LOG.config("Creating x86 decoding tree from XML...");
            parser = new XMLParserX86();
            parser.loadXML(xmlPath, dtdPath);
        }
        DecodeTree<OpcodeSyntax> tree = new DecodeTree<>();

        // build decode tree
        for(final OpcodeSyntax entry : parser.getSyntaxEntries()) {
            // if an opcode isn't supported on this model, don't put it into the tree
            if(!entry.getOpcodeEntry().isSupportedOn(cpu, mode)) {
                continue;
            }
            short[] prefix = entry.getPrefix();
            if(entry.hasEncodedRegister()) {
                int regIndex = prefix.length - 1 - entry.getEncodedRegisterRelativeIndex();
                for(int i = 0; i < 8; i++) {
                    tree.addEntry(prefix, entry);
                    prefix[regIndex]++;
                }
            } else {
                tree.addEntry(prefix, entry);
            }
        }

        // filter decode tree: remove opcodes that have a special version in the requested mode
        filterTree(cpu, mode, tree);

        return tree;
    }

    private static void filterTree(Model cpu, ExecutionMode mode, DecodeTree<OpcodeSyntax> tree) {
        for(short s : tree.getLeaveCodes()) {
            List<OpcodeSyntax> syntaxes = tree.getLeaves(s);
            if(syntaxes.size() > 1) {
                filterSpecializedMode(syntaxes, mode);
                filterReplaced(syntaxes, cpu);
                // if(syntaxes.size() > 1) {
                //    LOG.finest("Double prefix: " + syntaxes.get(0).getPrefixAsHexString());
                // }
            }
        }
        for(DecodeTree<OpcodeSyntax> subTree : tree.getSubTrees()) {
            filterTree(cpu, mode, subTree);
        }
    }

    private static void filterReplaced(List<OpcodeSyntax> syntaxes, Model cpu) {
        Model latestStart = null;
        Short extension = null;
        for(OpcodeSyntax syntax : syntaxes) {
            if(syntax.getOpcodeEntry().particular) {
                continue;
            }
            if(latestStart == null || syntax.getOpcodeEntry().getStartModel().ordinal() > latestStart.ordinal()) {
                latestStart = syntax.getOpcodeEntry().getStartModel();
                extension = syntax.getExtension();
            }
        }

        Iterator<OpcodeSyntax> it = syntaxes.iterator();
        while(it.hasNext()) {
            OpcodeSyntax syntax = it.next();
            if(syntax.getExtension() != extension) {
                continue;
            }
            if(syntax.getOpcodeEntry().getStartModel().ordinal() < latestStart.ordinal()) {
                it.remove();
            }
        }
    }

    private static boolean filterSpecializedMode(List<OpcodeSyntax> syntaxes, ExecutionMode mode) {
        Short extension = null;
        boolean hasSpecialMode = false;
        for(OpcodeSyntax syntax : syntaxes) {
            if(syntax.getOpcodeEntry().mode == mode) {
                hasSpecialMode = true;
                extension = syntax.getExtension();
            }
        }

        if(hasSpecialMode) {
            Iterator<OpcodeSyntax> it = syntaxes.iterator();
            while(it.hasNext()) {
                OpcodeSyntax syntax = it.next();
                if(syntax.getOpcodeEntry().mode != mode) {
                    if(extension == null || extension == syntax.getExtension()) {
                        // LOG.finest("Removing due to specialized version: " + syntax);
                        it.remove();
                    }
                }
            }
        }

        return false;
    }

    public List<OpcodeSyntax> getAllSyntaxes() {
        return getAllSyntaxesFromTree(decodeTree);
    }

    private List<OpcodeSyntax> getAllSyntaxesFromTree(DecodeTree<OpcodeSyntax> tree) {
        List<OpcodeSyntax> res = new LinkedList<>();
        for(short s : tree.getLeaveCodes()) {
            res.addAll(tree.getLeaves(s));
        }
        for(DecodeTree<OpcodeSyntax> subTree : tree.getSubTrees()) {
            res.addAll(getAllSyntaxesFromTree(subTree));
        }
        return Collections.unmodifiableList(res);
    }

    @Override
    public Instruction decodeOpcode(Context context, ByteSequence seq) {
        X86Context ctx = (X86Context) context;
        ctx.reset();
        X86Instruction inst = decodeNext(seq, ctx, decodeTree);
        return inst;
    }

    private X86Instruction decodeNext(ByteSequence sequence, X86Context ctx, DecodeTree<OpcodeSyntax> tree) {
        if(!sequence.hasMore()) {
            return null;
        }
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

        X86Instruction inst = new X86Instruction(ctx.getInstructionPointer(), leaves);
        if(!inst.decode(sequence, ctx)) {
            sequence.skip(-1);
            ctx.getPrefix().popPrefixByte();
            return null;
        }
        if(inst.isPrefix()) {
            ctx.applyPrefix(inst);
            return decodeNext(sequence, ctx, decodeTree);
        } else {
            return inst;
        }
    }
}
