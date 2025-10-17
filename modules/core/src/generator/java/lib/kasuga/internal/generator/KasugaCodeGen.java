package lib.kasuga.internal.generator;

import java.io.IOException;
import java.nio.file.Path;

public class KasugaCodeGen {
    public static void main(String[] args) throws IOException {
        String mainSrcDir = args[0];
        String generatedDir = args[1];
        System.out.println("Main source directory: " + mainSrcDir);
        RpcCodeGen.generate(Path.of(mainSrcDir), Path.of(generatedDir));
    }
}
