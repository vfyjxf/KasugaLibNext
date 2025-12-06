package lib.kasuga.slp.javet.downloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ProxyCountingInputStream extends FilterInputStream {

    // 使用 AtomicLong 确保计数操作的原子性（虽然在这个单线程场景中不是必须，但更安全）
    private final AtomicLong count = new AtomicLong(0);

    public ProxyCountingInputStream(InputStream in) {
        super(in);
    }

    public long getByteCount() {
        return count.get();
    }

    // 覆盖 read() 方法
    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            count.incrementAndGet();
        }
        return result;
    }

    // 覆盖 read(byte[] b, int off, int len) 方法 (这是 transferTo 内部会调用的主要方法之一)
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            count.addAndGet(result);
        }
        return result;
    }

    // 覆盖 readAllBytes() (Java 9+)
    @Override
    public byte[] readAllBytes() throws IOException {
        // 通常应该避免使用这个方法来计数，因为它会一次性读取所有内容，但为了完整性...
        byte[] result = super.readAllBytes();
        count.addAndGet(result.length);
        return result;
    }

    // **关键：继承 FilterInputStream 默认的 markSupported() 和 mark() / reset() 行为**
    // 很多敏感流不支持 mark/reset，默认继承可以避免引入不必要的缓冲逻辑。
}