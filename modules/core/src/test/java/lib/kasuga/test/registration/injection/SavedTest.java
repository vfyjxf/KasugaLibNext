package lib.kasuga.test.registration.injection;

import jakarta.inject.Inject;
import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibApplication;
import lib.kasuga.inject.auto_configure.Saved;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(EphemeralTestServerProvider.class)
public class SavedTest {

    @Test()
    public void mySavedData(MinecraftServer server) {
        Saved<MySavedData> data = KasugaLib.getContext().getBean(SavedTestModule.class).getMyData();
    }
}
