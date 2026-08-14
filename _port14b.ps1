param([Parameter(Mandatory=$true)][string]$Branch)
$ErrorActionPreference = "Stop"
Set-Location "c:\Users\cdaguenet\Desktop\Minecraft\Penchant\Penchant Neoforged"
$git = "C:\Program Files\Git\mingw64\bin\git.exe"
$env:GCM_GITHUB_ACCOUNT = "WoXayZ"

function Set-File([string]$Rel, [string]$Content) {
  $full = Join-Path (Get-Location) $Rel
  $dir = Split-Path $full -Parent
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
  $utf8 = New-Object System.Text.UTF8Encoding $false
  [IO.File]::WriteAllText($full, ($Content -replace "`r?`n","`n").TrimEnd() + "`n", $utf8)
}
function Edit-File([string]$Rel, [scriptblock]$Fn) {
  $full = Join-Path (Get-Location) $Rel
  $c = [IO.File]::ReadAllText($full)
  $n = & $Fn $c
  Set-File $Rel $n
}

Write-Host "=== HARD RESET $Branch ==="
& $git checkout -f $Branch
& $git reset --hard "origin/$Branch"
& $git clean -fd -e _port14.ps1 -e _port14b.ps1 -e releases

$mcVer = ((Get-Content gradle.properties) | Where-Object { $_ -match '^minecraft_version=' }) -replace 'minecraft_version=',''
Write-Host "MC=$mcVer"

$hasEFH = Select-String -Path "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" -Pattern "ExistingFileHelper" -Quiet
$usesRK = Select-String -Path "src/main/java/archives/tater/penchant/datagen/PenchantTagsProvider.java" -Pattern "TagAppender<ResourceKey" -Quiet
$usesId = Select-String -Path "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" -Pattern "resources\.Identifier;" -Quiet
$idType = if ($usesId) { "Identifier" } else { "ResourceLocation" }
Write-Host "EFH=$hasEFH RK=$usesRK id=$idType"

# --- PenchantTagsProvider: preserve ctor, add replaceBuilder ---
Edit-File "src/main/java/archives/tater/penchant/datagen/PenchantTagsProvider.java" {
  param($c)
  if ($c -match 'replaceBuilder') { return $c }
  if ($usesRK) {
    return $c.TrimEnd().TrimEnd('}') + @"

    /** Like {@link #builder} but marks the tag as {@code "replace": true}. */
    protected TagAppender<ResourceKey<T>, T> replaceBuilder(TagKey<T> tag) {
        var raw = this.getOrCreateRawBuilder(tag);
        raw.replace();
        return TagAppender.forBuilder(raw);
    }
}
"@
  } else {
    return $c.TrimEnd().TrimEnd('}') + @"

    /** Like {@link #builder} but marks the tag as {@code "replace": true}. */
    protected TagAppender<T> replaceBuilder(TagKey<T> tag) {
        this.getOrCreateRawBuilder(tag).replace();
        return this.tag(tag);
    }
}
"@
  }
}

