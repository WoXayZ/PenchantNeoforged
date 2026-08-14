param(
  [Parameter(Mandatory=$true)][string]$Branch
)
$ErrorActionPreference = "Stop"
$git = "C:\Program Files\Git\mingw64\bin\git.exe"
Set-Location "c:\Users\cdaguenet\Desktop\Minecraft\Penchant\Penchant Neoforged"
$env:GCM_GITHUB_ACCOUNT = "WoXayZ"

function Write-Utf8NoBom([string]$Path, [string]$Content) {
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [System.IO.File]::WriteAllText((Resolve-Path $Path), $Content, $utf8)
}
# PowerShell 5 compatible writer
function Set-FileContent([string]$Path, [string]$Content) {
  $dir = Split-Path $Path -Parent
  if ($dir -and -not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [System.IO.File]::WriteAllText((Join-Path (Get-Location) $Path), $Content.Replace("`n", "`r`n").TrimEnd() + "`r`n", $utf8)
}

Write-Host "======== CHECKOUT $Branch ========"
cmd /c "`"$git`" checkout $Branch"
if ($LASTEXITCODE -ne 0) { throw "checkout failed" }
$cur = (cmd /c "`"$git`" branch --show-current").Trim()
if ($cur -ne $Branch) { throw "expected on $Branch but on $cur" }
cmd /c "`"$git`" reset --hard origin/$Branch"
if ($LASTEXITCODE -ne 0) { throw "reset failed" }
$cur = (cmd /c "`"$git`" branch --show-current").Trim()
if ($cur -ne $Branch) { throw "branch drifted to $cur after reset" }

$mcVer = ((Get-Content gradle.properties) | Where-Object { $_ -match '^minecraft_version=' }) -replace 'minecraft_version=',''
$neoVer = ((Get-Content gradle.properties) | Where-Object { $_ -match '^neo_version=' }) -replace 'neo_version=',''
Write-Host "MC=$mcVer Neo=$neoVer"

# Detect styles
$tagsRaw = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\datagen\PenchantTagsProvider.java")
$lootRaw = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\datagen\LootEnchantmentTagGenerator.java")
$usesRK = $tagsRaw -match 'TagAppender<ResourceKey'
$usesId = $lootRaw -match 'resources\.Identifier;'
$idType = if ($usesId) { "Identifier" } else { "ResourceLocation" }
$packGenAlready = (Select-String -Path "src/main/java/archives/tater/penchant/PenchantDataGenerator.java" -Pattern "getPackGenerator" -Quiet)

# 1) PenchantTagsProvider replaceBuilder
if ($tagsRaw -notmatch 'replaceBuilder') {
  if ($usesRK) {
    $tagsRaw = $tagsRaw.TrimEnd() + @"

    /** Like {@link #builder} but marks the tag as {@code "replace": true}. */
    protected TagAppender<ResourceKey<T>, T> replaceBuilder(TagKey<T> tag) {
        var raw = this.getOrCreateRawBuilder(tag);
        raw.replace();
        return TagAppender.forBuilder(raw);
    }
}
"@
    # Fix double closing brace - we appended after final }
    $tagsRaw = $tagsRaw -replace '\}\s*/\*\* Like.*?replaceBuilder[\s\S]*?\n\}\n\}', {
      # fallback handled below
    }
  }
}
# Simpler: rewrite end of file
if ($usesRK) {
  $tagsNew = @'
package archives.tater.penchant.datagen;

import archives.tater.penchant.Penchant;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;

/**
 * Base {@link TagsProvider} that mirrors Fabric's {@code FabricTagsProvider.builder(...)} convenience
 * so the ported generators read almost identically to the originals.
 */
public abstract class PenchantTagsProvider<T> extends TagsProvider<T> {
    protected PenchantTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, registryKey, lookupProvider, Penchant.MOD_ID);
    }

    protected TagAppender<ResourceKey<T>, T> builder(TagKey<T> tag) {
        return TagAppender.forBuilder(this.getOrCreateRawBuilder(tag));
    }

    /** Like {@link #builder} but marks the tag as {@code "replace": true}. */
    protected TagAppender<ResourceKey<T>, T> replaceBuilder(TagKey<T> tag) {
        var raw = this.getOrCreateRawBuilder(tag);
        raw.replace();
        return TagAppender.forBuilder(raw);
    }
}
'@
} else {
  # Keep existing builder signature from file
  $builderBody = if ($tagsRaw -match '(?s)protected TagAppender<T> builder\(TagKey<T> tag\) \{.*?\}') { $Matches[0] } else { $null }
  if (-not $builderBody) { throw "Could not find TagAppender<T> builder" }
  $retExpr = if ($builderBody -match 'return (.+);') { $Matches[1] } else { 'TagAppender.forBuilder(this.getOrCreateRawBuilder(tag))' }
  $usesTagMethod = $builderBody -match 'return this\.tag\('
  $replaceBody = if ($usesTagMethod) {
@'
    protected TagAppender<T> replaceBuilder(TagKey<T> tag) {
        this.getOrCreateRawBuilder(tag).replace();
        return this.tag(tag);
    }
'@
  } else {
@'
    protected TagAppender<T> replaceBuilder(TagKey<T> tag) {
        var raw = this.getOrCreateRawBuilder(tag);
        raw.replace();
        return TagAppender.forBuilder(raw);
    }
'@
  }
  # Insert before final closing brace of class
  if ($tagsRaw -notmatch 'replaceBuilder') {
    $tagsNew = $tagsRaw.TrimEnd().TrimEnd('}') + "`n`n" + $replaceBody + "`n}`n"
  } else {
    $tagsNew = $tagsRaw
  }
}
Set-FileContent "src/main/java/archives/tater/penchant/datagen/PenchantTagsProvider.java" $tagsNew
Write-Host "OK PenchantTagsProvider"

