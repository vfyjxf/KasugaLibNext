package lib.kasuga.rendering.models.uml.structure.material;

import com.google.common.collect.ImmutableMap;
import lib.kasuga.rendering.models.uml.structure.material.animators.Animatable;
import lib.kasuga.rendering.models.uml.structure.material.animators.Animator;
import lib.kasuga.rendering.models.uml.structure.material.data.MaterialData;
import lib.kasuga.rendering.models.uml.structure.material.data.SpriteData;
import lib.kasuga.rendering.models.uml.structure.material.data.SpriteSetData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.*;

public class Material implements Animatable, AutoCloseable {

    @Getter
    @NonNull
    private final Texture[] textures;

    @Getter
    private final Map<String, Texture> textureMap;

    @Getter
    private final MaterialData data;

    private final List<SpriteSet> sprites;

    @Setter
    private int currentFrame;

    @Getter
    private final List<Animator> animators;

    public Material(@NotNull Texture[] textures, @Nullable MaterialData data) {
        this.textures = textures;
        this.textureMap = new HashMap<>();
        this.data = data;
        this.sprites = new ArrayList<>();
        this.currentFrame = 0;
        this.animators = new ArrayList<>();
    }

    public void hookTextures() {
        for (Texture texture : textures) {
            if (texture == null) continue;
            textureMap.put(texture.getId(), texture);
        }
    }

    public void addAnimator(Animator animator) {
        if (!animators.contains(animator)) {
            animators.add(animator);
        }
    }

    public void removeAnimator(Animator animator) {
        animators.remove(animator);
        animator.removeAnimatable(this);
    }

    public void addSprite(@NonNull SpriteSet sprite) {
        sprites.add(sprite);
    }

    public int getTextureCount() {
        return textures.length;
    }

    public boolean hasTexture(String id) {
        return textureMap.containsKey(id);
    }

    public Texture getTexture(String id) {
        return textureMap.get(id);
    }

    public Texture getTextureOrDefault(String id, Texture defaultTexture) {
        return textureMap.getOrDefault(id, defaultTexture);
    }

    @Override
    public List<SpriteSet> getSprites() {
        return sprites;
    }

    @Override
    public int getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void close() throws Exception {
        animators.forEach(a -> a.removeAnimatable(this));
    }
}