# --- LootEnchantmentTagGenerator: surgical edits ---
Edit-File "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" {
  param($c)
  # Add DENSITY/BREACH to UNCOMMON after PUNCH
  if ($c -notmatch 'Enchantments\.DENSITY') {
    $c = $c -replace '(Enchantments\.PUNCH,\r?\n)(\s+Enchantments\.LOOTING,)', "`$1            Enchantments.DENSITY,`n            Enchantments.BREACH,`n`$2"
    $c = $c -replace '(Enchantments\.LURE)\s*\n(\s*\);)', "`$1,`n            Enchantments.DENSITY`n`$2"
  }
  # Replace table/loot tag block
  $old = @'
        builder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .remove(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.TRADEABLE)
                .remove(PenchantEnchantmentTags.COMMON)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .remove(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_RANDOM_LOOT)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
        builder(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .addTag(PenchantEnchantmentTags.UNCOMMON)
                .addTag(PenchantEnchantmentTags.RARE)
                .remove(PenchantEnchantmentTags.UNIQUE);
'@
  $new = @'
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
'@
  $cn = $c -replace "`r`n","`n"
  $oldn = ($old -replace "`r`n","`n").Trim()
  $newn = ($new -replace "`r`n","`n").Trim()
  if ($cn.Contains($oldn)) { $cn = $cn.Replace($oldn, $newn) } else { throw "loot addTags pattern not found" }
  return $cn
}

# --- EnchantmentTagGenerator ---
Edit-File "src/main/java/archives/tater/penchant/datagen/EnchantmentTagGenerator.java" {
  param($c)
  if ($c -match 'ON_RANDOM_LOOT_BOOKS') { return $c }
  return $c -replace '(builder\(PenchantEnchantmentTags\.DISABLED\);\r?\n)', "`$1`n        builder(PenchantEnchantmentTags.ON_RANDOM_LOOT_BOOKS)`n                .addTag(EnchantmentTags.ON_RANDOM_LOOT);`n"
}

# --- Tags / Flags / Modules ---
Edit-File "src/main/java/archives/tater/penchant/registry/PenchantEnchantmentTags.java" {
  param($c)
  if ($c -match 'ON_RANDOM_LOOT_BOOKS') { return $c }
  return $c -replace '(NO_LEVELING = create\("no_leveling"\);)', "`$1`n    public static final TagKey<Enchantment> ON_RANDOM_LOOT_BOOKS = create(`"on_random_loot_books`");"
}
Edit-File "src/main/java/archives/tater/penchant/registry/PenchantFlag.java" {
  param($c)
  if ($c -match 'REPLACE_BOOK_LOOT_TAG') { return $c }
  return $c -replace '(ZOMBIE_SPAWN_PICKAXE = create\("zombie_spawn_pickaxe"\);)', "`$1`n    public static final PenchantFlag REPLACE_BOOK_LOOT_TAG = create(`"replace_book_loot_tag`");"
}
Edit-File "src/main/java/archives/tater/penchant/registry/PenchantModules.java" {
  param($c)
  if ($c -notmatch '@Deprecated') {
    $c = $c -replace "(public static final $idType REDUCED_CURSES = Penchant\.id\(`"reduced_curses`"\);)", @"
/**
     * @deprecated Folded into {@link #LOOT_REWORK} in 1.4 / upstream 0.5.0.
     */
    @Deprecated
    public static final $idType REDUCED_CURSES = Penchant.id(`"reduced_curses`");
"@
  }
  $c = $c -replace "\r?\n\s*registerPack\(event, REDUCED_CURSES, NORMAL_SOURCE\);", ""
  return $c
}

# --- DataGenerator ---
Edit-File "src/main/java/archives/tater/penchant/PenchantDataGenerator.java" {
  param($c)
  $c = $c -replace "import archives\.tater\.penchant\.datagen\.CurseEnchantmentTagGenerator;\r?\n", ""
  # Add REPLACE_BOOK_LOOT_TAG to ZOMBIE flag call (with or without EFH)
  $c = $c -replace 'PenchantFlag\.ZOMBIE_SPAWN_PICKAXE\)', 'PenchantFlag.ZOMBIE_SPAWN_PICKAXE, PenchantFlag.REPLACE_BOOK_LOOT_TAG)'
  # Remove reduced_curses block
  $lines = $c -split "`r?`n"
  $out = New-Object System.Collections.Generic.List[string]
  for ($i=0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match '// reduced_curses') {
      while ($i -lt $lines.Count -and ($lines[$i] -match 'reduced_curses|CurseEnchantment|var curses')) { $i++ }
      $i--; continue
    }
    if ($lines[$i] -match 'CurseEnchantmentTagGenerator|^\s*var curses = ') { continue }
    $out.Add($lines[$i])
  }
  $c = $out -join "`n"
  # Only switch to getPackGenerator when that API exists (1.21.4+). Older keep getBuiltinDatapack.
  $mcMinor = [int](($mcVer -split '\.')[2])
  if ($c -match 'getBuiltinDatapack' -and $mcMinor -ge 4) {
    $c = $c -replace 'generator\.getBuiltinDatapack\(true, id\.getPath\(\), "resourcepacks/" \+ id\.getPath\(\)\)', 'generator.getPackGenerator(true, id.getPath(), "resourcepacks/" + id.getPath())'
  }
  return $c
}
Remove-Item "src/main/java/archives/tater/penchant/datagen/CurseEnchantmentTagGenerator.java" -Force -ErrorAction SilentlyContinue

