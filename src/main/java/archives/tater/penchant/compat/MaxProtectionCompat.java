package archives.tater.penchant.compat;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;

import java.util.Locale;
import java.util.Set;

/**
 * Max Protection (and similarly named mods) lets Protection / Fire / Blast / Projectile
 * coexist on one armor piece. Datapack exclusive-set edits are already honored by vanilla
 * compatibility; this covers Java mods that only patch the anvil.
 */
public final class MaxProtectionCompat {
    private static final Set<Identifier> STACKABLE = Set.of(
            Identifier.withDefaultNamespace("protection"),
            Identifier.withDefaultNamespace("fire_protection"),
            Identifier.withDefaultNamespace("blast_protection"),
            Identifier.withDefaultNamespace("projectile_protection")
    );

    private static Boolean installed;

    private MaxProtectionCompat() {}

    public static boolean isInstalled() {
        if (installed == null) installed = detect();
        return installed;
    }

    public static boolean isStackableProtection(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey()
                .map(key -> STACKABLE.contains(key.identifier()))
                .orElse(false);
    }

    public static boolean allowsTogether(Holder<Enchantment> first, Holder<Enchantment> second) {
        if (!isInstalled() || first.equals(second)) return false;
        return isStackableProtection(first) && isStackableProtection(second);
    }

    private static boolean detect() {
        return ModList.get().getMods().stream().anyMatch(mod -> {
            String id = mod.getModId().replace("-", "").replace("_", "");
            if (id.equals("maxprotection") || id.equals("maximumprotection")) return true;
            String name = mod.getDisplayName().toLowerCase(Locale.ROOT).trim();
            if (name.equals("max protection") || name.equals("maximum protection")) return true;
            return (name.startsWith("max protection") || name.startsWith("maximum protection"))
                    && !name.contains("iii") && !name.contains(" 3");
        });
    }
}
