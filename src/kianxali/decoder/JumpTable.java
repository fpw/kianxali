package kianxali.decoder;

import java.util.ArrayList;
import java.util.List;

import kianxali.loader.ByteSequence;
import kianxali.util.OutputFormatter;

/**
 * A jump table is a special type of data that represents
 * a table of memory adresses pointing to code.
 * @author fwi
 *
 */
public class JumpTable extends Data {
    private final List<Long> entries;

    /**
     * Construct a new jump table object
     * @param memAddr the memory address of this table
     */
    public JumpTable(long memAddr) {
        super(memAddr, DataType.JUMP_TABLE);
        this.entries = new ArrayList<>();
    }

    /**
     * Adds a code address to the table
     * @param entry the code address to add
     */
    public void addEntry(long entry) {
        entries.add(entry);
    }

    @Override
    public void analyze(ByteSequence seq) {
        // analyzed by disassembler
        return;
    }

    @Override
    public String asString(OutputFormatter format) {
        StringBuilder res = new StringBuilder("<Jump table with " + entries.size() + " entries:");
        for(long entry : entries) {
            res.append(" " + format.formatAddress(entry));
        }
        res.append(">");
        return res.toString();
    }
}