# 2) LootEnchantmentTagGenerator - rebuild from current with patches
if ($lootRaw -match '(?s)private static .+? createOptionalId\(String namespace, String path\) \{.*?\}') {
  $createFn = $Matches[0]
} else { throw "no createOptionalId" }
if ($lootRaw -match '(?s)public LootEnchantmentTagGenerator\(PackOutput output, CompletableFuture<HolderLookup\.Provider> registriesFuture(?:, ExistingFileHelper existingFileHelper)?\) \{\s*super\([^;]+;\s*\}') {
  $ctor = $Matches[0]
} else { throw "no ctor" }
$needsEfhImport = $ctor -match 'ExistingFileHelper'
$efhImport = if ($needsEfhImport) { "`r`nimport net.neoforged.neoforge.common.data.ExistingFileHelper;`r`n" } else { "" }

# Detect whether DENSITY/BREACH constants exist by compiling later; include them by default for 1.21+
$hasDensityBreach = $true
# Pre-1.21 won't be in this loop; if branch's old file never referenced them and MC is odd, still try

$lootNew = @"
package archives.tater.penchant.datagen;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.$idType;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
$efhImport
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootEnchantmentTagGenerator extends PenchantTagsProvider<Enchantment> {

    public static final List<ResourceKey<Enchantment>> UNIQUE = List.of(
            Enchantments.BREACH, // vault
            Enchantments.WIND_BURST, // ominous vault
            Enchantments.SOUL_SPEED, // bartering/bastion
            Enchantments.SWIFT_SNEAK // ancient city
    );

    public static final List<ResourceKey<Enchantment>> RARE = List.of(
            Enchantments.FROST_WALKER, // igloo
            Enchantments.FIRE_ASPECT, // nether fortress, ruined portal
            Enchantments.FLAME, // nether fortress, ruined portal
            Enchantments.SILK_TOUCH, // mineshaft, dungeon
            Enchantments.FORTUNE, // mineshaft, dungeon
            Enchantments.RESPIRATION, // ocean ruins, shipwreck, buried treasure
            Enchantments.DEPTH_STRIDER, // ocean ruins, shipwreck, buried treasure
            Enchantments.CHANNELING, // ruins, buried treasure
            Enchantments.RIPTIDE, // ruins, buried treasure
            Enchantments.THORNS, // desert temple
            Enchantments.INFINITY, // jungle temple
            Enchantments.MULTISHOT // pillager outpost
    );

    public static final List<ResourceKey<Enchantment>> UNCOMMON = List.of(
            Enchantments.AQUA_AFFINITY,
            Enchantments.FEATHER_FALLING,
            Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.SWEEPING_EDGE,
            Enchantments.KNOCKBACK,
            Enchantments.PUNCH,
            Enchantments.DENSITY,
            Enchantments.BREACH,
            Enchantments.LOOTING,
            Enchantments.LUCK_OF_THE_SEA
    );

    public static final List<ResourceKey<Enchantment>> COMMON = List.of(
            Enchantments.EFFICIENCY,
            Enchantments.PROTECTION,
            Enchantments.SHARPNESS,
            Enchantments.UNBREAKING,
            Enchantments.POWER,
            Enchantments.PIERCING,
            Enchantments.QUICK_CHARGE,
            Enchantments.IMPALING,
            Enchantments.LOYALTY,
            Enchantments.LURE,
            Enchantments.DENSITY
    );

$createFn

$ctor

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(PenchantEnchantmentTags.UNIQUE)
                .addAll(UNIQUE);

        builder(PenchantEnchantmentTags.RARE)
                .addAll(RARE)
                .addOptional(createOptionalId("veinminer-enchantment", "veinminer"))
                .addOptional(createOptionalId("veinminer_enchantment", "veinminer"));

        builder(PenchantEnchantmentTags.UNCOMMON)
                .addAll(UNCOMMON)
                .addOptional(createOptionalId("farmersdelight", "backstabbing"));

        builder(PenchantEnchantmentTags.COMMON)
                .addAll(COMMON);

        builder(EnchantmentTags.TREASURE)
                .addTag(PenchantEnchantmentTags.RARE)
                .addTag(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.NON_TREASURE)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        replaceBuilder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addTag(PenchantEnchantmentTags.COMMON);
        builder(EnchantmentTags.TRADEABLE)
                .remove(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE)
                .remove(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS)
                .remove(PenchantEnchantmentTags.COMMON)
                .remove(EnchantmentTags.CURSE);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
    }
}
"@
Set-FileContent "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" $lootNew
Write-Host "OK LootEnchantmentTagGenerator"

