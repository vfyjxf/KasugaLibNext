package lib.kasuga.test.registration.injection;

import io.micronaut.context.annotation.Context;
import jakarta.inject.Inject;
import lib.kasuga.inject.auto_configure.Saved;

@Context
public class SavedTestModule {

    @Inject()
    Saved<MySavedData> myData;

    public Saved<MySavedData> getMyData() {
        return myData;
    }
}
