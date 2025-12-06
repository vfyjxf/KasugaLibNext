package lib.kasuga.scripting.module;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

public class ModuleDiscovery {


    public static class ProviderContext {

    }

    List<ModuleParser> parsers = new ArrayList<>();

    public void parse(ModuleProvider provider) {
        ProviderContext context = new ProviderContext();
    }
}
