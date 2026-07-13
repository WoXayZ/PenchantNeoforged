package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.component.RandomEnchantment;

import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantLootFunctions {
    private PenchantLootFunctions() {}

    public static void register(RegisterEvent event) {
        event.register(Registries.LOOT_FUNCTION_TYPE, helper ->
                helper.register(Penchant.id("random_enchantment"), RandomEnchantment.LootFunction.CODEC));
    }
}
