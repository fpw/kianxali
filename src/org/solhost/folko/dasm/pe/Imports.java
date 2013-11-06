package org.solhost.folko.dasm.pe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.solhost.folko.dasm.ByteSequence;

public class Imports {
    private List<Import> imports;
    private Map<Long, Import> memToImport;
    private Map<Long, String> memToFunc;

    private class Import {
        @SuppressWarnings("unused")
        long orgThunk, timeStamp, forwarderChain, nameRVA, firstThunk;
        String dllName;
        Map<Long, String> functions;
    }

    {
        imports = new LinkedList<>();
        memToImport = new HashMap<>();
        memToFunc = new HashMap<>();
    }

    public Imports() {
    }

    public Imports(ByteSequence image, RVAResolver rva) {
        do {
            Import imp = new Import();
            imp.orgThunk       = image.readUDword();
            imp.timeStamp      = image.readUDword();
            imp.forwarderChain = image.readUDword();
            imp.nameRVA        = image.readUDword();
            imp.firstThunk     = image.readUDword();
            if(imp.nameRVA == 0) {
                break;
            }
            imp.functions = new HashMap<>();
            imports.add(imp);
        } while(true);

        for(Import imp : imports) {
            loadImport(image, imp, rva);
        }
        loadIAT(image, rva);
    }

    private void loadImport(ByteSequence image, Import imp, RVAResolver rva) {
        image.seek(rva.rvaToFile(imp.nameRVA));
        imp.dllName = image.readString();

        List<Long> nameHints = new LinkedList<>();
        image.seek(rva.rvaToFile(imp.orgThunk));
        do {
            long nameHintRva = image.readUDword();
            if(nameHintRva == 0) {
                break;
            }
            nameHints.add(nameHintRva);
        } while(true);

        for(Long nameRva : nameHints) {
            if((nameRva & 0x80000000) != 0) {
                // hint only, no name
                imp.functions.put(nameRva, String.format("<anonymous ordinal %04X>", nameRva & 0xFFFF));
            } else {
                image.seek(rva.rvaToFile(nameRva));
                image.readUWord(); // ignore import hint
                imp.functions.put(nameRva, image.readString());
            }
        }
    }

    private void loadIAT(ByteSequence image, RVAResolver rva) {
        for(Import imp : imports) {
            image.seek(rva.rvaToFile(imp.firstThunk));
            do {
                long entryRVA = rva.fileToRVA(image.getPosition());
                long nameHintRva = image.readUDword();
                if(nameHintRva == 0) {
                    break;
                }
                memToImport.put(rva.rvaToMemory(entryRVA), imp);
                for(Long nameRva : imp.functions.keySet()) {
                    if(nameHintRva == nameRva) {
                        memToFunc.put(rva.rvaToMemory(entryRVA), imp.functions.get(nameRva));
                        break;
                    }
                }
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
