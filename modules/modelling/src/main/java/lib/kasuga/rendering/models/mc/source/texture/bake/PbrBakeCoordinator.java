package lib.kasuga.rendering.models.mc.source.texture.bake;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public final class PbrBakeCoordinator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final PbrBakeCoordinator INSTANCE = new PbrBakeCoordinator();

    private final Map<String, PbrBakeState> states = new ConcurrentHashMap<>();
    private final Semaphore parallelBakes = new Semaphore(2);
    private final StylizedPbrBaker baker = new StylizedPbrBaker();

    private PbrBakeCoordinator() {}

    public static PbrBakeCoordinator getInstance() {
        return INSTANCE;
    }

    public PbrBakeResult getOrBake(BufferedImage source, PbrBakeProfile profile) {
        String key = cacheKey(source, profile);
        Path directory = cacheDirectory().resolve(key);
        Path normalPath = directory.resolve("normal.png");
        Path specularPath = directory.resolve("specular.png");
        states.put(key, PbrBakeState.QUEUED);

        try {
            if (Files.isRegularFile(normalPath) && Files.isRegularFile(specularPath)) {
                BufferedImage normal = ImageIO.read(normalPath.toFile());
                BufferedImage specular = ImageIO.read(specularPath.toFile());
                if (normal != null && specular != null) {
                    states.put(key, PbrBakeState.READY);
                    return new PbrBakeResult(normal, specular);
                }
            }

            parallelBakes.acquire();
            try {
                states.put(key, PbrBakeState.BAKING);
                PbrBakeResult result = baker.bake(source, profile);
                Files.createDirectories(directory);
                ImageIO.write(result.normalMap(), "png", normalPath.toFile());
                ImageIO.write(result.specularMap(), "png", specularPath.toFile());
                states.put(key, PbrBakeState.READY);
                return result;
            } finally {
                parallelBakes.release();
            }
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
            states.put(key, PbrBakeState.FAILED);
            LOGGER.warn("Failed to bake stylized PBR texture {}; using default PBR maps", key, exception);
            return null;
        }
    }

    public Map<String, PbrBakeState> states() {
        return Map.copyOf(states);
    }

    public void clearCache() throws IOException {
        Path root = cacheDirectory();
        if (Files.notExists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException io) throw io;
            throw exception;
        }
        states.clear();
    }

    private Path cacheDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("cache").resolve("kasuga-pbr");
    }

    private String cacheKey(BufferedImage source, PbrBakeProfile profile) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(profile.cacheDescriptor().getBytes(StandardCharsets.UTF_8));
            ByteBuffer dimensions = ByteBuffer.allocate(8).putInt(source.getWidth()).putInt(source.getHeight());
            digest.update(dimensions.array());
            int[] pixels = source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth());
            ByteBuffer pixelBuffer = ByteBuffer.allocate(pixels.length * Integer.BYTES);
            for (int pixel : pixels) pixelBuffer.putInt(pixel);
            digest.update(pixelBuffer.array());
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
