package archives.tater.penchant;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

/**
 * Optional-mod probes. Enchiridion's category API on this Minecraft version is a different package
 * and is not compiled in; the flag still lets particle code skip Enchiridion's table when it is installed.
 */
public final class PenchantCompat {
    public static final boolean ENCHIRIDION_INSTALLED = isLoaded("enchiridion");
    public static final boolean MODEST_MAGIC_INSTALLED = isLoaded("modestmagic");

    /**
     * Mixin plugins run before {@link ModList} exists. The loader's mod list is the one available then.
     */
    public static boolean isLoaded(String modId) {
        var loading = FMLLoader.getLoadingModList();
        if (loading != null)
            return loading.getModFileById(modId) != null;
        var loaded = ModList.get();
        return loaded != null && loaded.isLoaded(modId);
    }

    private PenchantCompat() {}

    public static void init() {}
}
