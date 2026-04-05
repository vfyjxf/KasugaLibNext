package lib.kasuga.rendering.models.uml.loaders.serial;

import lib.kasuga.rendering.models.uml.loaders.ModelLoader;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceManager;
import lib.kasuga.rendering.models.uml.loaders.sources.SourceType;
import lib.kasuga.rendering.models.uml.structure.Model;
import lib.kasuga.rendering.models.uml.structure.basic.data.BoneBindingData;
import lib.kasuga.rendering.models.uml.structure.basic.data.mesh.MeshData;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.VertexData;
import lib.kasuga.rendering.models.uml.structure.data.ModelData;
import lib.kasuga.rendering.models.uml.structure.material.data.TextureData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.AnchorData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.SkeletonData;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class TextStreamLoader<
        A extends ModelData, B extends BoneData, C extends MeshData, D extends VertexData,
        E extends BoneBindingData, F extends TextureData, G extends SkeletonData, H extends AnchorData,
        I, S, M extends ContextData<M>> implements ModelLoader<A, B, C, D, E, F, G, H, I, S> {

    @Getter
    private final String separator;

    @Getter
    private final HashMap<String, LineProcessor> processors;

    @Getter
    private final boolean trim;

    @Getter
    private final String name;

    @Getter
    private SerialContext<M> context;

    @Getter
    private final HashMap<SourceType, HashMap<String, SourceManager<?>>> sidedSourceManagers;

    public TextStreamLoader(String name, String separator, boolean trim) {
        this.separator = separator;
        this.processors = new HashMap<>();
        this.trim = trim;
        this.name = name;
        sidedSourceManagers =  new HashMap<>();
    }

    public void registerProcessor(String key, LineProcessor processor) {
        processors.put(key, processor);
    }

    public abstract String getAsString(I input);

    public String[] split(String input) {
        String[] result = input.split(separator);
        if (!trim) return result;
        for (int i = 0; i < result.length; i++) {
            result[i] = innerTrim(result[i]);
        }
        return result;
    }

    @Override
    public Map<S, Model<A, B, C, D, F, G, E, H>> load(S identifier, I input) {
        HashMap<S, Model<A, B, C, D, F, G, E, H>> result = new HashMap<>();
        String[] str = getString(input);
        LineProcessor lastProcessor = null;
        context = new SerialContext<>(this);
        for (String line : str) {
            if (lastProcessor != null && lastProcessor.isValidInput(line, context)) {
                lastProcessor.input(line, context);
                continue;
            }
            for (LineProcessor processor : processors.values()) {
                if (processor == lastProcessor) continue;
                if (processor.isValidInput(line, context)) {
                    processor.input(line, context);
                    lastProcessor = processor;
                    break;
                }
            }
        }
        build(result);
        return result;
    }

    public abstract void build(HashMap<S, Model<A, B, C, D, F, G, E, H>> result);

    public String[] getString(I input) {
        String str = getAsString(input);
        if (str == null) return new String[0];
        return split(str);
    }

    public static String innerTrim(String str) {
        String cache = str.trim();
        while (cache.contains("  ")) {
            cache = cache.replace("  ", " ");
        }
        return cache;
    }

    @Override
    public HashMap<SourceType, HashMap<String, SourceManager<?>>> getSidedSources() {
        return getSidedSourceManagers();
    }
}
