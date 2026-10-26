package archives.tater.penchant;

import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.network.PenchantNetworking;
import archives.tater.penchant.registry.PenchantEntityTags;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.registry.PenchantItems;
import archives.tater.penchant.registry.PenchantMenus;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Penchant.MOD_ID)
public class Penchant {
    public static final String MOD_ID = "penchant";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Penchant() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PenchantServerConfig.SPEC, PenchantServerConfig.FILE_NAME);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PenchantGameplayConfig.SPEC, PenchantGameplayConfig.FILE_NAME);

        PenchantItems.ITEMS.register(modBus);
        PenchantMenus.MENUS.register(modBus);
        modBus.addListener(PenchantItems::addCreative);
        PenchantNetworking.register();

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Penchant Forge 1.20.1 initialized");
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!(entity instanceof Enemy) && !entity.getType().builtInRegistryHolder().is(PenchantEntityTags.KILL_LEVELS_UP_EQUIPMENT)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) return;

        for (var slot : EquipmentSlot.values()) {
            var stack = killer.getItemBySlot(slot);
            if (!stack.is(PenchantItemTags.LEVEL_UP_BY_KILLING) || killer.getEquipmentSlotForItem(stack) != slot) continue;
            EnchantmentProgress.addToProgress(stack, 1, killer);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        PenchantCommands.register(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
