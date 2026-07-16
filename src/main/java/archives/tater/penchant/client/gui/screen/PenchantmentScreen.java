package archives.tater.penchant.client.gui.screen;

import archives.tater.penchant.Penchant;
import archives.tater.penchant.client.gui.ScrollbarComponent;
import archives.tater.penchant.client.gui.widget.EnchantmentSlotWidget;
import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.penchant.util.PenchantmentHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
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
    private static final Identifier TEXTURE = Penchant.id("textures/gui/container/enchanting_table.png");
    private static final Identifier BOOK_TEXTURE = Identifier.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private static final Identifier SCROLLLER_TEXTURE = Penchant.id("container/enchanting_table/scroller");
    private static final Identifier BOOK_ICON_TEXTURE = Penchant.id("container/enchanting_table/book");
    private static final Identifier GRINDSTONE_ICON_TEXTURE = Penchant.id("container/enchanting_table/grindstone");
    private static final int INFO_ICON_SIZE = 9;
    public static final Identifier LAPIS_LAZULI_SLOT_TEXTURE = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");
    public static final Identifier BOOK_SLOT_TEXTURE = Penchant.id("container/slot/book");
    private static final List<Identifier> INGREDIENT_SLOT_TEXTURES = List.of(
            LAPIS_LAZULI_SLOT_TEXTURE,
            BOOK_SLOT_TEXTURE
    );
    private static final List<Identifier> INGREDIENT_SLOT_TEXTURES_NO_DISENCHANT = List.of(
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
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return scrollbar.mouseClicked(event) || super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        return scrollbar.mouseDragged(event) || super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrollbar.mouseReleased();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollbar.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        renderBook(graphics, x, y);
        renderInfoBar(graphics);

        secondSlotBackground.render(menu, graphics, partialTick, leftPos, topPos);

        scrollbar.render(graphics);
    }

    private void renderBook(GuiGraphics guiGraphics, int x, int y) {
        var partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var open = lerp(partialTick, oOpen, this.open);
        var flip = lerp(partialTick, oFlip, this.flip);
        var x0 = x + 14;
        var y0 = y + 25;
        var x1 = x0 + 38;
        var y1 = y0 + 31;
        guiGraphics.submitBookModelRenderState(requireNonNull(bookModel), BOOK_TEXTURE, 40.0F, open, flip, x0, y0, x1, y1);
    }

    private void renderInfoBar(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        var count = Integer.toString(menu.getBookCount());
        var status = menu.hasDisenchanter() ? "✔" : "❌";
        var countWidth = font.width(count);
        var statusWidth = font.width(status);
        var totalWidth = INFO_ICON_SIZE + 2 + countWidth + 4 + INFO_ICON_SIZE + 2 + statusWidth;
        var x = leftPos + 32 - totalWidth / 2;
        var y = topPos + 18;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_ICON_TEXTURE, x, y, INFO_ICON_SIZE, INFO_ICON_SIZE);
        x += INFO_ICON_SIZE + 2;
        graphics.drawString(font, count, x, y + 1, 0xFF606060, false);
        x += countWidth + 4;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GRINDSTONE_ICON_TEXTURE, x, y, INFO_ICON_SIZE, INFO_ICON_SIZE);
        x += INFO_ICON_SIZE + 2;
        graphics.drawString(font, status, x, y + 1, 0xFF606060, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.index <= 1) {
            var slotTooltip = hoveredSlot.index == 0
                    ? ENCHANTING_SLOT_TOOLTIP
                    : menu.canDisenchant()
                            ? INGREDIENT_SLOT_DISENCHANT_TOOLTIP
                            : INGREDIENT_SLOT_TOOLTIP;
            graphics.setComponentTooltipFromElementsForNextFrame(
                    font,
                    font.splitIgnoringLanguage(slotTooltip, TOOLTIP_WIDTH).stream().map(Either::<FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>left).toList(),
                    mouseX,
                    mouseY,
                    ItemStack.EMPTY
            );
        }
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
