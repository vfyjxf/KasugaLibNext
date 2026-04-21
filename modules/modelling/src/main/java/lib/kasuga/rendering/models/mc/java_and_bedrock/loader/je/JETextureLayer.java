package lib.kasuga.rendering.models.mc.java_and_bedrock.loader.je;

import com.google.gson.JsonObject;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTexture;
import lib.kasuga.rendering.models.mc.java_and_bedrock.data.MCTextureData;
import lib.kasuga.rendering.models.mc.java_and_bedrock.loader.TextureLayer;
import lib.kasuga.rendering.models.uml.loaders.MaterialSetBuilder;
import lib.kasuga.rendering.models.uml.loaders.SpriteSetBuilder;
import lib.kasuga.rendering.models.uml.loaders.structural.Context;
import lib.kasuga.structure.Pair;
import org.joml.Vector2f;

import java.util.HashMap;

public class JETextureLayer extends TextureLayer {

    @Override
    public void process(JsonObject input, Context context) {
        super.process(input, context);

        HashMap<String, Pair<net.minecraft.client.resources.model.Material, MCTextureData>> textures =
                (HashMap<String, Pair<net.minecraft.client.resources.model.Material, MCTextureData>>) context.getData("textures");
        
        MaterialSetBuilder<String> builder = context.getLoader().getMaterialSetBuilder();
        
        for (HashMap.Entry<String, Pair<net.minecraft.client.resources.model.Material, MCTextureData>> entry : textures.entrySet()) {
            String id = entry.getKey();
            Pair<net.minecraft.client.resources.model.Material, MCTextureData> pair = entry.getValue();
            MCTexture texture = new MCTexture(
                    id, pair::getFirst,
                    16, 16,
                    pair.getSecond());
            builder.registerTexture(id, texture);
        }
        
        String firstTextureId = textures.keySet().iterator().next();
        builder.beginMaterial()
                .useTexture(firstTextureId)
                .addSpriteBuildingFunc((mtlb, sprb, mtl) -> {
                    SpriteSetBuilder<String> sprBuilder = (SpriteSetBuilder<String>) sprb;
                    sprBuilder.textureId(firstTextureId)
                            .rectangularUVs(new Vector2f(), new Vector2f(1f, 1f))
                            .endSprite();
                })
                .endMaterial();
        
        lib.kasuga.rendering.models.uml.structure.material.Material mat =
                (lib.kasuga.rendering.models.uml.structure.material.Material) builder.getMaterials().getFirst();
        context.setData("material", mat);
    }
}