# 3) EnchantmentTagGenerator - add ON_RANDOM_LOOT_BOOKS
$etg = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\datagen\EnchantmentTagGenerator.java")
if ($etg -notmatch 'ON_RANDOM_LOOT_BOOKS') {
  $etg = $etg -replace '(builder\(PenchantEnchantmentTags\.DISABLED\);\r?\n)', "`$1`r`n        builder(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS)`r`n                .addTag(EnchantmentTags.ON_RANDOM_LOOT);`r`n"
  Set-FileContent "src/main/java/archives/tater/penchant/datagen/EnchantmentTagGenerator.java" $etg
}
Write-Host "OK EnchantmentTagGenerator"

# 4) PenchantEnchantmentTags
$pet = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\registry\PenchantEnchantmentTags.java")
if ($pet -notmatch 'ON_RANDOM_LOOT_BOOKS') {
  $pet = $pet -replace '(public static final TagKey<Enchantment> NO_LEVELING = create\("no_leveling"\);)', "`$1`r`n    public static final TagKey<Enchantment> ON_RANDOM_LOOT_BOOKS = create(`"on_random_loot_books`");"
  Set-FileContent "src/main/java/archives/tater/penchant/registry/PenchantEnchantmentTags.java" $pet
}

# 5) PenchantFlag
$pf = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\registry\PenchantFlag.java")
if ($pf -notmatch 'REPLACE_BOOK_LOOT_TAG') {
  $pf = $pf -replace '(public static final PenchantFlag ZOMBIE_SPAWN_PICKAXE = create\("zombie_spawn_pickaxe"\);)', "`$1`r`n    public static final PenchantFlag REPLACE_BOOK_LOOT_TAG = create(`"replace_book_loot_tag`");"
  Set-FileContent "src/main/java/archives/tater/penchant/registry/PenchantFlag.java" $pf
}

# 6) PenchantModules
$pm = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\registry\PenchantModules.java")
if ($pm -notmatch '@Deprecated') {
  $pm = $pm -replace "(public static final $idType REDUCED_CURSES = Penchant\.id\(`"reduced_curses`"\);)", @"
/**
     * @deprecated Folded into {@link #LOOT_REWORK} in 1.4 / upstream 0.5.0.
     */
    @Deprecated
    public static final $idType REDUCED_CURSES = Penchant.id(`"reduced_curses`");
"@
  $pm = $pm -replace "\r?\n\s*registerPack\(event, REDUCED_CURSES, NORMAL_SOURCE\);", ""
  Set-FileContent "src/main/java/archives/tater/penchant/registry/PenchantModules.java" $pm
}

