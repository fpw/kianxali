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
    public byte tttn;

    public Short prefix; // prefix that must be present for this opcode
    public Short secondOpcode; // mandatory byte after opcode
    public InstructionSetExtension instrExt;

    private final List<OpcodeSyntax> syntaxes;
    public final Set<Model> supportedProcessors;
    public final Set<OpcodeGroup> groups;

    public OpcodeEntry() {
        this.supportedProcessors = new HashSet<>();
        this.groups = new HashSet<>();
        this.syntaxes = new ArrayList<>(4);
    }

    public void setStartProcessor(Model p) {
        // fall-through on purpose
        switch (p) {
        case I8086:         supportedProcessors.add(Model.I8086);
        case I80186:        supportedProcessors.add(Model.I80186);
        case I80286:        supportedProcessors.add(Model.I80286);
        case I80386:        supportedProcessors.add(Model.I80386);
        case I80486:        supportedProcessors.add(Model.I80486);
        case PENTIUM:       supportedProcessors.add(Model.PENTIUM);
        case PENTIUM_MMX:   supportedProcessors.add(Model.PENTIUM_MMX);
        case PENTIUM_PRO:   supportedProcessors.add(Model.PENTIUM_PRO);
        case PENTIUM_II:    supportedProcessors.add(Model.PENTIUM_II);
        case PENTIUM_III:   supportedProcessors.add(Model.PENTIUM_III);
        case PENTIUM_IV:    supportedProcessors.add(Model.PENTIUM_IV);
        case CORE_1:        supportedProcessors.add(Model.CORE_1);
        case CORE_2:        supportedProcessors.add(Model.CORE_2);
        case CORE_I7:       supportedProcessors.add(Model.CORE_I7);
        case ITANIUM:       supportedProcessors.add(Model.ITANIUM);
                            break;
        default:            throw new UnsupportedOperationException("invalid model: " + p);
        }
    }

    public void setEndProcessor(Model p) {
        // fall-through on purpose
        switch (p) {
        case I8086:         supportedProcessors.remove(Model.I80186);
        case I80186:        supportedProcessors.remove(Model.I80286);
        case I80286:        supportedProcessors.remove(Model.I80386);
        case I80386:        supportedProcessors.remove(Model.I80486);
        case I80486:        supportedProcessors.remove(Model.PENTIUM);
        case PENTIUM:       supportedProcessors.remove(Model.PENTIUM_MMX);
        case PENTIUM_MMX:   supportedProcessors.remove(Model.PENTIUM_PRO);
        case PENTIUM_PRO:   supportedProcessors.remove(Model.PENTIUM_II);
        case PENTIUM_II:    supportedProcessors.remove(Model.PENTIUM_III);
        case PENTIUM_III:   supportedProcessors.remove(Model.PENTIUM_IV);
        case PENTIUM_IV:    supportedProcessors.remove(Model.CORE_1);
        case CORE_1:        supportedProcessors.remove(Model.CORE_2);
        case CORE_2:        supportedProcessors.remove(Model.CORE_I7);
        case CORE_I7:       supportedProcessors.remove(Model.ITANIUM);
        case ITANIUM:       break;
        default:            throw new UnsupportedOperationException("invalid model: " + p);
        }
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
