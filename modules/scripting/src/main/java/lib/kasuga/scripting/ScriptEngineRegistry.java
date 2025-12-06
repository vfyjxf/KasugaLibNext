package lib.kasuga.scripting;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ScriptEngineRegistry {
    protected Map<ResourceLocation, Supplier<ScriptEngine>> scriptEngines = new HashMap<>();
}
