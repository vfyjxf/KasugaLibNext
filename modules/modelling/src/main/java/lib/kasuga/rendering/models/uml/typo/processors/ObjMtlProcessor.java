package lib.kasuga.rendering.models.uml.typo.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;
import lib.kasuga.rendering.models.uml.typo.ObjModelLoader;

public class ObjMtlProcessor extends ObjKeyProcessor {

    public ObjMtlProcessor() {
        super("mtllib");
    }

    @Override
    public void process(String[] data, SerialContext<ObjContextData> context) {
        ObjModelLoader loader = (ObjModelLoader) context.getLoader();
        loader.setMtllibURL(data[1].trim());
        loader.loadMtl();
    }
}
