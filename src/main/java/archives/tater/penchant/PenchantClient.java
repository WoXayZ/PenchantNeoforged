package archives.tater.penchant;

import archives.tater.penchant.client.gui.screen.PenchantmentScreen;
import archives.tater.penchant.client.PenchantKeys;
import archives.tater.penchant.registry.PenchantMenus;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Penchant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PenchantClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(PenchantMenus.PENCHANTMENT_MENU.get(), PenchantmentScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(PenchantKeys.SHOW_PROGRESS);
    }
}
