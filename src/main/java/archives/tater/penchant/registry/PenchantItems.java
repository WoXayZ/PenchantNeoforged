package archives.tater.penchant.registry;

// PATCHOULI: Re-enable when a compatible Patchouli release exists for this Minecraft version.
// import archives.tater.penchant.Penchant;
// import archives.tater.penchant.item.TomeOfPenchantItem;

import net.minecraft.core.registries.Registries;
// import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.RegisterEvent;

public class PenchantItems {
    // PATCHOULI: public static Item TOME_OF_PENCHANT;

    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            // PATCHOULI: Tome of Penchant guidebook — requires Patchouli.
            // var id = Penchant.id("tome_of_penchant");
            // var item = new TomeOfPenchantItem(new Item.Properties()
            //         .setId(ResourceKey.create(Registries.ITEM, id))
            //         .stacksTo(1));
            // helper.register(id, item);
            // TOME_OF_PENCHANT = item;
        });
    }
}
