package lib.kasuga.rendering.models.uml.typo.wavefont_obj.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.wavefont_obj.ObjContextData;
import lib.kasuga.rendering.models.uml.typo.wavefont_obj.ObjVertexBinding;

import java.util.ArrayList;

public class ObjBindingProcessor extends ObjKeyProcessor {

    public ObjBindingProcessor() {
        super("f");
    }

    @Override
    public void process(String[] data, SerialContext<ObjContextData> context) {
        ArrayList<ObjVertexBinding> bindings = new ArrayList<>();
        for (int i = 1; i < data.length; i++) {
            String[] bindingData = data[i].split("/");
            int vertexIndex = Integer.parseInt(bindingData[0]) - 1;
            int textureIndex = bindingData.length > 1 && !bindingData[1].isEmpty() ? Integer.parseInt(bindingData[1]) - 1 : -1;
            int normalIndex = bindingData.length > 2 && !bindingData[2].isEmpty() ? Integer.parseInt(bindingData[2]) - 1 : -1;
            bindings.add(new ObjVertexBinding(vertexIndex, textureIndex, normalIndex));
        }
        context.peek().f(bindings);
    }
}