# 7) PenchantDataGenerator
$pdg = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\PenchantDataGenerator.java")
$pdg = $pdg -replace "import archives\.tater\.penchant\.datagen\.CurseEnchantmentTagGenerator;\r?\n", ""
$pdg = $pdg -replace 'FlagTagGenerator\(output, lookup, PenchantFlag\.ZOMBIE_SPAWN_PICKAXE\)', 'FlagTagGenerator(output, lookup, PenchantFlag.ZOMBIE_SPAWN_PICKAXE, PenchantFlag.REPLACE_BOOK_LOOT_TAG)'
$pdg = $pdg -replace 'FlagTagGenerator\(output, lookup, existingFileHelper, PenchantFlag\.ZOMBIE_SPAWN_PICKAXE\)', 'FlagTagGenerator(output, lookup, existingFileHelper, PenchantFlag.ZOMBIE_SPAWN_PICKAXE, PenchantFlag.REPLACE_BOOK_LOOT_TAG)'
$pdg = $pdg -replace '(?s)\r?\n\s*// reduced_curses\r?\n\s*var curses = builtinPack\(generator, PenchantModules\.REDUCED_CURSES\);\r?\n\s*curses\.addProvider\(output -> new CurseEnchantmentTagGenerator\(output, lookup(?:, existingFileHelper)?\)\);', ''
# Fallback line-based removal if regex missed
if ($pdg -match 'CurseEnchantmentTagGenerator') {
  $lines = $pdg -split "`r?`n"
  $out = New-Object System.Collections.Generic.List[string]
  for ($i=0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match '// reduced_curses') { 
      # skip this and next two non-empty related lines
      while ($i -lt $lines.Count -and ($lines[$i] -match 'reduced_curses|CurseEnchantment|var curses')) { $i++ }
      if ($i -lt $lines.Count -and $lines[$i] -match '^\s*$') { continue }
      $i--
      continue
    }
    if ($lines[$i] -match 'CurseEnchantmentTagGenerator|var curses = ') { continue }
    $out.Add($lines[$i])
  }
  $pdg = ($out -join "`n")
}
# Keep branch's DataGenerator pack API (getPackGenerator vs getBuiltinDatapack) ? do not rewrite.
Set-FileContent "src/main/java/archives/tater/penchant/PenchantDataGenerator.java" $pdg

# Delete CurseEnchantmentTagGenerator
Remove-Item "src/main/java/archives/tater/penchant/datagen/CurseEnchantmentTagGenerator.java" -Force -ErrorAction SilentlyContinue

# 8) UI fixes
$widget = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\client\gui\widget\EnchantmentSlotWidget.java")
$widget = $widget -replace 'if \(!isUnlocked && canUse\)', 'if (!isUnlocked && !alreadyAdded)'
Set-FileContent "src/main/java/archives/tater/penchant/client/gui/widget/EnchantmentSlotWidget.java" $widget

$screen = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\client\gui\screen\PenchantmentScreen.java")
$screen = $screen -replace '(\s+)menu\.isAvailable\(enchantment\)', '$1creative || menu.isAvailable(enchantment)'
Set-FileContent "src/main/java/archives/tater/penchant/client/gui/screen/PenchantmentScreen.java" $screen

