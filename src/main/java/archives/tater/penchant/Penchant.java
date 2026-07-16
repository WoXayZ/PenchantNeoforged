package archives.tater.penchant;

import archives.tater.penchant.loot.LootModification;
import archives.tater.penchant.registry.PenchantAdvancements;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.registry.PenchantEnchantments;
import archives.tater.penchant.registry.PenchantFlag;
import archives.tater.penchant.registry.PenchantItems;
import archives.tater.penchant.registry.PenchantLootFunctions;
import archives.tater.penchant.registry.PenchantMenus;
import archives.tater.penchant.registry.PenchantModules;
import archives.tater.penchant.registry.PenchantRegistries;

import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
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

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }

    public Penchant(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::registerDatapackRegistries);
        modBus.addListener(this::registerNewRegistries);
        modBus.addListener(this::registerContents);
        modBus.addListener(this::addPackFinders);
        modBus.addListener(PenchantNetworking::register);
        modBus.addListener((GatherDataEvent.Client event) -> PenchantDataGenerator.gatherData(event));
        modBus.addListener((GatherDataEvent.Server event) -> PenchantDataGenerator.gatherData(event));

        NeoForge.EVENT_BUS.addListener(this::onServerStarted);

        LootModification.init();
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
