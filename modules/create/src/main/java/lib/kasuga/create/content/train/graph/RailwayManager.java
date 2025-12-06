package lib.kasuga.create.content.train.graph;

import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import lib.kasuga.inject.auto_configure.Saved;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

@Context
public class RailwayManager {
    Saved<RailwayData> railwayData = new Saved.Impl<>("kasuga_railway_data", new SavedData.Factory<>(
            RailwayData::new,
            RailwayData::load
    ));

    public RailwayData getData() {
        return railwayData.get();
    }

    public void load(MinecraftServer server) {
        railwayData.onLoad(server);
    }
}
