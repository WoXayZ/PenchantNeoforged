import archives.tater.penchant.PenchantCompat;

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
            case "modestmagic" -> PenchantCompat.MODEST_MAGIC_INSTALLED;
            default -> true;
        };
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
