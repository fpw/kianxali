package kianxali.cpu.x86.xml;

import kianxali.decoder.UsageType;

public class OperandDesc {
    public enum AddressType {
        DIRECT,             // absolute address of adressType coded after opcode
        MOD_RM_M,           // modRM.mem
        MOD_RM_M_FPU,       // modRM.mem but use FPU registers when not mem
        MOD_RM_M_FPU_REG,   // modRM.mem must be mode 3 with FPU reg TODO
        MOD_RM_R,           // modRM.reg
        MOD_RM_R_CTRL,      // modRM.reg selects control register
        MOD_RM_R_DEBUG,     // modRM.reg selects debug register
        MOD_RM_R_TEST,      // modRM.reg selects test register
        MOD_RM_R_SEG,       // modRM.reg as segment register
        MOD_RM_M_FORCE_GEN, // to be checked: modRM.mem regardless of mode TODO
        MOD_RM_MUST_M,      // to be checked: modRM.mem regardless of mode TODO
        MOD_RM_R_FORCE_GEN, // to be checked: modRM.reg regardless of mode TODO
        MOD_RM_XMM,         // modRM as XMM
        MOD_RM_R_XMM,       // modRM.reg as XMM
        MOD_RM_M_XMM_REG,   // modRM.mem as XMM
        IMMEDIATE,          // immediate coded after opcode
        RELATIVE,           // relative address coded after opcode
        MOD_RM_MMX,         // modRM.reg or modRM.mem as MMX TODO
        MOD_RM_R_MMX,       // modRM.reg as MMX register
        MOD_RM_M_MMX,       // modRM.mem pointing to MMX qword / reg
        OFFSET,             // offset coded after opcode, TODO check difference to DIRECT
        LEAST_REG,          // least 3 bits of opcode (!) select general register
        GROUP,              // indirectly given by instruction -> look at DirectGroup
        STACK,              // by group so it fits on stack
        SEGMENT2,           // by group: two bits of index three select segment
        SEGMENT30,          // by group: least three bits select segment
        SEGMENT33,          // by group: three bits at index three select segment
        FLAGS,              // by group: rFLAGS register
        ES_EDI_RDI,         // by group: memory through ES:EDI or RDI
        DS_ESI_RSI,         // by group: memory through DS:ESI or RSI
        DS_EAX_RAX,         // by group: memory through DS:EAX or RAX
        DS_EBX_AL_RBX,      // by group: memory through DS:EBX+AL or RBX+AL
        DS_EDI_RDI          // by group: memory through DS:EDI or RDI
    }

    public enum DirectGroup {
        GENERIC,        // AL, AX, EAX etc.
        SEGMENT,        // CS, DS etc.
        X87FPU,         // FPU register
        MMX,            // MMX register
        XMM,            // XMM register
        CONTROL,        // control register
        DEBUG,          // debug register
        MSR,            // no idea
        SYSTABP,        // no idea
        XCR             // no idea
    }

    public enum OperandType {
        TWO_INDICES,        // TODO for bound instruction
        BYTE,               // byte regardless of operand size
        BCD,                // packed BCD
        BYTE_SGN,           // byte, sign-extended to operand size
        BYTE_STACK,         // byte, sign-extended to size of stack pointer
        DWORD,              // dword, regardless of operand size
        DWORD_OPS,          // dword according to opsize TODO
        DWORD_ADR,          // dword according to address size TODO
        DWORD_INT_FPU,      // dword integer for FPU
        DWORD_QWORD,        // dword or qword depending on REX.W
        DWORD_QWORD_ADR,    // dword or qword depending on address size
        DQWORD,             // double quadword (128 bits), regardless of operand size
        DOUBLE_FPU,         // double real for FPU
        FPU_ENV,            // FPU environment
        REAL_EXT_FPU,       // extended real for FPU
        REAL_SINGLE_FPU,    // single precision real for FPU
        POINTER,            // 32 or 48 bit address, depending on operand size
        QWORD_MMX,          // MMX qword
        QWORD,              // qword, regardless of operand size
        QWORD_STACK,        // qwrod according to stack size
        QWORD_ADR,          // qword according to address size
        QWORD_WORD,         // qword (default) or word if op-size prefix set
        QWORD_FPU,          // qword integer for FPU
        QWORD_REX,          // qword, promoted by REX.W
        DOUBLE_128,         // packed 128 bit double float
        SINGLE_128,         // packed 128 bit single float
        SINGLE_64,          // packed 64 bit single float
        POINTER_REX,        // 32 or 48 bit pointer, but 80 if REX.W
        PSEUDO_DESC,        // 6 byte pseudo descriptor
        FPU_STATE,          // 94 / 108 bit FPU state
        SCALAR_DOUBLE,      // scalar of 128 bit double float
        SCALAR_SINGLE,      // scalar of 128 bit single float
        FPU_SIMD_STATE,     // 512 bit FPU and SIMD state
        WORD,               // word, regardless of operand size
        WORD_STACK,         // word according to stack operand size
        WORD_ADR,           // word according to address size
        WORD_OPS,           // word according to operand size TODO
        WORD_FPU,           // word integer for FPU
        WORD_DWORD,         // word or dword (default) depending on opsize
        WORD_DWORD_ADR,     // word or dword (default) depending on address size
        WORD_DWORD_STACK,   // word or dword depending on stack pointer size
        WORD_DWORD_64,      // word or dword (depdending op size) extended if REX.W
        WORD_DWORD_S64      // word or dword (depending op size) sign ext to 64 bit if REX.W
    }

    public String hardcoded; // hardcoded operand
    public boolean indirect; // only indirect modification, e.g. LEAVE modifying EBP
    public boolean depends; // resulting value depends on its previous value
    public DirectGroup directGroup;
    public long numForGroup;
    public UsageType usageType;
    public AddressType adrType;
    public OperandType operType;

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        if(!indirect) {
            res.append(String.format("<operand %s = %s / %s>", usageType, adrType, operType));
        } else {
            res.append(String.format("<indirect operand %s = %s / %s>", usageType, adrType, operType));
        }
        return res.toString();
    }
}
