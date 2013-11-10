package org.solhost.folko.dasm.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.solhost.folko.dasm.instructions.x86.CPUMode;

// an opcode can have a different meaning depending on CPU mode, hence store it here
public class OpcodeOpts {
    public enum Processor {
        I8086, I80186, I80286, I80386, I80486,
        PENTIUM, PENTIUM_MMX, PENTIUM_PRO, PENTIUM_II, PENTIUM_III, PENTIUM_IV,
        CORE_1, CORE_2, CORE_I7,
        ITANIUM
    }

    public enum Extension {
        NONE,
        MMX,
        SSE_1, SSE_2, SSE_3, SSE_4_1, SSE_4_2, SSSE_3,
        SMX,
        VMX
    }

    public enum Prefix {
        OPERAND_SIZE, ADDRESS_SIZE,
        REP_Z, REP_NZ,
        SEG_CS, SEG_DS, SEG_SS, SEG_ES, SEG_FS, SEG_GS,
        LOCK, WAIT
    }

    public enum OpcodeGroup {
        PREFIX,
        PREFIX_SEGREG,
        PREFIX_BRANCH,
        PREFIX_BRANCH_CONDITIONAL,
        PREFIX_FPU,
        PREFIX_FPU_CONTROL,
        PREFIX_STRING, // undocumented in the XML doc

        OBSOLETE,
        OBSOLETE_CONTROL,

        GENERAL,
        GENERAL_DATAMOVE,
        GENERAL_STACK,
        GENERAL_CONVERSION,
        GENERAL_ARITHMETIC,
        GENERAL_ARITHMETIC_BINARY,
        GENERAL_ARITHMETIC_DECIMAL,
        GENERAL_LOGICAL,
        GENERAL_SHIFTROT,
        GENERAL_BITMANIPULATION,
        GENERAL_BRANCH,
        GENERAL_BRANCH_CONDITIONAL,
        GENERAL_BREAK,
        GENERAL_STRING, // can use REP prefix
        GENERAL_IO,
        GENERAL_FLAGCONTROL,
        GENERAL_SEGREGMANIPULATION,
        GENERAL_CONTROL,

        SYSTEM,
        SYSTEM_BRANCH,
        SYSTEM_BRANCH_TRANSITIONAL, // obeys operand-size attribute

        FPU,
        FPU_DATAMOVE,
        FPU_ARITHMETIC,
        FPU_COMPARISON,
        FPU_TRANSCENDENTAL,
        FPU_LOADCONST,
        FPU_CONTROL,
        FPU_CONVERSION,

        FPUSIMDSTATE,

        MMX_DATAMOV,
        MMX_ARITHMETIC,
        MMX_COMPARISON,
        MMX_CONVERSION,
        MMX_LOGICAL,
        MMX_SHIFT,
        MMX_UNPACK,

        SSE1_SINGLE, // SIMD single precision floating point
        SSE1_SINGLE_DATAMOVE,
        SSE1_SINGLE_ARITHMETIC,
        SSE1_SINGLE_COMPARISON,
        SSE1_SINGLE_LOGICAL,
        SSE1_SINGLE_SHUFFLEUNPACK,
        SSE1_CONVERSION,
        SSE1_INT64, // SIMD on 64 bit integers
        SSE1_MXCSR, // MXCSR state management
        SSE1_CACHE,
        SSE1_PREFETCH,
        SSE1_INSTRUCTIONORDER,

        SSE2_DOUBLE, // packed and scalar double precision floats
        SSE2_DOUBLE_DATAMOVE,
        SSE2_DOUBLE_CONVERSION,
        SSE2_DOUBLE_ARITHMETIC,
        SSE2_DOUBLE_COMPARISON,
        SSE2_DOUBLE_LOGICAL,
        SSE2_DOUBLE_SHUFFLEUNPACK,
        SSE2_SINGLE, // packed single precision floats
        SSE2_INT128, // SIMD on 128 bit integers
        SSE2_INT128_DATAMOVE,
        SSE2_INT128_ARITHMETIC,
        SSE2_INT128_SHUFFLEUNPACK,
        SSE2_INT128_SHIFT,
        SSE2_INT128_COMPARISON,
        SSE2_INT128_CONVERSION,
        SSE2_INT128_LOGICAL,
        SSE2_CACHE,
        SSE2_INSTRUCTIONORDER,

        SSE3_FLOAT, // SIMD single precision float
        SSE3_FLOAT_DATAMOVE,
        SSE3_FLOAT_ARITHMETIC,
        SSE3_CACHE,
        SSE3_SYNC,

