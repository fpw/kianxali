package kianxali.cpu.x86.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kianxali.cpu.x86.X86CPU.ExecutionMode;
import kianxali.cpu.x86.X86CPU.InstructionSetExtension;
import kianxali.cpu.x86.X86CPU.Model;

// an opcode can have a different meaning depending on CPU mode, hence store it here
public class OpcodeEntry {
    public boolean twoByte;
    public short opcode;
    public ExecutionMode mode;
    public boolean invalid, undefined;
    public boolean direction;
    public boolean sgnExt;
    public boolean opSize;
    public boolean modRM;
    public boolean lock;
    public boolean particular;
    public byte tttn;
    public String briefDescription;

    public Short prefix; // prefix that must be present for this opcode
    public Short secondOpcode; // mandatory byte after opcode
    public InstructionSetExtension instrExt;

    private final List<OpcodeSyntax> syntaxes;
    Model startModel, lastModel;
    public final Set<OpcodeGroup> groups;

    public OpcodeEntry() {
        this.groups = new HashSet<>();
        this.syntaxes = new ArrayList<>(4);
    }

    public void setStartProcessor(Model p) {
        this.startModel = p;
    }

    public Model getStartModel() {
        if(startModel != null) {
            return startModel;
        } else {
            return Model.I8086;
        }
    }

    public void setEndProcessor(Model p) {
        this.lastModel = p;
    }

    public Model getLastModel() {
        if(lastModel != null) {
            return lastModel;
        } else {
            return Model.ANY;
        }
    }

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

    public void addOpcodeGroup(OpcodeGroup group) {
        groups.add(group);
    }

    public boolean belongsTo(OpcodeGroup group) {
        return groups.contains(group);
    }

    public void addSyntax(OpcodeSyntax syntax) {
        syntaxes.add(syntax);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(String.format("<opcode=%s%2X mode=%s modRM=%s", twoByte ? "0F" : "", opcode, mode, modRM));
        for(OpcodeSyntax syntax : syntaxes) {
            res.append(" " + syntax.toString());
        }
        res.append(">");
        return res.toString();
    }
}
