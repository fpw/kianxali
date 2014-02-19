package kianxali.image;

public interface Section {
    String getName();
    boolean isExecutable();
    long getStartAddress();
    long getEndAddress();
}