        SSSE3_INT, // SIMD integer

        SSE41_INT, // SIMD integer
        SSE41_INT_DATAMOVE,
        SSE41_INT_ARITHMETIC,
        SSE41_INT_COMPARISON,
        SSE41_INT_CONVERSION,
        SSE41_FLOAT,
        SSE41_FLOAT_DATAMOVE,
        SSE41_FLOAT_ARITHMETIC,
        SSE41_FLOAT_CONVERSION,
        SSE41_CACHE,

        SSE42_INT, // SIMD integer
        SSE42_INT_COMPARISON,
        SSE42_STRINGTEXT
    };

    public CPUMode mode;
    public boolean invalid, undefined;
    public boolean direction;
    public boolean sgnExt;
    public boolean opSize;
    public boolean modRM;
    public boolean lock;
    public byte tttn;

    public Prefix prefix; // prefix that must be present for this opcode
    public Short secondOpcode; // mandatory byte after opcode
    public Extension instrExt;

    private final List<Syntax> syntaxes;
    public final Set<Processor> supportedProcessors;
    public final Set<OpcodeGroup> groups;

    public OpcodeOpts() {
        this.instrExt = Extension.NONE;
        this.supportedProcessors = new HashSet<>();
        this.groups = new HashSet<>();
        this.syntaxes = new ArrayList<>(4);
    }

    public void setStartProcessor(Processor p) {
        // fall-through on purpose
        switch (p) {
        case I8086:         supportedProcessors.add(Processor.I8086);
        case I80186:        supportedProcessors.add(Processor.I80186);
        case I80286:        supportedProcessors.add(Processor.I80286);
        case I80386:        supportedProcessors.add(Processor.I80386);
        case I80486:        supportedProcessors.add(Processor.I80486);
        case PENTIUM:       supportedProcessors.add(Processor.PENTIUM);
        case PENTIUM_MMX:   supportedProcessors.add(Processor.PENTIUM_MMX);
        case PENTIUM_PRO:   supportedProcessors.add(Processor.PENTIUM_PRO);
        case PENTIUM_II:    supportedProcessors.add(Processor.PENTIUM_II);
        case PENTIUM_III:   supportedProcessors.add(Processor.PENTIUM_III);
        case PENTIUM_IV:    supportedProcessors.add(Processor.PENTIUM_IV);
        case CORE_1:        supportedProcessors.add(Processor.CORE_1);
        case CORE_2:        supportedProcessors.add(Processor.CORE_2);
        case CORE_I7:       supportedProcessors.add(Processor.CORE_I7);
        case ITANIUM:       supportedProcessors.add(Processor.ITANIUM);
        }
    }

    public void setEndProcessor(Processor p) {
        // fall-through on purpose
        switch (p) {
        case I8086:         supportedProcessors.remove(Processor.I80186);
        case I80186:        supportedProcessors.remove(Processor.I80286);
        case I80286:        supportedProcessors.remove(Processor.I80386);
        case I80386:        supportedProcessors.remove(Processor.I80486);
        case I80486:        supportedProcessors.remove(Processor.PENTIUM);
        case PENTIUM:       supportedProcessors.remove(Processor.PENTIUM_MMX);
        case PENTIUM_MMX:   supportedProcessors.remove(Processor.PENTIUM_PRO);
        case PENTIUM_PRO:   supportedProcessors.remove(Processor.PENTIUM_II);
        case PENTIUM_II:    supportedProcessors.remove(Processor.PENTIUM_III);
        case PENTIUM_III:   supportedProcessors.remove(Processor.PENTIUM_IV);
        case PENTIUM_IV:    supportedProcessors.remove(Processor.CORE_1);
        case CORE_1:        supportedProcessors.remove(Processor.CORE_2);
        case CORE_2:        supportedProcessors.remove(Processor.CORE_I7);
        case CORE_I7:       supportedProcessors.remove(Processor.ITANIUM);
        case ITANIUM:
        }
    }

    public void addGroup(OpcodeGroup group) {
        groups.add(group);
    }

    public boolean belongsTo(OpcodeGroup group) {
        return groups.contains(group);
    }

    // without opcode extension
    public void addSyntax(Syntax syntax) {
        syntaxes.add(syntax);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("<mode=" + mode + " modRM=" + modRM);
        for(Syntax syntax : syntaxes) {
            res.append(" " + syntax.toString());
        }
        res.append(">");
        return res.toString();
    }
}
