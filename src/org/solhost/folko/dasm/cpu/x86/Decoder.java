package org.solhost.folko.dasm.cpu.x86;

import java.util.List;

import org.solhost.folko.dasm.ByteSequence;
import org.solhost.folko.dasm.cpu.x86.X86CPU.ExecutionMode;
import org.solhost.folko.dasm.cpu.x86.X86CPU.Model;
import org.solhost.folko.dasm.decoder.DecodeListener;
import org.solhost.folko.dasm.decoder.DecodeTree;
import org.solhost.folko.dasm.decoder.DecodedEntity;
import org.solhost.folko.dasm.xml.OpcodeSyntax;

public class Decoder {
    private final DecodeTree<OpcodeSyntax> decodeTree;

    public Decoder(DecodeTree<OpcodeSyntax> decodeTree) {
        this.decodeTree = decodeTree;
    }

    public void decode(final ByteSequence seq, DecodeListener listener) {
        Context ctx = new Context(Model.CORE_I7, ExecutionMode.PROTECTED);
        boolean goOn = true;
        while(goOn) {
            ctx.setFileOffset(seq.getPosition());
            Instruction inst = decodeNext(seq, ctx, decodeTree);
            if(inst != null) {
                if(inst.isPrefix()) {
                    ctx.applyPrefix(inst);
                } else {
                    inst.decode(seq, ctx);
                    long size = seq.getPosition() - ctx.getFileOffset();
                    listener.onDecode(ctx.getFileOffset(), (int) size, inst);
                    ctx.reset();
                }
            } else {
                listener.onDecode(ctx.getFileOffset(), 1, new DecodedEntity() {
                    @Override
                    public String toString() {
                        return String.format("Unknown opcode: %02X", seq.readUByte());
                    }
                });
                goOn = false;
            }
        }
    }

    private Instruction decodeNext(ByteSequence sequence, Context ctx, DecodeTree<OpcodeSyntax> tree) {
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

        // TODO: what if there are multiple syntaxes for this sequence? take first for now
        OpcodeSyntax syntax = leaves.get(0);

        return new Instruction(syntax);
    }
}
