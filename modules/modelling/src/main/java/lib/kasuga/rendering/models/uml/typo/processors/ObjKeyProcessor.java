package lib.kasuga.rendering.models.uml.typo.processors;

import lib.kasuga.rendering.models.uml.loaders.serial.LineSepProcessor;
import lib.kasuga.rendering.models.uml.loaders.serial.SerialContext;
import lib.kasuga.rendering.models.uml.typo.ObjContextData;
import lombok.Getter;

public abstract class ObjKeyProcessor extends LineSepProcessor<ObjContextData> {

    @Getter
    private final String key;

    public ObjKeyProcessor(String key) {
        super(" ", true);
        this.key = key;
    }

    @Override
    public abstract void process(String[] data, SerialContext<ObjContextData> context);

    @Override
    public boolean isValidInput(String[] data) {
        return data.length > 0 && data[0].equals(key);
    }
}
