package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.menu.PenchantmentMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PenchantMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Penchant.MOD_ID);

    public static final RegistryObject<MenuType<PenchantmentMenu>> PENCHANTMENT_MENU =
            MENUS.register("penchantment", () -> IForgeMenuType.create(PenchantmentMenu::fromNetwork));

    private PenchantMenus() {}
}
