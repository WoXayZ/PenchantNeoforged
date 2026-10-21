package archives.tater.penchant;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * Optional-mod probes. There is no Enchiridion release for 1.20.1.
 */
public final class PenchantCompat {
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
}
