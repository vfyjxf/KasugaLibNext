package lib.kasuga.rendering.models.uml.typo.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;

public class ObjUseMtlProcessor extends ObjKeyProcessor {

        public ObjUseMtlProcessor() {
            super("usemtl");
        }

    @Override
    public void process(String[] data, SerialContext<ObjContextData> context) {
        ObjContextData contextData = context.peek();
        contextData.useMtl(data[1]);
    }
}
