package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.advancement.ExtractEnchantmentTrigger;
import archives.tater.penchant.advancement.OpenTableTrigger;

import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantAdvancements {

    public static final OpenTableTrigger OPEN_TABLE = new OpenTableTrigger();

    public static final ExtractEnchantmentTrigger EXTRACT_ENCHANTMENT = new ExtractEnchantmentTrigger();

    public static void register(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, helper -> {
            helper.register(Penchant.id("open_table"), OPEN_TABLE);
            helper.register(Penchant.id("extract_enchantment"), EXTRACT_ENCHANTMENT);
        });
    }

    public static void init() {

    }
}
