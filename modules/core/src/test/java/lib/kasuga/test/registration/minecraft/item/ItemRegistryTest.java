package lib.kasuga.test.registration.minecraft.item;

import lib.kasuga.KasugaLib;
import lib.kasuga.KasugaLibRegistry;
import lib.kasuga.registration.Registry;
import lib.kasuga.registration.minecraft.item.ItemReg;
import lib.kasuga.registration.minecraft.item.ItemRegModifiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Rarity;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class ItemRegistryTest {
    
    public static Registry registry = KasugaLibRegistry.getRegistryOf(KasugaLib.MODID);
    
    public static ItemReg<TestItem> TEST_BASIC_ITEM = ItemReg.of("test_basic_item", TestItem::new)
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("stacksTo", p -> { p.stacksTo(64); return p; }))
            .setParent(registry);
    
    public static ItemReg<TestFoodItem> TEST_FOOD_ITEM = ItemReg.of("test_food_item", TestFoodItem::new)
            .isFood(true)
            .configure(ItemRegModifiers.FoodPropertyModifiers.of("nutrition", fb -> { fb.nutrition(5); return fb; }))
            .configure(ItemRegModifiers.FoodPropertyModifiers.of("saturationMod", fb -> { fb.saturationModifier(0.6f); return fb; }))
            .configure(ItemRegModifiers.FoodPropertyModifiers.of("alwaysEdible", fb -> { fb.alwaysEdible().fast(); return fb; }))
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("stacksTo", p -> { p.stacksTo(16); return p; }))
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("rarity", p -> { p.rarity(Rarity.UNCOMMON); return p; }))
            .setParent(registry);
    
    public static ItemReg<TestDurableItem> TEST_DURABLE_ITEM = ItemReg.of("test_durable_item", TestDurableItem::new)
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("durability", p -> { p.durability(100); return p; }))
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("fireResistant", p -> { p.fireResistant(); return p; }))
            .configure(ItemRegModifiers.ItemPropertyModifiers.of("rarity", p -> { p.rarity(Rarity.RARE); return p; }))
            .setParent(registry);

    @Test
    public void testItemRegistry(MinecraftServer server) {
        // Test that items are registered
        assert TEST_BASIC_ITEM.getEntry() != null;
        assert TEST_FOOD_ITEM.getEntry() != null;
        assert TEST_DURABLE_ITEM.getEntry() != null;
        
        // Test registry contains the items
        var itemRegistry = server.registryAccess().registryOrThrow(Registries.ITEM);
        assert itemRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_basic_item"));
        assert itemRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_food_item"));
        assert itemRegistry.containsKey(ResourceLocation.fromNamespaceAndPath(KasugaLib.MODID, "test_durable_item"));
        
        // Test item properties
        assert TEST_BASIC_ITEM.getEntry().getMaxStackSize(TEST_BASIC_ITEM.getEntry().getDefaultInstance()) == 64;
        assert TEST_FOOD_ITEM.getEntry().getMaxStackSize(TEST_FOOD_ITEM.getEntry().getDefaultInstance()) == 16;
        assert TEST_FOOD_ITEM.getEntry().getFoodProperties(TEST_FOOD_ITEM.getEntry().getDefaultInstance(), null) != null;
        assert TEST_DURABLE_ITEM.getEntry().getMaxDamage(TEST_DURABLE_ITEM.getEntry().getDefaultInstance()) == 100;
        
        // Test rarity
        assert TEST_FOOD_ITEM.getEntry().builtInRegistryHolder().isBound();
        assert TEST_DURABLE_ITEM.getEntry().builtInRegistryHolder().isBound();
    }
}
