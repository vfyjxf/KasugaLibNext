package lib.kasuga.test.registration.minecraft.entity;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.entity.EntityReg;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class EntityRegistryTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    public static EntityReg<TestEntity> TEST_CREATURE_ENTITY = EntityReg.of("test_creature", 
            MobCategory.CREATURE, TestEntity::new)
            .size(0.6f, 1.8f)
            .setParent(registry);
    
    public static EntityReg<TestProjectileEntity> TEST_PROJECTILE_ENTITY = EntityReg.of("test_projectile",
                    TestProjectileEntity::new)
            .category(MobCategory.MISC)
            .size(0.25f, 0.25f)
            .setParent(registry);
    
    public static EntityReg<TestVehicleEntity> TEST_VEHICLE_ENTITY = EntityReg.of("test_vehicle",
                    TestVehicleEntity::new)
            .category(MobCategory.MISC)
            .size(1.0f, 0.5f)
            .setParent(registry);

    @Test
    public void testEntityRegistry(MinecraftServer server) {
        // Test that entities are registered
        assert TEST_CREATURE_ENTITY.getEntry() != null;
        assert TEST_PROJECTILE_ENTITY.getEntry() != null;
        assert TEST_VEHICLE_ENTITY.getEntry() != null;
        
        // Test registry contains the entities
        var entityRegistry = server.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        assert entityRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_creature"));
        assert entityRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_projectile"));
        assert entityRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_vehicle"));
        
        // Test entity categories
        EntityType<?> creatureType = entityRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_creature"));
        EntityType<?> projectileType = entityRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_projectile"));
        EntityType<?> vehicleType = entityRegistry.get(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_vehicle"));
        
        assert creatureType != null && creatureType.getCategory() == MobCategory.CREATURE;
        assert projectileType != null && projectileType.getCategory() == MobCategory.MISC;
        assert vehicleType != null && vehicleType.getCategory() == MobCategory.MISC;
        
        // Test entity dimensions
        assert creatureType.getWidth() == 0.6f;
        assert creatureType.getHeight() == 1.8f;
        assert projectileType.getWidth() == 0.25f;
        assert projectileType.getHeight() == 0.25f;
        assert vehicleType.getWidth() == 1.0f;
        assert vehicleType.getHeight() == 0.5f;
    }
}
