package lib.kasuga.test.registration.minecraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TestVehicleEntity extends Entity {
    public TestVehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Implementation for test vehicle entity
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Implementation for test vehicle entity
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Implementation for test vehicle entity  
    }
}
