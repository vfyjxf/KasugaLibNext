package lib.kasuga.registration.data_driven;

import com.google.gson.JsonObject;

import java.util.List;

public interface TypeHandler<T> {

    String getTypeName();

    int getPhase();

    T parse(JsonObject json);

    void apply(T definition, BuildContext context);

    default String getParentTypeName() { return null; }

    default List<JsonObject> extractEmbedded(JsonObject parentJson) { return null; }
}
