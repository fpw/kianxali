package org.solhost.folko.dasm.xml;

public class OperandDesc {
    public enum UsageType {SOURCE, DEST};

    public enum AddressType {
        DIRECT,         // absolute address of adressType coded after opcode
        CONTROL,        // modRM.reg selects control register
        DEBUG,          // modRM.reg selects debug register
        TEST,           // modRM.reg selects test register
        MOD_RM_M,       // modRM.mem
        MOD_RM_M_FPU,   // modRM.mem but use FPU registers when not mem
        MOD_RM_R_FPU,   // to be checked: modRM.reg with FPU reg TODO
        MOD_RM_R,       // modRM.reg
        MOD_RM_R_SEG,   // modRM.reg as segment register
        MOD_RM_R_FORCE, // to be checked: modRM.reg regardless of mode TODO
        MOD_RM_M_FORCE, // to be checked: modRM.mem regardless of mode TODO
        MOD_RM_R_FORCE2,// to be checked: modRM.reg regardless of mdoe TODO
        MOD_RM_XMM,     // modRM as XMM
        MOD_RM_R_XMM,   // modRM.reg as XMM
        MOD_RM_M_XMM,   // modRM.mem as XMM
        IMMEDIATE,      // immediate coded after opcode
        RELATIVE,       // relative address coded after opcode
        MOD_RM_MMX,     // modRM.reg or modRM.mem as MMX TODO
        MOD_RM_R_MMX,   // modRM.reg as MMX register
        MOD_RM_M_MMX,   // modRM.mem pointing to MMX qword
        OFFSET,         // offset coded after opcode, TODO check difference to DIRECT
        LEAST_REG       // least 3 bits of opcode (!) select general register
        };

    public enum OperandType {
        TWO_INDICES,    // TODO for bound instruction
        BYTE,           // byte regardless of operand size
        BCD,            // packed BCD
        BYTE_SGN,       // byte, sign-extended to operand size
        BYTE_STACK,     // byte, sign-extended to size of stack pointer
        DWORD,          // dword, regardless of operand size
        DWORD_INT_FPU,  // dword integer for FPU
        DWORD_QWORD,    // dword or qword depending on REX.W
        DQWORD,         // double quadword (128 bits), regardless of operand size
        DOUBLE_FPU,     // double real for FPU
        FPU_ENV,        // FPU environment
        REAL_EXT_FPU,   // extended real for FPU
        REAL_SINGLE_FPU,// single precision real for FPU
        POINTER,        // 32 or 48 bit address, depending on operand size
        QWORD_MMX,      // MMX qword
        QWORD,          // qword, regardless of operand size
        QWORD_WORD,     // qword (default) or word if op-size prefix set
        QWORD_FPU,      // qword integer for FPU
        QWORD_REX,      // qword, promoted by REX.W
        DOUBLE_128,     // packed 128 bit double float
        SINGLE_128,     // packed 128 bit single float
        SINGLE_64,      // packed 64 bit single float
        POINTER_REX,    // 32 or 48 bit pointer, but 80 if REX.W
        PSEUDO_DESC,    // 6 byte pseudo descriptor
        FPU_STATE,      // 94 / 108 bit FPU state
        SCALAR_DOUBLE,  // scalar of 128 bit double float
        SCALAR_SINGLE,  // scalar of 128 bit single float
        FPU_SIMD_STATE, // 512 bit FPU and SIMD state
        WORD,           // word, regardless of operand size
        WORD_FPU,       // word integer for FPU
        WORD_DWORD,     // word or dword (default) depending on opsize
        WORD_DWORD_STACK, // word or dword depending on stack pointer size
        WORD_DWORD_64,  // word or dword (depdending op size) extended if REX.W
        WORD_DWORD_S64, // word or dword (depending op size) sign ext to 64 bit if REX.W
    };

    public UsageType opType;
    public OperandType operType;
    public AddressType adrType;
}
