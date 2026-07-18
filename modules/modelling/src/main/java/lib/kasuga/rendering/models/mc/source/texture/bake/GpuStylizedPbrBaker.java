package lib.kasuga.rendering.models.mc.source.texture.bake;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/** Render-thread, off-screen implementation of the stylized PBR baker. */
public final class GpuStylizedPbrBaker implements PbrBaker, AutoCloseable {
    private static final String VERTEX_SHADER = """
            #version 150
            out vec2 texCoord;
            void main() {
                vec2 positions[3] = vec2[3](
                    vec2(-1.0, -1.0), vec2(3.0, -1.0), vec2(-1.0, 3.0)
                );
                vec2 position = positions[gl_VertexID];
                texCoord = position * 0.5 + 0.5;
                gl_Position = vec4(position, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 150
            uniform sampler2D sourceTexture;
            uniform float smoothness;
            uniform float f0Code;
            uniform float sssCode;
            uniform float normalStrength;
            uniform float emission;
            in vec2 texCoord;
            out vec4 normalOut;
            out vec4 specularOut;

            float luminance(vec3 color) {
                return dot(color, vec3(0.2126, 0.7152, 0.0722));
            }

            void main() {
                vec2 texel = 1.0 / vec2(textureSize(sourceTexture, 0));
                float left = luminance(texture(sourceTexture, texCoord - vec2(texel.x, 0.0)).rgb);
                float right = luminance(texture(sourceTexture, texCoord + vec2(texel.x, 0.0)).rgb);
                float up = luminance(texture(sourceTexture, texCoord - vec2(0.0, texel.y)).rgb);
                float down = luminance(texture(sourceTexture, texCoord + vec2(0.0, texel.y)).rgb);
                vec2 normalXY = clamp(vec2(left - right, up - down) * normalStrength, -1.0, 1.0);
                normalOut = vec4(normalXY * 0.5 + 0.5, 1.0, 1.0);
                specularOut = vec4(smoothness, f0Code, sssCode, emission);
            }
            """;

    private int program;
    private int vertexArray;

    @Override
    public PbrBakeResult bake(BufferedImage source, PbrBakeProfile profile) {
        RenderSystem.assertOnRenderThread();
        ensureProgram();

        int width = source.getWidth();
        int height = source.getHeight();
        int framebuffer = 0;
        int sourceTexture = 0;
        int normalTexture = 0;
        int specularTexture = 0;
        ByteBuffer sourcePixels = null;
        ByteBuffer outputPixels = null;

        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int previousVertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int previousPackAlignment = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
        int previousUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer viewport = stack.mallocInt(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            int previousTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            try {
                sourcePixels = imageToRgba(source);
                sourceTexture = createTexture(width, height, sourcePixels, true);
                normalTexture = createTexture(width, height, null, false);
                specularTexture = createTexture(width, height, null, false);

                framebuffer = GL30.glGenFramebuffers();
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                        GL11.GL_TEXTURE_2D, normalTexture, 0);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1,
                        GL11.GL_TEXTURE_2D, specularTexture, 0);
                GL20.glDrawBuffers(stack.ints(GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1));
                int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
                if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                    throw new IllegalStateException("PBR bake framebuffer is incomplete: " + status);
                }

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glViewport(0, 0, width, height);
                GL20.glUseProgram(program);
                GL30.glBindVertexArray(vertexArray);
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);

                GL20.glUniform1i(GL20.glGetUniformLocation(program, "sourceTexture"), 0);
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "smoothness"), profile.smoothness());
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "f0Code"), profile.f0Code() / 255.0f);
                float sssCode = profile.sssStrength() <= 0.0f
                        ? 0.0f
                        : Math.clamp((65.0f + profile.sssStrength() * 190.0f) / 255.0f, 65.0f / 255.0f, 1.0f);
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "sssCode"), sssCode);
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "normalStrength"), profile.normalStrength());
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "emission"), profile.emissionStrength());
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

                GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
                outputPixels = MemoryUtil.memAlloc(width * height * 4);
                BufferedImage normal = readAttachment(GL30.GL_COLOR_ATTACHMENT0, width, height, outputPixels);
                BufferedImage specular = readAttachment(GL30.GL_COLOR_ATTACHMENT1, width, height, outputPixels);
                return new PbrBakeResult(normal, specular);
            } finally {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture0);
                GL11.glViewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
            }
        } finally {
            if (outputPixels != null) MemoryUtil.memFree(outputPixels);
            if (sourcePixels != null) MemoryUtil.memFree(sourcePixels);
            if (framebuffer != 0) GL30.glDeleteFramebuffers(framebuffer);
            if (sourceTexture != 0) GL11.glDeleteTextures(sourceTexture);
            if (normalTexture != 0) GL11.glDeleteTextures(normalTexture);
            if (specularTexture != 0) GL11.glDeleteTextures(specularTexture);
            GL20.glUseProgram(previousProgram);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            GL30.glBindVertexArray(previousVertexArray);
            GL13.glActiveTexture(previousActiveTexture);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, previousPackAlignment);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
            restoreEnabled(GL11.GL_BLEND, blend);
            restoreEnabled(GL11.GL_DEPTH_TEST, depth);
            restoreEnabled(GL11.GL_CULL_FACE, cull);
            restoreEnabled(GL11.GL_SCISSOR_TEST, scissor);
        }
    }

    private void ensureProgram() {
        if (program != 0) return;
        int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        try {
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL30.glBindFragDataLocation(program, 0, "normalOut");
            GL30.glBindFragDataLocation(program, 1, "specularOut");
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                throw new IllegalStateException("Failed to link PBR bake shader: " + GL20.glGetProgramInfoLog(program));
            }
            vertexArray = GL30.glGenVertexArrays();
        } catch (RuntimeException exception) {
            if (program != 0) GL20.glDeleteProgram(program);
            program = 0;
            throw exception;
        } finally {
            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
        }
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Failed to compile PBR bake shader: " + log);
        }
        return shader;
    }

    private static int createTexture(int width, int height, ByteBuffer pixels, boolean source) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, source ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, source ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
        return texture;
    }

    private static ByteBuffer imageToRgba(BufferedImage image) {
        ByteBuffer pixels = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                pixels.put((byte) (argb >>> 16));
                pixels.put((byte) (argb >>> 8));
                pixels.put((byte) argb);
                pixels.put((byte) (argb >>> 24));
            }
        }
        return pixels.flip();
    }

    private static BufferedImage readAttachment(int attachment, int width, int height, ByteBuffer pixels) {
        pixels.clear();
        GL11.glReadBuffer(attachment);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (y * width + x) * 4;
                int r = pixels.get(index) & 0xff;
                int g = pixels.get(index + 1) & 0xff;
                int b = pixels.get(index + 2) & 0xff;
                int a = pixels.get(index + 3) & 0xff;
                image.setRGB(x, y, a << 24 | r << 16 | g << 8 | b);
            }
        }
        return image;
    }

    private static void restoreEnabled(int capability, boolean enabled) {
        if (enabled) GL11.glEnable(capability);
        else GL11.glDisable(capability);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        if (program != 0) GL20.glDeleteProgram(program);
        if (vertexArray != 0) GL30.glDeleteVertexArrays(vertexArray);
        program = 0;
        vertexArray = 0;
    }
}
