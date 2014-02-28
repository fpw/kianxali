package kianxali.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents a stream of bytes and allows to read the standard x86
 * data types from the stream. The file is completely loaded into an array instead
 * of using memory mapped I/O because it should be possible to store data files
 * of the disassembler that can be loaded without having the actual image file.
 * @author fwi
 *
 */
public final class ByteSequence {
    private final byte[] data;
    private final ByteBuffer bytes;
    private final ReentrantLock lock;

    private ByteSequence(byte[] input, boolean doCopy) {
        if(doCopy) {
            this.data = new byte[input.length];
            System.arraycopy(input, 0, data, 0, input.length);
        } else {
            this.data = input;
        }
        this.bytes = ByteBuffer.wrap(data);
        this.bytes.order(ByteOrder.LITTLE_ENDIAN);
        this.lock = new ReentrantLock();
    }

    /**
     * Construct a new byte sequence from a given path.
     * @param path the path describing the file to be opened
     * @return the byte sequence for the file
     * @throws IOException if the file couldn't be read
     */
    public static ByteSequence fromFile(Path path) throws IOException {
        File file = path.toFile();
        FileInputStream fileStream = new FileInputStream(file);
        byte[] input = new byte[(int) file.length()];
        fileStream.read(input);
        fileStream.close();
        return new ByteSequence(input, false);
    }

    /**
     * Construct a new byte sequence from a given byte array. The byte array
     * will be copied, i.e. the changes will not be reflected in either direction
     * @param bytes the byte array to use
     * @return the byte sequence containing the array
     */
    public static ByteSequence fromBytes(byte[] bytes) {
        return new ByteSequence(bytes, true);
    }

    /**
     * Applies a patch to the byte sequence. This only happens in memory.
     * @param offset the file offset to patch
     * @param b the byte to write at the given offset
     */
    public void patch(long offset, byte b) {
        data[(int) offset] = b;
    }

    /**
     * Applies a patch to the current location.
     * @param b the byte to write at the current location
     */
    public void patch(byte b) {
        data[(int) getPosition()] = b;
    }

    /**
     * Attempts to lock the byte sequence. Note that the lock only
     * applies to other threads that also use lock, i.e. it's still possible
     * to access the byte sequence without a lock.
     */
    public void lock() {
        lock.lock();
    }

    /**
     * Unlocks the byte sequence. Must always be called after lock.
     */
    public void unlock() {
        lock.unlock();
    }

    /**
     * Sets the byte order of the sequence. Affects the read and write operations.
     * @param endian the byte order to use
     */
    public void setByteOrder(ByteOrder endian) {
        bytes.order(endian);
    }

    /**
     * Seek to a given offset in the file
     * @param offset the offset to position the cursor to
     */
    public void seek(long offset) {
        bytes.position((int) offset);
    }

    /**
     * Lets the cursor skip the given amount of bytes
     * @param amount the number of bytes to skip the cursor
     */
    public void skip(long amount) {
        bytes.position((int) (bytes.position() + amount));
    }

    /**
     * Returns the current position of the cursor
     * @return the cursor's offset in the byte array
     */
    public long getPosition() {
        return bytes.position();
    }

    /**
     * Returns whether there are more bytes after the cursor
     * @return true iff at least one byte can be read after the cursor
     */
    public boolean hasMore() {
        return bytes.hasRemaining();
    }

    /**
     * Returns the number of bytes that can be read after the cursor
     * @return the number of bytes that can be read after the cursor
     */
    public int getRemaining() {
        return bytes.remaining();
    }

    /**
     * Reads a unsigned byte from the current cursor position
     * @return the byte read
     */
    public short readUByte() {
        return (short) (bytes.get() & 0xFF);
    }

    /**
     * Reads a signed byte from the current cursor position
     * @return the byte read
     */
    public byte readSByte() {
        return bytes.get();
    }

    /**
     * Reads a unsigned word (16 bit) from the current cursor position
     * @return the word read
     */
    public int readUWord() {
        return bytes.getShort() & 0xFFFF;
    }

    /**
     * Reads a signed word (16 bit) from the current cursor position
     * @return the word read
     */
    public short readSWord() {
        return bytes.getShort();
    }

    /**
     * Reads a unsigned dword (32 bit) from the current cursor position
     * @return the dword read
     */
    public long readUDword() {
        return bytes.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Reads a signed dword (32 bit) from the current cursor position
     * @return the dword read
     */
    public int readSDword() {
        return bytes.getInt();
    }

    /**
     * Reads a signed qword (64 bit) from the current cursor position
     * @return the qword read
     */
    public long readSQword() {
        return bytes.getLong();
    }

    /**
     * Reads a float from the current cursor position
     * @return the float read
     */
    public float readFloat() {
        return bytes.getFloat();
    }

    /**
     * Reads a double from the current cursor position
     * @return the double read
     */
    public double readDouble() {
        return bytes.getDouble();
    }

    /**
     * Reads a null-terminated ASCII string from the current cursor position
     * @return the string read
     */
    public String readString() {
        StringBuilder res = new StringBuilder();
        do {
            byte b = bytes.get();
            if(b != 0) {
                res.append((char) b);
            } else {
                break;
            }
        } while(true);

        return res.toString();
    }

    /**
     * Reads an ASCII string with a given length from the current cursor position.
     * If there is a 0 character in the string, it will be skipped
     * @return the string read
     */
    public String readString(int maxLen) {
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < maxLen; i++) {
            byte b = bytes.get();
            if(b != 0) {
                res.append((char) b);
            }
        }
        return res.toString();
    }

    /**
     * Saves a version of the byte array that contains all the patches
     * @param path the path to save the array to
     * @throws IOException if the file couldn't be written
     */
    public void savePatched(Path path) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path.toFile(), "rws");
        FileChannel channel = file.getChannel();

        bytes.rewind();
        channel.write(bytes);

        channel.close();
        file.close();
    }
}
