package lib.kasuga.slp.javet.downloader;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.loader.IJavetLibLoadingListener;
import com.caoccao.javet.interop.loader.JavetLibLoader;
import com.caoccao.javet.utils.JavetOSUtils;
import com.mojang.logging.LogUtils;
import lib.kasuga.early.ModLoadingManager;
import lib.kasuga.early.ModLoadingProgress;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.zip.ZipInputStream;

public class JavetDownloader implements IJavetLibLoadingListener {
    private static Logger LOGGER = LogUtils.getLogger();

    protected static volatile boolean isLibraryPresent = false;

    protected static final Object lock = new Object();

    public static boolean libraryDownloaded() {
        return isLibraryPresent;
    }

    public record Source(String sourceName, MessageFormat normalUrl){
        private String getOSArch() {
            // Copied from JaVeT's Lib Loader (JavetLibLoader.java)
            if (JavetOSUtils.IS_WINDOWS) {
                return "x86_64";
            } else if (JavetOSUtils.IS_LINUX) {
                return JavetOSUtils.IS_ARM64 ? "arm64" : "x86_64";
            } else if (JavetOSUtils.IS_MACOS) {
                return JavetOSUtils.IS_ARM64 ? "arm64" : "x86_64";
            } else {
                if (JavetOSUtils.IS_ANDROID) {
                    if (JavetOSUtils.IS_ARM) {
                        return "arm";
                    }

                    if (JavetOSUtils.IS_ARM64) {
                        return "arm64";
                    }

                    if (JavetOSUtils.IS_X86) {
                        return "x86";
                    }

                    if (JavetOSUtils.IS_X86_64) {
                        return "x86_64";
                    }
                }

                return null;
            }
        }
        private String getOSName() {
            // Copied from JaVeT's Lib Loader (JavetLibLoader.java)
            if (JavetOSUtils.IS_WINDOWS) {
                return "windows";
            } else if (JavetOSUtils.IS_LINUX) {
                return "linux";
            } else if (JavetOSUtils.IS_MACOS) {
                return "macos";
            } else {
                return JavetOSUtils.IS_ANDROID ? "android" : null;
            }
        }

        public String getUrl(JSRuntimeType runtimeType) {
            return normalUrl.format(
                new Object[]{
                    runtimeType.getName(),
                    getOSName(),
                    getOSArch(),
                    runtimeType.isI18nEnabled() ? "-i18n" : "",
                    JavetLibLoader.LIB_VERSION
                }
            );
        }
    }

    protected static List<Source> SOURCES = List.of(
      new Source("Alibaba Cloud Mirror", new MessageFormat("https://maven.aliyun.com/repository/public/com/caoccao/javet/javet-{0}-{1}-{2}{3}/{4}/javet-{0}-{1}-{2}{3}-{4}.jar")),
      new Source("Maven Repository", new MessageFormat("https://repo1.maven.org/maven2/com/caoccao/javet/com/caoccao/javet/javet-{0}-{1}-{2}{3}/{4}/javet-{0}-{1}-{2}{3}-{4}.jar"))
    );

    public static File tryDownload(JSRuntimeType runtimeType) {
        JavetLibLoader loader = new JavetLibLoader(runtimeType);
        File v8Path = new File(FMLLoader.getGamePath().toFile(), "native-libraries/javet");

        if(!v8Path.exists()){
            v8Path.mkdirs();
        }

        String fileName = "";

        if(JavetOSUtils.IS_ANDROID || JavetOSUtils.OS_NAME.contains("Android")) {
            throw new RuntimeException("Invalid OS/Architecture for JaVeT, please using the GraalJS mode instead.");
        }

        try{
            fileName = loader.getLibFileName();
        }catch (JavetException exception) {
            throw new RuntimeException("Invalid OS/Architecture for JaVeT, please using the GraalJS mode instead.", exception);
        }

        File v8File = new File(v8Path, fileName);

        if(v8File.exists())
            return v8Path;

        for (int i = 0; i < 3; i++) {
            ModLoadingProgress progress = ModLoadingManager.createTask("Fetching JaVeT V8 Engine URL", 100 * 1000);

            try{
                download(v8File, fileName, loader, progress);
            }finally {
                progress.complete();
            }
        }

        return v8Path;
    }

