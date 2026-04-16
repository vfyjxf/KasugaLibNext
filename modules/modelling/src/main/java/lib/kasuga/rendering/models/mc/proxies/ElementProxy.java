package lib.kasuga.rendering.models.mc.proxies;

public interface ElementProxy<ProxiedType, ProxiedInstanceType> {

    boolean isValidInput(Object input);

    boolean isValidInstance(Object instance);
}
