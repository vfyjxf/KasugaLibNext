package lib.kasuga.registration.stages;

public class RegistrationStage<T> {
    public static final RegistrationStage<MenuScreenBindingStage> MENU_SCREEN_BINDING = new RegistrationStage<>();
    public static RegistrationStage<RegisterStageContext> REGISTER_EVENT = new RegistrationStage<>();

    public static RegistrationStage<RegisterStageContext> COMMON_SETUP = new RegistrationStage<>();

    public static RegistrationStage<BakingCompleteStage> BAKING_COMPLETE = new RegistrationStage<>();

    public static RegistrationStage<PayloadRegistrationStage> PAYLOAD_REGISTRATION = new RegistrationStage<>();

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
