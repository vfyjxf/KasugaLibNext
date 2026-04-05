package lib.kasuga.rendering.models.uml.typo.mtl.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;
import lib.kasuga.rendering.models.uml.typo.ObjTextureData;
import lib.kasuga.rendering.models.uml.typo.mtl.MtlContext;
import lib.kasuga.rendering.models.uml.typo.mtl.MtlKeyProcessor;
import lib.kasuga.rendering.models.uml.typo.processors.ObjKeyProcessor;

public class MtlObjectProcessor extends MtlKeyProcessor {

    public MtlObjectProcessor() {
        super("newmtl");
    }

    @Override
    public void process(String[] data, SerialContext<MtlContext> context) {
        MtlContext contextData = new MtlContext(MtlMapProcessor.removeCtrlCodes(data[1]));
        context.push(contextData);
    }
}
