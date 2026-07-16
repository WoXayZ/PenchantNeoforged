package archives.tater.penchant.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// PATCHOULI: Re-enable when a compatible Patchouli release exists for this Minecraft version.
// import net.minecraft.server.level.ServerPlayer;
// import archives.tater.penchant.Penchant;
// import vazkii.patchouli.api.PatchouliAPI;

public class TomeOfPenchantItem extends Item {
    public TomeOfPenchantItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // PATCHOULI: Opens the in-game guidebook.
        // if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
        //     PatchouliAPI.get().openBookGUI(serverPlayer, Penchant.id("tome_of_penchant"));
        // }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
