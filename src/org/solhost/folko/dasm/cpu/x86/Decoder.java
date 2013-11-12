package org.solhost.folko.dasm.cpu.x86;

import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.ImageFile;
import org.solhost.folko.dasm.OutputFormat;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.decoder.DecodeListener;
import org.solhost.folko.dasm.decoder.DecodeTree;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.xml.OpcodeSyntax;

public class Decoder {
    private final DecodeTree<OpcodeSyntax> decodeTree;

    public Decoder(DecodeTree<OpcodeSyntax> decodeTree) {
        this.decodeTree = decodeTree;
    }

    public void decodeImage(ImageFile image, DecodeListener listener) {
        final ByteSequence seq = image.getByteSequence(image.getCodeEntryPointMem());
        X86Context ctx = (X86Context) image.createContext();

        boolean goOn = true;
        while(goOn) {
            ctx.setFileOffset(seq.getPosition());
            Instruction inst = decodeNext(seq, ctx, decodeTree);
            if(inst != null) {
                inst.decode(seq, ctx);
                long size = seq.getPosition() - ctx.getFileOffset();
                listener.onDecode(ctx.getVirtualAddress(), (int) size, inst);
                ctx.reset();
            } else {
                listener.onDecode(ctx.getFileOffset(), 1, new DecodedEntity() {
                    public String asString(OutputFormat options) {
                        return String.format("Unknown opcode: %02X", seq.readUByte());
                    }
                });
                goOn = false;
            }
        }
    }

    public Instruction decodeOpcode(X86Context ctx, ByteSequence seq) {
        Instruction inst = decodeNext(seq, ctx, decodeTree);
        if(inst != null) {
            inst.decode(seq, ctx);
        }
        ctx.reset();
        return inst;
    }

    private Instruction decodeNext(ByteSequence sequence, X86Context ctx, DecodeTree<OpcodeSyntax> tree) {
        short s = sequence.readUByte();
        ctx.addDecodedPrefix(s);

        DecodeTree<OpcodeSyntax> subTree = tree.getSubTree(s);
        if(subTree != null) {
            Instruction res = decodeNext(sequence, ctx, subTree);
            if(res != null) {
                return res;
            }
        }

        // no success in sub tree -> could be in leaf
        List<OpcodeSyntax> leaves = tree.getLeaves(s);
        if(leaves == null) {
            sequence.skip(-1);
            ctx.removeDecodedPrefixTop();
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
            ctx.removeDecodedPrefixTop();
            return null;
        }

        Instruction inst = new Instruction(res);
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
                    extension = (short) ((sequence.readUByte() >> 3) & 0x07);
                    sequence.skip(-1);
                }
                if(syntax.getExtension() == extension) {
                    res = syntax;
                    break;
                }
            } else if(ctx.acceptsOpcode(syntax)) {
                // TODO: what if there are multiple syntaxes for this sequence without extension?
                // take first match for now
                res = syntax;
                break;
            }
        }
        return res;
    }
}
