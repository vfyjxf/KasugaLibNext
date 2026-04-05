package lib.kasuga.rendering.models.uml.typo.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;
import org.joml.Vector3f;

public class ObjNormalProcessor extends ObjKeyProcessor {

    public ObjNormalProcessor() {
        super("vn");
    }

    @Override
    public void process(String[] data, SerialContext<ObjContextData> context) {
        ObjContextData contextData = context.peek();
        contextData.vn(
                new Vector3f(
                        Float.parseFloat(data[1]),
                        Float.parseFloat(data[2]),
                        Float.parseFloat(data[3])
                )
        );
    }
}
