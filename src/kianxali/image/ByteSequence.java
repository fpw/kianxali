package kianxali.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

// Not using memory mapped I/O because we want to store the full file inside the Kianxali data file
// so the user needn't store the original file along with the Kianxali file
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

    public static ByteSequence fromFile(Path path) throws IOException {
        File file = path.toFile();
        FileInputStream fileStream = new FileInputStream(file);
        byte[] input = new byte[(int) file.length()];
        fileStream.read(input);
        fileStream.close();
        return new ByteSequence(input, false);
    }

    public static ByteSequence fromBytes(byte[] bytes) {
        return new ByteSequence(bytes, true);
    }

    public void patch(long offset, byte b) {
        data[(int) offset] = b;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void setByteOrder(ByteOrder endian) {
        bytes.order(endian);
    }

    public void seek(long offset) {
        bytes.position((int) offset);
    }

    public void skip(long amount) {
        bytes.position((int) (bytes.position() + amount));
    }

    public long getPosition() {
        return bytes.position();
    }

    public boolean hasMore() {
        return bytes.hasRemaining();
    }

    public int getRemaining() {
        return bytes.remaining();
    }

    public short readUByte() {
        return (short) (bytes.get() & 0xFF);
    }

    public byte readSByte() {
        return bytes.get();
    }

    public int readUWord() {
        return bytes.getShort() & 0xFFFF;
    }

    public long readSWord() {
        return bytes.getShort();
    }

    public long readUDword() {
        return bytes.getInt() & 0xFFFFFFFFL;
    }

    public long readSDword() {
        return bytes.getInt();
    }

    public long readSQword() {
        return bytes.getLong();
    }

    public float readFloat() {
        return bytes.getFloat();
    }

    public double readDouble() {
        return bytes.getDouble();
    }

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

    public void savePatched(Path path) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path.toFile(), "rws");
        FileChannel channel = file.getChannel();

        bytes.rewind();
        channel.write(bytes);

        channel.close();
        file.close();
    }
}
