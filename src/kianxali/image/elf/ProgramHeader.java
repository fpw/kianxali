package kianxali.image.elf;

import kianxali.image.ByteSequence;

public class ProgramHeader {
	public enum Type {
		PT_NULL, 		// Unused program header
		PT_LOAD,		// Loadable program segment
		PT_DYNAMIC,		// Dynamic linking information
		PT_INTERP,		// Program interpreter, e.g. ld-linux
		PT_NOTE,		// Auxiliary information
		UNKNOWN};

	private Type type;
	private long flags, fileOffset, virtAddr, physAddr;
	private long segmentSize, memSize, align;

	public ProgramHeader(ByteSequence seq, boolean elf64) {
		int pType = (int) seq.readUDword();
		switch(pType) {
		case 0: type = Type.PT_NULL; break;
		case 1: type = Type.PT_LOAD; break;
		case 2: type = Type.PT_DYNAMIC; break;
		case 3: type = Type.PT_INTERP; break;
		case 4: type = Type.PT_NOTE; break;
		default: type = Type.UNKNOWN; break;
		}

		if(elf64) {
			flags = seq.readUDword();
			fileOffset = seq.readSQword();
			virtAddr = seq.readSQword();
			physAddr = seq.readSQword();
			segmentSize = seq.readSQword();
			memSize = seq.readSQword();
			align = seq.readSQword();
		} else {
			fileOffset = seq.readUDword();
			virtAddr = seq.readUDword();
			physAddr = seq.readUDword();
			segmentSize = seq.readUDword();
			memSize = seq.readUDword();
			flags = seq.readUDword();
			align = seq.readUDword();
		}
	}

	public Type getType() {
		return type;
	}

	public boolean isReadable() {
		return (flags & 1) != 0;
	}

	public boolean isWritable() {
		return (flags & 2) != 0;
	}

	public boolean isExecutable() {
		return (flags & 4) != 0;
	}

	// from start of file
	public long getFileOffset() {
		return fileOffset;
	}

	// for loaded segments: virtual address
	public long getVirtAddr() {
		return virtAddr;
	}

	// for not loaded segments
	public long getPhysAddr() {
		return physAddr;
	}

	// size of segment in file
	public long getSegmentSize() {
		return segmentSize;
	}

	// size in virtual memory
	public long getMemSize() {
		return memSize;
	}

	public long getAlign() {
		return align;
	}


}