# --- UI ---
Edit-File "src/main/java/archives/tater/penchant/client/gui/widget/EnchantmentSlotWidget.java" {
  param($c) $c -replace 'if \(!isUnlocked && canUse\)', 'if (!isUnlocked && !alreadyAdded)'
}
Edit-File "src/main/java/archives/tater/penchant/client/gui/screen/PenchantmentScreen.java" {
  param($c)
  if ($c -match 'creative \|\| menu\.isAvailable') { return $c }
  return $c -replace '(\s+)menu\.isAvailable\(enchantment\)', '$1creative || menu.isAvailable(enchantment)'
}
Edit-File "src/main/java/archives/tater/penchant/menu/PenchantmentMenu.java" {
  param($c)
  if ($c -match 'var creative = player\.isCreative\(\);\s*\n\s*displayedEnchantments = streamOrdered') { return $c }
  $c = $c -replace '(if \(isEnchanting\(\)\) \{\n)(\s+displayedEnchantments = streamOrdered)', "`$1            var creative = player.isCreative();`n`$2"
  $c = $c -replace '(\.sorted\(comparingInt\(enchantment ->\n\s+)!availableEnchantments\.contains\(enchantment\) && !PenchantmentHelper\.hasEnchantment\(stack, enchantment\) \? 2', '$1creative || !availableEnchantments.contains(enchantment) && !PenchantmentHelper.hasEnchantment(stack, enchantment) ? 2'
  return $c
}

# --- Mixins ---
Set-File "src/main/java/archives/tater/penchant/mixin/loot/EnchantRandomlyFunctionMixin.java" @'
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
Set-File "src/main/java/archives/tater/penchant/mixin/loot/EnchantWithLevelsFunctionMixin.java" @'
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
Edit-File "src/main/resources/penchant.mixins.json" {
  param($c)
  if ($c -match 'loot\.EnchantRandomlyFunctionMixin') { return $c }
  return $c -replace '("loot\.LootTableAccessor",)', "`$1`n    `"loot.EnchantRandomlyFunctionMixin`",`n    `"loot.EnchantWithLevelsFunctionMixin`","
}

# --- version / readme / lang ---
Edit-File "gradle.properties" { param($c) $c -replace 'mod_version=.*', "mod_version=$mcVer-1.4" }
Edit-File "README.md" {
  param($c)
  $c = $c -replace '\*\*0\.4\.[0-9]\*\*', '**0.5.0**'
  $c = $c -replace '\*\*Port version\*\* \| \*\*[^*]+\*\*', '**Port version** | **1.4**'
  $c = $c -replace "\*\*Mod version\*\* \| [^\r\n]+", "**Mod version** | $mcVer-1.4"
  $c = $c -replace '\| `loot_rework` \| .+ \| Reworks where enchantment books appear in loot & mob equipment \|', '| `loot_rework` | ✔ | Reworks loot/trades; uncategorized off table; curses off trades & loot books |'
  $c = $c -replace '\| `reduced_curses` \| .+ \| Reduces how often curses appear \|\r?\n', ''
  $c = $c -replace '### Item tags & components \(0\.4\.[0-9](?:-fix)?\)', '### Item tags & components (0.5.0)'
  return $c
}
Get-ChildItem "src/main/resources/assets/penchant/lang/*.json" | ForEach-Object {
  $c = [IO.File]::ReadAllText($_.FullName)
  $c2 = [regex]::Replace($c, '\s*"dataPack\.penchant\.reduced_curses\.name"\s*:\s*"[^"]*"\s*,?\r?\n', '')
  $c2 = [regex]::Replace($c2, '\s*"dataPack\.penchant\.reduced_curses\.description"\s*:\s*"[^"]*"\s*,?\r?\n', '')
  if ($c2 -ne $c) { [IO.File]::WriteAllText($_.FullName, $c2, (New-Object System.Text.UTF8Encoding $false)) }
}
Edit-File "build.gradle" {
  param($c)
  if ($c -match 'data \{\s*data\(\)') { $c = $c -replace '(data \{\s*)data\(\)', '$1clientData()' }
  return $c
}
if (Test-Path "src/generated/resources/resourcepacks/reduced_curses") {
  Remove-Item -Recurse -Force "src/generated/resources/resourcepacks/reduced_curses"
}

# --- Build ---
Remove-Item -Recurse -Force "src/generated/resources/.cache" -ErrorAction SilentlyContinue
$adapt = @("no Enderscape","no cost-factor","isCreative()","TagAppender<T>=$(-not $usesRK)","EFH=$hasEFH","id=$idType")

