package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantMenus {
    public static final MenuType<PenchantmentMenu> PENCHANTMENT_MENU =
            new MenuType<>(PenchantmentMenu::new, FeatureFlags.VANILLA_SET);

    public static void register(RegisterEvent event) {
        event.register(Registries.MENU, helper ->
                helper.register(Penchant.id("penchantment"), PENCHANTMENT_MENU));
    }

    public static void init() {

    }
}
