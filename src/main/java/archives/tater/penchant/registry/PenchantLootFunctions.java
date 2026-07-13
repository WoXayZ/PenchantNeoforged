package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.RandomEnchantment;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantLootFunctions {
    public static LootItemFunctionType<RandomEnchantment.LootFunction> RANDOM_ENCHANTMENT;

    private PenchantLootFunctions() {}

    public static void register(RegisterEvent event) {
        event.register(Registries.LOOT_FUNCTION_TYPE, helper -> {
            RANDOM_ENCHANTMENT = new LootItemFunctionType<>(RandomEnchantment.LootFunction.CODEC);
            helper.register(Penchant.id("random_enchantment"), RANDOM_ENCHANTMENT);
        });
    }
}
