package archives.tater.penchant.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class PenchantKeys {
    public static final KeyMapping SHOW_PROGRESS = new KeyMapping(
            "key.penchant.show_progress",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            "key.categories.penchant"
    );

    private PenchantKeys() {}

    /** Works in GUIs too (vanilla {@link KeyMapping#isDown()} is often false while a screen is open). */
    public static boolean isShowProgressDown() {
        if (SHOW_PROGRESS.isDown()) return true;
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, SHOW_PROGRESS.getKey().getValue());
    }
}
