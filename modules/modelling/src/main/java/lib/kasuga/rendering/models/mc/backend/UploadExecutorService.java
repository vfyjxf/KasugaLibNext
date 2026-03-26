package lib.kasuga.rendering.models.mc.backend;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadExecutorService {

    private final ExecutorService threadPool;

    private final HashMap<KsgVertexBuffer, CountDownLatch> runningUploads;

    public UploadExecutorService(int threadCount) {
        this.threadPool = Executors.newFixedThreadPool(threadCount);
        runningUploads = new HashMap<>();
    }

    public void submitUpload(KsgVertexBuffer buffer, Runnable uploadTask) {
        CountDownLatch latch = new CountDownLatch(1);
        runningUploads.put(buffer, latch);
        threadPool.submit(() -> {
            try {
                uploadTask.run();
            } finally {
                latch.countDown();
                runningUploads.remove(buffer);
            }
        });
    }


    public void stop() {
        threadPool.shutdown();
    }
}