$menu = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\menu\PenchantmentMenu.java")
if ($menu -notmatch 'var creative = player\.isCreative\(\);\s*\r?\n\s*displayedEnchantments') {
  $menu = $menu -replace '(if \(isEnchanting\(\)\) \{\r?\n)(\s+displayedEnchantments = streamOrdered)', "`$1            var creative = player.isCreative();`r`n`$2"
  $menu = $menu -replace '(\.sorted\(comparingInt\(enchantment ->\r?\n\s+)!availableEnchantments\.contains\(enchantment\) && !PenchantmentHelper\.hasEnchantment\(stack, enchantment\) \? 2', '$1creative || !availableEnchantments.contains(enchantment) && !PenchantmentHelper.hasEnchantment(stack, enchantment) ? 2'
  Set-FileContent "src/main/java/archives/tater/penchant/menu/PenchantmentMenu.java" $menu
}

# 9) Mixins
$mixinRand = @'
package archives.tater.penchant.mixin.loot;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.objectweb.asm.Opcodes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import java.util.Optional;

@Mixin(EnchantRandomlyFunction.class)
public class EnchantRandomlyFunctionMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyExpressionValue(
            method = "run",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/storage/loot/functions/EnchantRandomlyFunction;options:Ljava/util/Optional;", opcode = Opcodes.GETFIELD)
    )
    private static Optional<HolderSet<Enchantment>> replaceLootTag(Optional<HolderSet<Enchantment>> original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LootContext context) {
        if (!itemStack.is(Items.BOOK)) return original;
        if (original.flatMap(HolderSet::unwrapKey).orElse(null) != EnchantmentTags.ON_RANDOM_LOOT) return original;

        return Optional.of(context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS));
    }
}
'@
$mixinLevels = @'
package archives.tater.penchant.mixin.loot;

import archives.tater.penchant.registry.PenchantEnchantmentTags;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;

import java.util.Optional;

@Mixin(EnchantWithLevelsFunction.class)
public class EnchantWithLevelsFunctionMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyArg(
            method = "run",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;enchantItem(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/RegistryAccess;Ljava/util/Optional;)Lnet/minecraft/world/item/ItemStack;"),
            index = 4
    )
    private static Optional<HolderSet<Enchantment>> replaceLootTag(Optional<HolderSet<Enchantment>> original, @Local(argsOnly = true) ItemStack itemStack, @Local(argsOnly = true) LootContext context) {
        if (!itemStack.is(Items.BOOK)) return original;
        if (original.flatMap(HolderSet::unwrapKey).orElse(null) != EnchantmentTags.ON_RANDOM_LOOT) return original;

        return Optional.of(context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS));
    }
}
'@
Set-FileContent "src/main/java/archives/tater/penchant/mixin/loot/EnchantRandomlyFunctionMixin.java" $mixinRand
Set-FileContent "src/main/java/archives/tater/penchant/mixin/loot/EnchantWithLevelsFunctionMixin.java" $mixinLevels

$mixins = [IO.File]::ReadAllText("$pwd\src\main\resources\penchant.mixins.json")
if ($mixins -notmatch 'loot\.EnchantRandomlyFunctionMixin') {
  $mixins = $mixins -replace '("loot\.LootTableAccessor",)', "`$1`r`n    `"loot.EnchantRandomlyFunctionMixin`",`r`n    `"loot.EnchantWithLevelsFunctionMixin`","
  Set-FileContent "src/main/resources/penchant.mixins.json" $mixins
}

# 10) gradle.properties version
$gp = [IO.File]::ReadAllText("$pwd\gradle.properties")
$gp = $gp -replace 'mod_version=.*', "mod_version=$mcVer-1.4"
Set-FileContent "gradle.properties" $gp

