package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Built-in datapack modules. Packs are enabled by default on new worlds but are
 * not required, so they can be turned off in the datapack screen / {@code /datapack}.
 */
@Mod.EventBusSubscriber(modid = Penchant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PenchantModules {
    /** Builtin label, but {@code shouldAddAutomatically} so new worlds get the modules. */
    private static final PackSource MODULE = PackSource.create(
            name -> Component.translatable("pack.nameAndSource", name, Component.translatable("pack.source.builtin"))
                    .withStyle(ChatFormatting.GRAY),
            true
    );
    public static final ResourceLocation DURABILITY_REWORK = Penchant.id("durability_rework");
    public static final ResourceLocation BOOKSHELF_PLACEMENT = Penchant.id("bookshelf_placement");
    public static final ResourceLocation TABLE_REWORK = Penchant.id("table_rework");
    public static final ResourceLocation NO_ANVIL_BOOKS = Penchant.id("no_anvil_books");
    public static final ResourceLocation LOOT_REWORK = Penchant.id("loot_rework");
    public static final ResourceLocation GUARANTEED_DROPS = Penchant.id("guaranteed_drops");

    private PenchantModules() {}

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        register(event, TABLE_REWORK, true);
        register(event, BOOKSHELF_PLACEMENT, true);
        register(event, NO_ANVIL_BOOKS, true);
        register(event, DURABILITY_REWORK, true);
        register(event, LOOT_REWORK, true);
        register(event, GUARANTEED_DROPS, true);
    }

    private static void register(AddPackFindersEvent event, ResourceLocation id, boolean enabledByDefault) {
        Path root;
        try {
            root = ModList.get().getModFileById(Penchant.MOD_ID).getFile().findResource("resourcepacks/" + id.getPath());
            if (root == null || !Files.exists(root)) return;
        } catch (Exception e) {
            return;
        }

        PackSource source = enabledByDefault ? MODULE : PackSource.create(name -> name, false);
        event.addRepositorySource((Consumer<Pack> consumer) -> {
            try {
                Pack.ResourcesSupplier resources = (packId) -> new PathPackResources(packId, true, root);
                Pack pack = Pack.readMetaAndCreate(
                        id.toString(),
                        Component.translatable("dataPack." + id.getNamespace() + "." + id.getPath() + ".name"),
                        false,
                        resources,
                        PackType.SERVER_DATA,
                        Pack.Position.TOP,
                        source
                );
                if (pack != null) consumer.accept(pack);
            } catch (Exception ignored) {
                // Pack optional until resources are filled in.
            }
        });
    }
}
