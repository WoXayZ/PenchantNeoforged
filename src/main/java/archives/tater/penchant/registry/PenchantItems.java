package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.item.TomeOfPenchantItem;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PenchantItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Penchant.MOD_ID);

    public static final RegistryObject<Item> TOME_OF_PENCHANT = ITEMS.register(
            "tome_of_penchant",
            () -> new TomeOfPenchantItem(new Item.Properties().stacksTo(1))
    );

    private PenchantItems() {}

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TOME_OF_PENCHANT.get());
        }
    }
}
