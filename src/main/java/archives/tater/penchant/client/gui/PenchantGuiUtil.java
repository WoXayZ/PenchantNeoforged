package archives.tater.penchant.client.gui;

public class PenchantGuiUtil {
    private PenchantGuiUtil() {}

    public static boolean containsPoint(int x, int y, int width, int height, double mouseX, double mouseY) {
        return x < mouseX && mouseX < x + width && y < mouseY && mouseY < y + height;
    }

    public static boolean containsPoint(int x, int y, int width, int height, int mouseX, int mouseY) {
        return x < mouseX && mouseX < x + width && y < mouseY && mouseY < y + height;
    }
}
