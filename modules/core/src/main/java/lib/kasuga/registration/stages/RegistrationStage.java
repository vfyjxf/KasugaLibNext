package lib.kasuga.registration.stages;

public class RegistrationStage<T> {
    public static RegistrationStage<RegisterStageContext> REGISTER_EVENT = new RegistrationStage<>();

    public static RegistrationStage<RegisterStageContext> COMMON_SETUP = new RegistrationStage<>();

    public static RegistrationStage<BakingCompleteStage> BAKING_COMPLETE = new RegistrationStage<>();

    public static RegistrationStage<PayloadRegistrationStage> PAYLOAD_REGISTRATION = new RegistrationStage<>();
}
