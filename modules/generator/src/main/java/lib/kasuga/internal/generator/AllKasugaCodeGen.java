package lib.kasuga.internal.generator;

import lib.kasuga.internal.generator.generators.reg.RegCodeGen;
import lib.kasuga.internal.generator.generators.rpc.RpcCodeGen;

import java.util.HashMap;

public class AllKasugaCodeGen {
    public static HashMap<String, CodeGenerator> GENERATORS = new HashMap<>();
    static {
        GENERATORS.put("Reg", new RegCodeGen());
        GENERATORS.put("Rpc", new RpcCodeGen());
    }
}
