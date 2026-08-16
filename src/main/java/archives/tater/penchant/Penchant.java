package archives.tater.penchant;

import archives.tater.penchant.network.PenchantNetworking;
import archives.tater.penchant.registry.PenchantItems;
import archives.tater.penchant.registry.PenchantMenus;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Penchant.MOD_ID)
public class Penchant {
    public static final String MOD_ID = "penchant";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Penchant() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        PenchantItems.ITEMS.register(modBus);
        PenchantMenus.MENUS.register(modBus);
        modBus.addListener(PenchantItems::addCreative);
        PenchantNetworking.register();

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Penchant Forge 1.20.1 initialized");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
