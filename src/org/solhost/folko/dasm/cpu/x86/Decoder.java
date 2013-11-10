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
            long pos = seq.getPosition();
            Instruction inst = decodeNext(seq, decodeTree);
            if(inst != null) {
                if(inst.isPrefix()) {
                    ctx.applyPrefix(inst);
                } else {
                    inst.decode(seq, ctx);
                    listener.onDecode(pos, (int) (seq.getPosition() - pos), inst);
                    ctx.reset();
                    goOn = false;
                }
            } else {
                listener.onDecode(pos, 1, new DecodedEntity() {
                    @Override
                    public String toString() {
                        return String.format("Unknown opcode: %02X", seq.readUByte());
                    }
                });
                goOn = false;
            }
        }
    }

    private Instruction decodeNext(ByteSequence sequence, DecodeTree<OpcodeSyntax> tree) {
        short s = sequence.readUByte();

        DecodeTree<OpcodeSyntax> subTree = tree.getSubTree(s);
        if(subTree != null) {
            Instruction res = decodeNext(sequence, subTree);
            if(res != null) {
                return res;
            }
        }

        // no success in sub tree -> could be in leaf
        List<OpcodeSyntax> leaves = tree.getLeaves(s);
        if(leaves == null) {
            sequence.skip(-1);
            return null;
        }

        // TODO: what if there are multiple syntaxes for this sequence? take first for now
        OpcodeSyntax syntax = leaves.get(0);

        return new Instruction(syntax);
    }
}
