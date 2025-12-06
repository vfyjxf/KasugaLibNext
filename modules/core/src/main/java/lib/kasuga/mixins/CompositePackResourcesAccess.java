package lib.kasuga.mixins;

import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CompositePackResources.class)
public interface CompositePackResourcesAccess {
    @Accessor()
    public PackResources getPrimaryPackResources();

    @Accessor()
    public List<PackResources> getPackResourcesStack();
}