Write-Host "=== compileJava ==="
cmd /c "gradlew.bat compileJava --no-daemon"
if ($LASTEXITCODE -ne 0) {
  # Try without DENSITY/BREACH
  Edit-File "src/main/java/archives/tater/penchant/datagen/LootEnchantmentTagGenerator.java" {
    param($c)
    $c = $c -replace '\s*Enchantments\.DENSITY,\r?\n\s*Enchantments\.BREACH,\r?\n', "`n"
    $c = $c -replace ',\r?\n\s*Enchantments\.DENSITY\r?\n', "`n"
    return $c
  }
  $adapt += "removed DENSITY/BREACH"
  cmd /c "gradlew.bat compileJava --no-daemon"
  if ($LASTEXITCODE -ne 0) { throw "compile failed $Branch" }
}

Write-Host "=== runData ==="
cmd /c "gradlew.bat runData --no-daemon"
if ($LASTEXITCODE -ne 0) { $adapt += "runData failed" }
else {
  foreach ($m in @("bookshelf_placement","durability_rework","guaranteed_drops","loot_rework","no_anvil_books","table_rework","randomized_librarians")) {
    $src = "src/generated/resources/data/$m/datapacks/resourcepacks/$m"
    if (Test-Path $src) {
      $dst = "src/generated/resources/resourcepacks/$m"
      if (Test-Path $dst) { Remove-Item -Recurse -Force $dst }
      New-Item -ItemType Directory -Path (Split-Path $dst) -Force | Out-Null
      Move-Item $src $dst
      Remove-Item -Recurse -Force "src/generated/resources/data/$m" -ErrorAction SilentlyContinue
      $adapt += "moved nested $m"
    }
  }
}

cmd /c "gradlew.bat jar --no-daemon"
if ($LASTEXITCODE -ne 0) { throw "jar failed $Branch" }
New-Item -ItemType Directory -Path releases -Force | Out-Null
$jar = Get-ChildItem "build/libs/penchant-neoforge-*.jar" | Where-Object { $_.Name -match "$mcVer-1\.4" } | Select-Object -First 1
if (-not $jar) { $jar = Get-ChildItem "build/libs/penchant-neoforge-*.jar" | Sort-Object LastWriteTime -Descending | Select-Object -First 1 }
if (-not $jar) { throw "no jar built" }
Copy-Item $jar.FullName releases/ -Force
Write-Host "Copied $($jar.Name)"

# --- Commit via cmd commit-tree ---
& $git add -A
& $git reset HEAD -- build/ _port14.ps1 _port14b.ps1 2>$null
$msgFile = Join-Path $env:TEMP "penchant-commit-msg.txt"
[IO.File]::WriteAllText($msgFile, "1.4`n")
$tree = (& $git write-tree).Trim()
$parent = (& $git rev-parse HEAD).Trim()
$env:GIT_AUTHOR_NAME="woxayz"; $env:GIT_AUTHOR_EMAIL="woxayz@gmail.com"
$env:GIT_COMMITTER_NAME="woxayz"; $env:GIT_COMMITTER_EMAIL="woxayz@gmail.com"
$commit = (cmd /c "`"$git`" commit-tree $tree -p $parent -F `"$msgFile`"").Trim()
if ($commit -notmatch '^[0-9a-f]{40}$') { throw "bad commit: $commit" }
cmd /c "`"$git`" update-ref refs/heads/$Branch $commit"
$body = (cmd /c "`"$git`" log -1 --format=%B").Trim()
if ($body -ne "1.4") { throw "bad message: [$body]" }
cmd /c "`"$git`" tag -f $mcVer-1.4 $commit"
cmd /c "`"$git`" remote set-url --push origin https://WoXayZ@github.com/WoXayZ/PenchantNeoforged.git"
cmd /c "`"$git`" push origin $Branch"
cmd /c "`"$git`" push -f origin $mcVer-1.4"
Write-Host "DONE $Branch $commit adaptations=$($adapt -join '; ')"
[pscustomobject]@{Branch=$Branch;Status='success';Tag="$mcVer-1.4";Commit=$commit;Adaptations=($adapt -join '; ')} | Export-Csv "$env:TEMP\penchant14-results.csv" -Append -NoTypeInformation