    private static File download(File v8File, String fileName, JavetLibLoader loader, ModLoadingProgress progress) {
        for(Source source : SOURCES) {
            progress.label("Fetching JaVeT V8 Engine From " + source.sourceName);
            String url = source.getUrl(loader.getJSRuntimeType());

            // **获取内容长度 (Content-Length)**
            long totalBytes = -1;
            File downloadFile = null;
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (var response = httpClient.execute(httpGet)) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                        org.apache.http.Header contentLengthHeader = response.getFirstHeader("Content-Length");
                        if (contentLengthHeader != null) {
                            try {
                                totalBytes = Math.max(Long.parseLong(contentLengthHeader.getValue()), totalBytes);
                            } catch (NumberFormatException ignored) {}
                        }

                        try (var inputStream = response.getEntity().getContent();
                             ProxyCountingInputStream counter = new ProxyCountingInputStream(inputStream);
                             ZipInputStream zipInputStream = new ZipInputStream(counter)
                        ) {
                            String name = zipInputStream.getNextEntry().getName();
                            System.out.println("File: {}".formatted(name));
                            if(name.contains(fileName)) {

                                downloadFile = new File(v8File.getParentFile(), v8File.getName() + "_" + ProcessHandle.current().pid() + "_" + Thread.currentThread().threadId() + ".tmp");
                                FileOutputStream outputStream = new FileOutputStream(downloadFile);

                                // **手动传输并更新进度**
                                byte[] buffer = new byte[8192];
                                int bytesRead;

                                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                    long bytesTransferred = counter.getByteCount();

                                    // **更新进度**
                                    if (totalBytes > 0) {
                                        // 仅当知道总大小时才计算百分比
                                        // 计算百分比: (bytesTransferred / totalBytes) * 100
                                        double percentage = (double) bytesTransferred / totalBytes;
                                        // progress.set(percentage * 1000);
                                        // 进度条显示从 0 到 1000
                                        int progressValue = (int) (percentage * 1000 * 100);
                                        progress.set(progressValue);
                                        progress.label("Downloading JaVeT: %.1f%% (%d KB / %d KB)".formatted(
                                                percentage * 100,
                                                bytesTransferred / 1024,
                                                totalBytes / 1024
                                        ));
                                    } else {
                                        // 如果不知道总大小，只显示已传输的字节数
                                        progress.label("Downloading JaVeT: %d KB downloaded".formatted(
                                                bytesTransferred / 1024
                                        ));
                                    }
                                }

                                outputStream.close();
                                progress.complete(); // 传输完成

                                if(v8File.exists())
                                    return v8File;

                                if(!downloadFile.renameTo(v8File)) {
                                    throw new RuntimeException("Failed to move the download file! Check the permission");
                                }

                                return v8File;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to download from " + source.sourceName, e);
                try{
                    if(downloadFile != null) downloadFile.delete();
                } catch (Exception ignored) {}
            }
        }
        progress.complete();
        throw new RuntimeException("Failed to download the V8 library. Please check your network connection or try again later. Or you can download the libjavet-xxx-xxxx from mavens and extract to [version dir]/native-libraries/javet/");
    }

    @Override
    public File getLibPath(JSRuntimeType jsRuntimeType) {
        File f = tryDownload(jsRuntimeType);
        isLibraryPresent = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        return f;
    }

    public static void waitForDownload() {
        synchronized (lock) {
            while (!isLibraryPresent) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean isDeploy(JSRuntimeType jsRuntimeType) {
        return false;
    }
}
