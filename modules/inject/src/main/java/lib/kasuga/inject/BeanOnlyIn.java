package lib.kasuga.inject;

import io.micronaut.context.annotation.Requires;

import java.lang.annotation.*;

public @interface BeanOnlyIn {
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Requires(condition = MinecraftDist.IsClient.class)
    public @interface Client {}

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Requires(condition = MinecraftDist.IsDedicatedServer.class)
    public @interface DedicatedServer {}

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Requires(condition = MinecraftDist.IsDevelopment.class)
    public @interface Development {}

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Requires(condition = MinecraftDist.Never.class)
    public @interface Never {}
}
