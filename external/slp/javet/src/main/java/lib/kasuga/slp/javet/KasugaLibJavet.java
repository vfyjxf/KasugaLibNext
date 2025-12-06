package lib.kasuga.slp.javet;

import com.caoccao.javet.interop.loader.JavetLibLoader;
import lib.kasuga.slp.javet.downloader.JavetDownloader;
import net.neoforged.fml.common.Mod;

@Mod(KasugaLibJavet.MODID)
public class KasugaLibJavet {
    public static final String MODID = "kasuga_lib_javet";
    public KasugaLibJavet() {
        // ScriptEngineType.ScriptEngineTypeBuilder<JavetScriptEngine> engine = ScriptEngineType.builder(JavetScriptEngine::new);
        JavetLibLoader.setLibLoadingListener(new JavetDownloader());
    }
}
