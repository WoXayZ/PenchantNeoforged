package archives.tater.penchant.client.gui.screen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.client.FontUtils;
import archives.tater.penchant.client.gui.ScrollbarComponent;
import archives.tater.penchant.client.gui.widget.EnchantmentSlotWidget;
import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.util.PenchantmentHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.minecraft.util.Mth.clamp;
import static net.minecraft.util.Mth.lerp;

public class PenchantmentScreen extends AbstractContainerScreen<PenchantmentMenu> {
    private static final ResourceLocation TEXTURE = Penchant.id("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private static final ResourceLocation SCROLLLER_TEXTURE = Penchant.id("container/enchanting_table/scroller");
    public static final ResourceLocation LAPIS_LAZULI_SLOT_TEXTURE = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");
    public static final ResourceLocation BOOK_SLOT_TEXTURE = Penchant.id("item/empty_slot_book");
    private static final List<ResourceLocation> INGREDIENT_SLOT_TEXTURES = List.of(
            LAPIS_LAZULI_SLOT_TEXTURE,
            BOOK_SLOT_TEXTURE
    );
    private static final List<ResourceLocation> INGREDIENT_SLOT_TEXTURES_NO_DISENCHANT = List.of(
            LAPIS_LAZULI_SLOT_TEXTURE
    );
    private static final Component ENCHANTING_SLOT_TOOLTIP = Component.translatable("container.penchant.enchant.slot.enchant");
    private static final Component INGREDIENT_SLOT_TOOLTIP = Component.translatable("container.penchant.enchant.slot.ingredient");
    private static final Component INGREDIENT_SLOT_DISENCHANT_TOOLTIP = Component.translatable("container.penchant.enchant.slot.ingredient.disenchant");
    private static final int TOOLTIP_WIDTH = 115;

    private final ScrollbarComponent scrollbar = new ScrollbarComponent(
            SCROLLLER_TEXTURE,
            6,
            19,
            60,
            EnchantmentSlotWidget.WIDTH + 1,
            60,
            this::rebuildWidgets
    );

    private final CyclingSlotBackground secondSlotBackground = new CyclingSlotBackground(1);

    private final RandomSource random = RandomSource.create();
    private @Nullable BookModel bookModel;

    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;

    public PenchantmentScreen(PenchantmentMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 206;
        imageHeight = 172;
        inventoryLabelX = 23;
        inventoryLabelY = imageHeight - 94;
        menu.setSlotChangeListener(this::rebuildWidgets);
    }

    @Override
    protected void init() {
        super.init();
        bookModel = new BookModel(minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
        var displayedEnchantments = menu.getDisplayedEnchantments();
        var stack = menu.getEnchantingStack();
        scrollbar.update(
                leftPos + 192,
                topPos + 14,
                leftPos + 60,
                topPos + 14,
                displayedEnchantments.size() - 4
        );

        var creative = requireNonNull(minecraft.player).isCreative();
        if (!menu.isEnchanting() && !menu.isDisenchanting()) return;
        var disenchanting = menu.isDisenchanting();
        for (var i = 0; i < 5; i++) {
            var index = scrollbar.getPosition() + i;
            if (index >= displayedEnchantments.size()) break;
            var enchantment = displayedEnchantments.get(index);
            if (disenchanting)
                addRenderableWidget(new EnchantmentSlotWidget(
                        leftPos + 60,
                        topPos + 14 + i * EnchantmentSlotWidget.HEIGHT,
                        enchantment,
                        getIncompatible(menu.getIngredientStack(), enchantment),
                        !PenchantmentHelper.hasEnchantment(menu.getIngredientStack(), enchantment),
                        creative || PenchantmentHelper.getBookRequirement(enchantment) <= menu.getBookCount()
                ));
            else
                addRenderableWidget(new EnchantmentSlotWidget(
                        leftPos + 60,
                        topPos + 14 + i * EnchantmentSlotWidget.HEIGHT,
                        enchantment,
                        getIncompatible(stack, enchantment),
                        PenchantmentHelper.canEnchant(stack, enchantment),
                        PenchantmentHelper.hasEnchantment(stack, enchantment),
                        creative || !menu.getIngredientStack().isEmpty(),
                        creative || PenchantmentHelper.getBookRequirement(enchantment) <= menu.getBookCount(),
                        creative || PenchantmentHelper.getXpLevelCost(enchantment) <= menu.getPlayerXp(),
                        menu.isAvailable(enchantment)
                ));
        }
    }

    private List<Holder<Enchantment>> getIncompatible(ItemStack stack, Holder<Enchantment> enchantment) {
        return PenchantmentHelper.hasEnchantment(stack, enchantment)
                ? List.of()
                : PenchantmentHelper.getEnchantments(stack).keySet().stream().filter(other -> !enchantment.equals(other) && !Enchantment.areCompatible(enchantment, other)).toList();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        secondSlotBackground.tick(menu.canDisenchant() ? INGREDIENT_SLOT_TEXTURES : INGREDIENT_SLOT_TEXTURES_NO_DISENCHANT);
        tickBook();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return scrollbar.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return scrollbar.mouseDragged(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrollbar.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollbar.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        renderBook(graphics, x, y, partialTick);

        var font = Minecraft.getInstance().font;
        var infoText = Component.empty()
                .append(FontUtils.BOOK_TEXT)
                .append(FontUtils.THIN_SPACE_TEXT)
                .append(Integer.toString(menu.getBookCount()))
                .append(" ")
                .append(FontUtils.GRINDSTONE_TEXT)
                .append(FontUtils.THIN_SPACE_TEXT)
                .append(menu.hasDisenchanter() ? "✔" : "❌");
        graphics.drawString(font, infoText, leftPos + 32 - font.width(infoText) / 2, topPos + 18, 0xFF606060, false);

        secondSlotBackground.render(menu, graphics, partialTick, leftPos, topPos);

        scrollbar.render(graphics);
    }

    private void renderBook(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        var open = lerp(partialTick, oOpen, this.open);
        var flip = lerp(partialTick, oFlip, this.flip);
        var i = x + 14;
        var j = y + 14;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(i + 19, j + 16, 50.0F);
        float f2 = 1.0F - open;
        f2 = 1.0F - f2 * f2 * f2;
        f2 *= 90.0F / (flip + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F - f2 * 20.0F));
        float f3 = lerp(partialTick, oFlip, this.flip);
        poseStack.translate(1.0F, (f3 + 0.25F) * 0.1F, 0.0F);
        float f4 = 1.0F - (f3 + 0.75F) * 0.1F;
        poseStack.scale(f4, f4, f4);
        float f5 = -(f3 + 0.2F) * 20.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(f5));
        poseStack.mulPose(Axis.YP.rotationDegrees(80.0F));
        requireNonNull(bookModel).setupAnim(0.0F, f3, Mth.clamp(open, 0.0F, 1.0F), 1.0F);
        var renderType = RenderType.entitySolid(BOOK_TEXTURE);
        var bufferSource = guiGraphics.bufferSource();
        bookModel.renderToBuffer(poseStack, bufferSource.getBuffer(renderType), 15728880, 0, -1);
        bufferSource.endBatch();
        poseStack.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index <= 1)
            graphics.renderTooltip(font, font.split(
                    hoveredSlot.index == 0
                            ? ENCHANTING_SLOT_TOOLTIP
                            : menu.canDisenchant()
                                    ? INGREDIENT_SLOT_DISENCHANT_TOOLTIP
                                    : INGREDIENT_SLOT_TOOLTIP,
                    TOOLTIP_WIDTH), mouseX, mouseY);
    }

    public void tickBook() {
        var stack = menu.getEnchantingStack();
        if (!ItemStack.matches(stack, last)) {
            last = stack;
            do {
                flipT = flipT + (random.nextInt(4) - random.nextInt(4));
            } while (flip <= flipT + 1.0F && flip >= flipT - 1.0F);
        }

        oFlip = flip;
        oOpen = open;

        open = clamp(open + (!menu.getDisplayedEnchantments().isEmpty() ? 0.2F : -0.2F), 0.0F, 1.0F);
        var f = clamp((flipT - flip) * 0.4F, -0.2F, 0.2F);
        flipA = flipA + (f - flipA) * 0.9F;
        flip = flip + flipA;
    }
}
