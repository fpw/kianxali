package kianxali.loader.pe;

public interface AddressConverter {
    long rvaToMemory(long rva);
    long rvaToFile(long rva);
    long fileToRVA(long offset);
    long fileToMemory(long offset);
    long memoryToRVA(long mem);
    long memoryToFile(long mem);
}
