package lib.kasuga.rendering.models.uml.typo.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;

public class ObjSmoothShadeProcessor extends ObjKeyProcessor {

    public ObjSmoothShadeProcessor() {
        super("s");
    }

    @Override
    public void process(String[] data, SerialContext<ObjContextData> context) {
        ObjContextData contextData = context.peek();
        String dataStr = data[1];
        contextData.s(
                dataStr.equals("on") || dataStr.equals("1") ||
                dataStr.compareToIgnoreCase("true") == 0
        );
    }
}
