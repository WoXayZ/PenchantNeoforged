package archives.tater.penchant;

import net.neoforged.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PenchantMixinPlugin implements IMixinConfigPlugin {
    private static final String PREFIX = "archives.tater.penchant.mixin";
    public static final String COMPAT_PREFIX = "compat";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var relativeName = mixinClassName.substring(PREFIX.length() + 1);
        if (!relativeName.startsWith(COMPAT_PREFIX)) return true;

        var compatName = relativeName.substring(COMPAT_PREFIX.length() + 1, relativeName.indexOf('.', COMPAT_PREFIX.length() + 1));
        return switch (compatName) {
            // PenchantCompat references Holder and Enchiridion. Loading it here, while mixins are still
            // being applied, makes KubeJS touch registry holders before bootstrap.
            case "modestmagic" -> modestMagicInstalled();
            default -> true;
        };
    }

    private static boolean modestMagicInstalled() {
        var loader = FMLLoader.getCurrentOrNull();
        var loading = loader == null ? null : loader.getLoadingModList();
        return loading != null && loading.getModFileById("modestmagic") != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
