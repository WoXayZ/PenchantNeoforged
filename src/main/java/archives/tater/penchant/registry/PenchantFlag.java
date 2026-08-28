package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PenchantFlag {
    public static final ResourceKey<Registry<PenchantFlag>> REGISTRY_KEY = ResourceKey.createRegistryKey(Penchant.id("flag"));
    public static final Registry<PenchantFlag> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
    public static final TagKey<PenchantFlag> ENABLED = TagKey.create(REGISTRY_KEY, Penchant.id("enabled"));

    private static final List<PenchantFlag> TO_REGISTER = new ArrayList<>();

    private final Identifier id;
    private final String packPath;

    private PenchantFlag(Identifier id, String packPath) {
        this.id = id;
        this.packPath = packPath;
    }

    public boolean isEnabled() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            var repo = server.getPackRepository();
            if (isPackSelected(repo.getAvailableIds()) && !isPackSelected(repo.getSelectedIds())) {
                return false;
            }
        }
        return REGISTRY.wrapAsHolder(this).is(ENABLED);
    }

    private boolean isPackSelected(Collection<String> selected) {
        for (String id : selected) {
            if (id.equals(packPath) || id.equals("penchant:" + packPath)
                    || id.endsWith(":" + packPath) || id.endsWith("/" + packPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private static PenchantFlag create(Identifier id, String packPath) {
        var flag = new PenchantFlag(id, packPath);
        TO_REGISTER.add(flag);
        return flag;
    }

    private static PenchantFlag create(String path, String packPath) {
        return create(Penchant.id(path), packPath);
    }

    public static final PenchantFlag REWORKED_TABLE_MENU = create("reworked_table_menu", "table_rework");
    public static final PenchantFlag LENIENT_BOOKSHELF_PLACEMENT = create("lenient_bookshelf_placement", "bookshelf_placement");
    public static final PenchantFlag NO_ANVIL_BOOKS = create("no_anvil_books", "no_anvil_books");
    public static final PenchantFlag GUARANTEED_ENCHANTED_DROP = create("guaranteed_enchanted_drop", "guaranteed_drops");
    public static final PenchantFlag GUARANTEED_TRIDENT_DROP = create("guaranteed_trident_drop", "guaranteed_drops");
    public static final PenchantFlag ZOMBIE_SPAWN_PICKAXE = create("zombie_spawn_pickaxe", "loot_rework");
    public static final PenchantFlag REPLACE_BOOK_LOOT_TAG = create("replace_book_loot_tag", "loot_rework");

    public static void register(RegisterEvent event) {
        event.register(REGISTRY_KEY, helper -> {
            for (var flag : TO_REGISTER) {
                helper.register(flag.id, flag);
            }
        });
    }

    public static void init() {

    }
}
