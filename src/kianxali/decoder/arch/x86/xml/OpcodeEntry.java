package kianxali.decoder.arch.x86.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kianxali.decoder.arch.x86.X86CPU.ExecutionMode;
import kianxali.decoder.arch.x86.X86CPU.InstructionSetExtension;
import kianxali.decoder.arch.x86.X86CPU.Model;

/**
 * An x86 opcode can have a syntax depending on the current CPU mode.
 * This class stores the information that is shared across all different
 * syntaxes.
 *
 * @author fwi
 *
 */
public class OpcodeEntry {
    /** The prefix that must be present for this opcode (optional).
     * Note that it doesn't have to be directly before the opcode */
    public Short prefix;

    /** whether the opcode is a two-byte one, e.g. needs 0F directly before it */
    public boolean twoByte;

    /** the actual opcode byte */
    public short opcode;

    /** A byte that must be immediately followed by the opcode */
    public Short secondOpcode;

    /** the mode where this opcode is defined */
    public ExecutionMode mode;

    /** whether the opcode always needs a ModR/M byte. false doesn't imply there won't be one */
    public boolean modRM;

    /** If the opcode is not part of the basic x86 opcodes,
     * the instruction set extension is stored here */
    public InstructionSetExtension instrExt;

    /** A brief description of what this opcode does */
    public String briefDescription;

    /** groups that further describe this opcode */
    public final Set<OpcodeGroup> groups;

    // not so important stuff coming from the XML document
    public boolean invalid, undefined;
    public boolean direction;
    public boolean sgnExt;
    public boolean opSize;
    public boolean lock;
    public boolean particular;
    public byte tttn;

    private final List<OpcodeSyntax> syntaxes;

    // models where the opcode was first and last supported
    Model startModel, lastModel;

    public Integer memFormat;

    OpcodeEntry() {
        this.groups = new HashSet<>();
        this.syntaxes = new ArrayList<>(4);
    }

    void setStartProcessor(Model p) {
        this.startModel = p;
    }

    /**
     * Get the CPU model that introduced this opcode,
     * i.e. where it was first supported.
     * @return the first CPU model supporting this opcode. Never null.
     */
    public Model getStartModel() {
        if(startModel != null) {
            return startModel;
        } else {
            return Model.I8086;
        }
    }

    void setEndProcessor(Model p) {
        this.lastModel = p;
    }

    /**
     * Get the latest CPU model that supports this opcode.
     * @return the latest CPU model supporting this opcode.
     *         {@link Model#ANY} iff not obsolete.
     */
    public Model getLastModel() {
        if(lastModel != null) {
            return lastModel;
        } else {
            return Model.ANY;
        }
    }

    /**
     * Checks whether this opcode is supported on a specific CPU model
     * in a specific execution mode.
     * @param p the CPU model to check for
     * @param pMode the execution mode to check for
     * @return true if the opcode is supported, false if not
     */
    public boolean isSupportedOn(Model p, ExecutionMode pMode) {
        Model compare = p;
        if(p == Model.ANY) {
            compare = Model.CORE_I7;
        }
        Model last = getLastModel();
        if(last.ordinal() < compare.ordinal()) {
            return false;
        }
        if(mode.ordinal() > pMode.ordinal()) {
            return false;
        }

        return true;
    }

    void addOpcodeGroup(OpcodeGroup group) {
        groups.add(group);
    }

    /**
     * Checks whether this opcode belongs to a certain group
     * @param group the group to check for
     * @return true if the opcode is part of the group, false otherwise
     */
    public boolean belongsTo(OpcodeGroup group) {
        return groups.contains(group);
    }

    void addSyntax(OpcodeSyntax syntax) {
        syntaxes.add(syntax);
    }
}
