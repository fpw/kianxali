package org.solhost.folko.dasm.decoder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DecodeTree<LeafType> {
    private class Node {
        public Map<Short, DecodeTree<LeafType>> subTrees;
        public Map<Short, List<LeafType>> leaves;

        public Node() {
            subTrees = new HashMap<>();
            leaves = new HashMap<>();
        }
    }
    private final Node node;

    public DecodeTree() {
        node = new Node();
    }

    public void addEntry(short sequence[], LeafType leaf) {
        addEntry(sequence, 0, leaf);
    }

    public boolean hasSubTrees() {
        return node.subTrees.size() != 0;
    }

    public DecodeTree<LeafType> getSubTree(short s) {
        return node.subTrees.get(s);
    }

    public List<LeafType> getLeaves(short s) {
        return node.leaves.get(s);
    }

    private void addEntry(short sequence[], int index, LeafType leaf) {
        short s = sequence[index];
        if(index < sequence.length - 1) {
            // non-leaf child
            DecodeTree<LeafType> subTree = node.subTrees.get(s);
            if(subTree == null) {
                subTree = new DecodeTree<LeafType>();
                node.subTrees.put(s, subTree);
            }
            subTree.addEntry(sequence, index + 1, leaf);
        } else {
            // leaf
            List<LeafType> leaves = node.leaves.get(s);
            if(leaves == null) {
                leaves = new LinkedList<>();
                node.leaves.put(s, leaves);
            }
            leaves.add(leaf);
        }
    }
}
