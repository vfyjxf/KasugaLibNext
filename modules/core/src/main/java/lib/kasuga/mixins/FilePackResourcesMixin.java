package lib.kasuga.mixins;

import net.minecraft.server.packs.FilePackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FilePackResources.class)
public interface FilePackResourcesMixin {
    @Invoker()
    public String invokeAddPrefix(String resourcesPath);

    @Accessor()
    public FilePackResources.SharedZipFileAccess getZipFileAccess();
}
