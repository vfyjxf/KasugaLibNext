package lib.kasuga.core.saved;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class ExternalSaveItem extends Item {
    public ExternalSaveItem(Properties properties) {
        super(properties);
    }

    public ItemStack initialize(ItemStack input, Consumer<UUID> onCreate) {
        if(input == null || input.has(SavedSystem.ITEM_SAVE_ID.getEntry()))
            return null;
        UUID id = UUID.randomUUID();
        input = input.copy();
        onCreate.accept(id);
        input.set(SavedSystem.ITEM_SAVE_ID.getEntry(), id);
        return input;
    }

    public ItemStack[] initializeOutputs(ItemStack input, @Nullable LivingEntity entity, Consumer<UUID> onCreate) {
        ItemStack stack = initialize(input, onCreate);
        if(stack == null)
            return new ItemStack[]{input};
        stack.setCount(1);
        input.consume(1, entity);
        return new ItemStack[]{input, stack};
    }

    public UUID getSaveId(ItemStack stack){
        DataComponentType<UUID> type = SavedSystem.ITEM_SAVE_ID.getEntry();
        if(!stack.has(type))
            return null;
        return stack.get(type);
    }
}