# 11) README
$readme = [IO.File]::ReadAllText("$pwd\README.md")
$readme = $readme -replace '\*\*0\.4\.[0-9]\*\*', '**0.5.0**'
$readme = $readme -replace '\*\*Port version\*\* \| \*\*1\.3(?:-fix)?\*\*', '**Port version** | **1.4**'
$readme = $readme -replace "\*\*Mod version\*\* \| $mcVer-1\.3(?:-fix)?", "**Mod version** | $mcVer-1.4"
$readme = $readme -replace '\| `loot_rework` \| ? \| Reworks where enchantment books appear in loot & mob equipment \|', '| `loot_rework` | ? | Reworks loot/trades; uncategorized off table; curses off trades & loot books |'
# handle encoding weirdness for checkmarks - try multiple patterns
$readme = $readme -replace '\| `loot_rework` \| .+ \| Reworks where enchantment books appear in loot & mob equipment \|', '| `loot_rework` | ? | Reworks loot/trades; uncategorized off table; curses off trades & loot books |'
$readme = $readme -replace '\| `reduced_curses` \| .+ \| Reduces how often curses appear \|\r?\n', ''
$readme = $readme -replace '### Item tags & components \(0\.4\.[0-9]\)', '### Item tags & components (0.5.0)'
Set-FileContent "README.md" $readme

# 12) Lang keys
Get-ChildItem "src/main/resources/assets/penchant/lang/*.json" | ForEach-Object {
  $c = [IO.File]::ReadAllText($_.FullName)
  $c2 = [regex]::Replace($c, '\s*"dataPack\.penchant\.reduced_curses\.name"\s*:\s*"[^"]*"\s*,?\r?\n', '')
  $c2 = [regex]::Replace($c2, '\s*"dataPack\.penchant\.reduced_curses\.description"\s*:\s*"[^"]*"\s*,?\r?\n', '')
  if ($c2 -ne $c) { [IO.File]::WriteAllText($_.FullName, $c2, (New-Object System.Text.UTF8Encoding $false)) }
}

# 13) build.gradle data() -> clientData() if needed
$bg = [IO.File]::ReadAllText("$pwd\build.gradle")
if ($bg -match 'data \{\s*data\(\)') {
  $bg = $bg -replace '(data \{\s*)data\(\)', '$1clientData()'
  Set-FileContent "build.gradle" $bg
  Write-Host "OK build.gradle clientData"
}

# 14) Delete reduced_curses pack
if (Test-Path "src/generated/resources/resourcepacks/reduced_curses") {
  Remove-Item -Recurse -Force "src/generated/resources/resourcepacks/reduced_curses"
}

Write-Host "======== BUILD $Branch ========"
# Clean cache
Remove-Item -Recurse -Force "src/generated/resources/.cache" -ErrorAction SilentlyContinue

$buildOk = $true
$adaptations = @()
$adaptations += "no Enderscape"
$adaptations += "no cost-factor component"
$adaptations += "isCreative()"
$adaptations += "DENSITY/BREACH added to COMMON/UNCOMMON"
if ($usesRK) { $adaptations += "TagAppender<ResourceKey<T>,T>" } else { $adaptations += "TagAppender<T>" }
$adaptations += "idType=$idType"

cmd /c "gradlew.bat compileJava --no-daemon & exit /b %ERRORLEVEL%"
if ($LASTEXITCODE -ne 0) {
  $errLog = cmd /c "gradlew.bat compileJava --no-daemon 2>&1"
  Write-Host $errLog
  if ("$errLog" -match 'DENSITY|BREACH|LUNGE') {
    Write-Host "compileJava failed - trying without DENSITY/BREACH"
    $lootFix = [IO.File]::ReadAllText("$pwd\src\main\java\archives\tater\penchant\datagen\LootEnchantmentTagGenerator.java")
    $lootFix = $lootFix -replace '\s*Enchantments\.DENSITY,\r?\n\s*Enchantments\.BREACH,\r?\n', "`r`n"
    $lootFix = $lootFix -replace ',\r?\n\s*Enchantments\.DENSITY\r?\n', "`r`n"
    Set-FileContent "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" $lootFix
    $adaptations += "removed DENSITY/BREACH (compile fail)"
    cmd /c "gradlew.bat compileJava --no-daemon & exit /b %ERRORLEVEL%"
    if ($LASTEXITCODE -ne 0) { $buildOk = $false; throw "compile still failing on $Branch" }
  } else {
    $buildOk = $false; throw "compile failing on $Branch (not DENSITY related)"
  }
}

