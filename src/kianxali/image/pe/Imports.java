package kianxali.image.pe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kianxali.image.ByteSequence;

public class Imports {
    private List<Import> imports;
    private Map<Long, Import> memToImport;
    private Map<Long, String> memToFunc;
    private boolean imports64;

    private class Import {
        @SuppressWarnings("unused")
        long orgThunk, timeStamp, forwarderChain, nameRVA, firstThunk;
        String dllName;
        List<String> functionNames;
    }

    {
        imports = new LinkedList<>();
        memToImport = new HashMap<>();
        memToFunc = new HashMap<>();
    }

    public Imports() {
    }

    public Imports(ByteSequence image, AddressConverter rva, boolean isPE32Plus) {
        this.imports64 = isPE32Plus;
        do {
            Import imp = new Import();
            // IMAGE_IMPORT_DESCRIPTOR entry
            imp.orgThunk       = image.readUDword(); // points to IMAGE_THUNK_DATA (union: as type IMAGE_IMPORT_BY_NAME) chain
            imp.timeStamp      = image.readUDword();
            imp.forwarderChain = image.readUDword();
            imp.nameRVA        = image.readUDword();
            imp.firstThunk     = image.readUDword();
            if(imp.nameRVA == 0) {
                break;
            }
            imp.functionNames = new ArrayList<>();
            imports.add(imp);
        } while(true);

        for(Import imp : imports) {
            loadImport(image, imp, rva);
        }
        loadIAT(image, rva);
    }

    private void loadImport(ByteSequence image, Import imp, AddressConverter rva) {
        image.seek(rva.rvaToFile(imp.nameRVA));
        imp.dllName = image.readString();

        List<Long> nameHints = new LinkedList<>();
        image.seek(rva.rvaToFile(imp.orgThunk));
        do {
            long nameHintRva;
            // walk IMAGE_THUNK_DATA chain, interpreting it as IMAGE_IMPORT_BY_NAME
            if(imports64) {
                nameHintRva = image.readSQword(); // FIXME UQWord
            } else {
                nameHintRva = image.readUDword();
            }
            if(nameHintRva == 0) {
                break;
            }
            nameHints.add(nameHintRva);
        } while(true);

        // now that we read the chain with the name pointers, read the actual names
        for(Long nameRva : nameHints) {
            String name;
            if((!imports64 && (nameRva & 0x80000000) != 0) || (imports64 && (nameRva & 0x8000000000000000L) != 0)) {
                // hint only, no name
                name = String.format("<%s!ordinal%04X>", imp.dllName, nameRva & 0xFFFF);
            } else {
                image.seek(rva.rvaToFile(nameRva));
                image.readUWord(); // ignore import hint
                name = image.readString();
            }
            imp.functionNames.add(name);
        }
    }

    private void loadIAT(ByteSequence image, AddressConverter rva) {
        for(Import imp : imports) {
            image.seek(rva.rvaToFile(imp.firstThunk));
            int nameIndex = 0;
            do {
                long entryRVA = rva.fileToRVA(image.getPosition());
                long nameHintRva;
                if(imports64) {
                    nameHintRva = image.readSQword(); // FIXME UQWord
                } else {
                    nameHintRva = image.readUDword();
                }
                if(nameHintRva == 0) {
                    break;
                }
                memToImport.put(rva.rvaToMemory(entryRVA), imp);
                memToFunc.put(rva.rvaToMemory(entryRVA), imp.functionNames.get(nameIndex++));
            } while(true);
        }
    }

    public String getDLLName(long memAddress) {
        if(memToImport.containsKey(memAddress)) {
            return memToImport.get(memAddress).dllName;
        } else {
            return null;
        }
    }

    public String getFunctionName(long memAddress) {
        if(memToFunc.containsKey(memAddress)) {
            return memToFunc.get(memAddress);
        } else {
            return null;
        }
    }

    public Map<Long, String> getAllImports() {
        Map<Long, String> res = new HashMap<>();

        for(Long mem : memToFunc.keySet()) {
            res.put(mem, memToFunc.get(mem));
        }

        return res;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        List<Long> memLocs = new ArrayList<>(memToFunc.keySet());
        Collections.sort(memLocs);
        for(Long mem : memLocs) {
            res.append(String.format("%08X", mem) + ": " + memToImport.get(mem).dllName + " -> " + memToFunc.get(mem) + "\n");
        }

        return res.toString();
    }
}
