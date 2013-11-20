package kianxali.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

public final class ByteSequence {
    private final ByteBuffer bytes;
    private final ReentrantLock lock;

    private ByteSequence(ByteBuffer buffer) {
        this.bytes = buffer;
        this.bytes.order(ByteOrder.LITTLE_ENDIAN);
        this.lock = new ReentrantLock();
    }

    public static ByteSequence fromFile(Path path) throws IOException {
        FileInputStream fileStream = new FileInputStream(path.toFile());
        FileChannel chan = fileStream.getChannel();
        ByteBuffer imageBuffer = chan.map(MapMode.READ_ONLY, 0, fileStream.available());
        chan.close();
        fileStream.close();
        return new ByteSequence(imageBuffer);
    }

    public static ByteSequence fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.rewind();
        return new ByteSequence(buffer);
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

    public short readSByte() {
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
}
