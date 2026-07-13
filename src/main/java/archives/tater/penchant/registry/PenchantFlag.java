package archives.tater.penchant.registry;

import archives.tater.penchant.Penchant;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;

public class PenchantFlag {
    public static final ResourceKey<Registry<PenchantFlag>> REGISTRY_KEY = ResourceKey.createRegistryKey(Penchant.id("flag"));
    public static final Registry<PenchantFlag> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).sync(true).create();
    public static final TagKey<PenchantFlag> ENABLED = TagKey.create(REGISTRY_KEY, Penchant.id("enabled"));

    private static final List<PenchantFlag> TO_REGISTER = new ArrayList<>();

    private final ResourceLocation id;

    private PenchantFlag(ResourceLocation id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return REGISTRY.wrapAsHolder(this).is(ENABLED);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private static PenchantFlag create(ResourceLocation id) {
        var flag = new PenchantFlag(id);
        TO_REGISTER.add(flag);
        return flag;
    }

    private static PenchantFlag create(String path) {
        return create(Penchant.id(path));
    }

    public static final PenchantFlag REWORKED_TABLE_MENU = create("reworked_table_menu");
    public static final PenchantFlag LENIENT_BOOKSHELF_PLACEMENT = create("lenient_bookshelf_placement");
    public static final PenchantFlag NO_ANVIL_BOOKS = create("no_anvil_books");
    public static final PenchantFlag GUARANTEED_ENCHANTED_DROP = create("guaranteed_enchanted_drop");
    public static final PenchantFlag GUARANTEED_TRIDENT_DROP = create("guaranteed_trident_drop");
    public static final PenchantFlag ZOMBIE_SPAWN_PICKAXE = create("zombie_spawn_pickaxe");

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
