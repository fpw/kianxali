package kianxali.decoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class describes the prefix tree that is used to parse
 * opcodes.
 * @author fwi
 *
 * @param <LeafType> the type of data in the leaves, architecture dependent
 */
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

    /**
     * Creates a new and empty prefix tree
     */
    public DecodeTree() {
        node = new Node();
    }

    /**
     * Adds a sequence of bytes to the prefix tree
     * @param sequence the byte sequence to ad
     * @param leaf the leaf to add for this sequence
     */
    public void addEntry(short[] sequence, LeafType leaf) {
        addEntry(sequence, 0, leaf);
    }

    /**
     * Returns whether this tree has any sub trees
     * @return true iff there are sub trees
     */
    public boolean hasSubTrees() {
        return node.subTrees.size() != 0;
    }

    /**
     * Get the sub tree for a given byte
     * @param s the byte to dive into
     * @return the sub tree for this byte
     */
    public DecodeTree<LeafType> getSubTree(short s) {
        return node.subTrees.get(s);
    }

    /**
     * Get a list of all sub trees
     * @return a list of all sub trees inside this node
     */
    public Collection<DecodeTree<LeafType>> getSubTrees() {
        return node.subTrees.values();
    }

    /**
     * Get a list of all leaves in this node for a given byte
     * @param s the byte to get the leaves for
     * @return the list of leaves matching this byte
     */
    public List<LeafType> getLeaves(short s) {
        return node.leaves.get(s);
    }

    /**
     * Returns a set of bytes that this tree has leaves for
     * @return the set of bytes
     */
    public Set<Short> getLeaveCodes() {
        return node.leaves.keySet();
    }

    private void addEntry(short[] sequence, int index, LeafType leaf) {
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