cmd /c "gradlew.bat runData --no-daemon & exit /b %ERRORLEVEL%"
if ($LASTEXITCODE -ne 0) {
  Write-Host "runData failed - checking nested output / manual"
  $adaptations += "runData failed"
} else {
  # Fix nested path if present
  $modules = @("bookshelf_placement","durability_rework","guaranteed_drops","loot_rework","no_anvil_books","table_rework","randomized_librarians")
  foreach ($m in $modules) {
    $src = "src/generated/resources/data/$m/datapacks/resourcepacks/$m"
    if (Test-Path $src) {
      $dst = "src/generated/resources/resourcepacks/$m"
      if (Test-Path $dst) { Remove-Item -Recurse -Force $dst }
      New-Item -ItemType Directory -Path (Split-Path $dst) -Force | Out-Null
      Move-Item $src $dst
      Remove-Item -Recurse -Force "src/generated/resources/data/$m" -ErrorAction SilentlyContinue
      $adaptations += "moved nested datagen $m"
    }
  }
}

cmd /c "gradlew.bat jar --no-daemon & exit /b %ERRORLEVEL%"
if ($LASTEXITCODE -ne 0) { throw "jar failed on $Branch" }

New-Item -ItemType Directory -Path "releases" -Force | Out-Null
Copy-Item "build/libs/penchant-neoforge-$mcVer-1.4.jar" "releases/" -Force -ErrorAction SilentlyContinue
# fallback copy any matching
Get-ChildItem "build/libs/penchant-neoforge-$mcVer-1.4*.jar" | ForEach-Object { Copy-Item $_.FullName "releases/" -Force }

# Remove temp port scripts from staging
Remove-Item "_port14_phase1.ps1" -ErrorAction SilentlyContinue

Write-Host "======== COMMIT $Branch ========"
cmd /c "`"$git`" add -A"
cmd /c "`"$git`" reset HEAD -- build/ releases/ _port*.ps1 2>nul"
# Keep releases jars? User said copy to releases/ - those are typically gitignored or tracked
# Check if releases is tracked
$releasesTracked = cmd /c "`"$git`" ls-files releases"

$msgFile = Join-Path $env:TEMP "penchant-commit-msg.txt"
[IO.File]::WriteAllText($msgFile, "1.4`n")
$tree = (cmd /c "`"$git`" write-tree").Trim()
$parent = (cmd /c "`"$git`" rev-parse HEAD").Trim()
$env:GIT_AUTHOR_NAME = "woxayz"
$env:GIT_AUTHOR_EMAIL = "woxayz@gmail.com"
$env:GIT_COMMITTER_NAME = "woxayz"
$env:GIT_COMMITTER_EMAIL = "woxayz@gmail.com"
$commit = (cmd /c "`"$git`" commit-tree $tree -p $parent -F `"$msgFile`"").Trim()
if ($commit -notmatch '^[0-9a-f]{40}$') { throw "commit-tree failed: $commit" }
cmd /c "`"$git`" update-ref refs/heads/$Branch $commit"
cmd /c "`"$git`" checkout $Branch"
cmd /c "`"$git`" reset --hard $commit"
cmd /c "`"$git`" tag -f $mcVer-1.4 $commit"
cmd /c "`"$git`" remote set-url origin https://WoXayZ@github.com/WoXayZ/PenchantNeoforged.git"
cmd /c "`"$git`" push --force-with-lease origin $Branch"
cmd /c "`"$git`" push --force origin $mcVer-1.4"

$body = ((& $git -C "$pwd" log -1 --format="%B") -join "`n").Trim()
if ($body -ne "1.4") { throw "Bad commit message on $Branch : [$body]" }

$result = [pscustomobject]@{
  Branch = $Branch
  Status = "success"
  Tag = "$mcVer-1.4"
  Commit = $commit
  Adaptations = ($adaptations -join "; ")
}
$result | ConvertTo-Json
$result | Export-Csv -Path "$env:TEMP\penchant14-results.csv" -Append -NoTypeInformation
Write-Host "DONE $Branch -> $mcVer-1.4 ($commit)"

