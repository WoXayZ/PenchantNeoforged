package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.enchantment.UnbreakableEffect;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

public class PenchantEnchantments {
    public static final DataComponentType<List<UnbreakableEffect>> UNBREAKABLE =
            DataComponentType.<List<UnbreakableEffect>>builder().persistent(UnbreakableEffect.CODEC.listOf()).build();

    public static void register(RegisterEvent event) {
        event.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, helper ->
                helper.register(Penchant.id("unbreakable"), UNBREAKABLE));
    }

    public static void init() {

    }
}
