package archives.tater.penchant;

import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.registry.PenchantAdvancements;
import archives.tater.penchant.registry.PenchantEntityTags;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantEnchantments;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.registry.PenchantItemTags;
import archives.tater.penchant.registry.PenchantItems;
import archives.tater.penchant.registry.PenchantLootFunctions;
import archives.tater.penchant.registry.PenchantMenus;
import archives.tater.penchant.registry.PenchantModules;
import archives.tater.penchant.registry.PenchantRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Penchant.MOD_ID)
public class Penchant {
    public static final String MOD_ID = "penchant";

    // This logger is used to write text to the console and the log file.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation id(String path) {
        return id(MOD_ID, path);
    }

    public Penchant(IEventBus modBus, ModContainer container) {
        PenchantCompat.init();

        container.registerConfig(ModConfig.Type.STARTUP, PenchantServerConfig.SPEC, PenchantServerConfig.FILE_NAME);
        container.registerConfig(ModConfig.Type.SERVER, PenchantGameplayConfig.SPEC, PenchantGameplayConfig.FILE_NAME);

        modBus.addListener(this::registerDatapackRegistries);
        modBus.addListener(this::registerNewRegistries);
        modBus.addListener(this::registerContents);
        modBus.addListener(this::addPackFinders);
        modBus.addListener(PenchantNetworking::register);
        modBus.addListener(PenchantDataGenerator::gatherData);

        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(PenchantCommands::register);

        LootModification.init();
    }

    private void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        if (!(entity instanceof Enemy) && !entity.getType().builtInRegistryHolder().is(PenchantEntityTags.KILL_LEVELS_UP_EQUIPMENT)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) return;

        for (var slot : EquipmentSlot.values()) {
            var stack = killer.getItemBySlot(slot);
            if (!stack.is(PenchantItemTags.LEVEL_UP_BY_KILLING) || killer.getEquipmentSlotForItem(stack) != slot) continue;
            EnchantmentProgress.addToProgress(stack, 1, killer);
        }
    }

    private void registerNewRegistries(NewRegistryEvent event) {
        event.register(PenchantFlag.REGISTRY);
    }

    private void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(PenchantRegistries.PENCHANTMENT_DEFINITION, PenchantmentDefinition.CODEC, PenchantmentDefinition.CODEC);
        event.dataPackRegistry(LootModification.KEY, LootModification.CODEC);
    }

    private void registerContents(RegisterEvent event) {
        PenchantFlag.register(event);
        PenchantComponents.register(event);
        PenchantEnchantments.register(event);
        PenchantMenus.register(event);
        PenchantItems.register(event);
        PenchantAdvancements.register(event);
        PenchantLootFunctions.register(event);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        PenchantModules.addPackFinders(event);
    }

    private void onServerStarted(ServerStartedEvent event) {
        PenchantmentDefinition.buildCache(event.getServer().registryAccess());
    }
}
