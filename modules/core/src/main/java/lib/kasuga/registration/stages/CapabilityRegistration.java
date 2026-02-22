package lib.kasuga.registration.stages;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilityRegistration {
    protected RegisterCapabilitiesEvent event;
    public CapabilityRegistration(RegisterCapabilitiesEvent event) {
        this.event = event;
    }

    public RegisterCapabilitiesEvent getEvent() {
        return event;
    }
}
