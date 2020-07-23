package kdp.limelib.helper;

import java.awt.*;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.item.DyeColor;
import net.minecraft.util.math.MathHelper;

import org.apache.commons.lang3.Validate;

public class ColorHelper {

    public static int getRGB(DyeColor color) {
        return color.getColorValue() | 0xFF000000;
    }

    public static int getRGB(DyeColor color, int alpha) {
        return getRGB(getRGB(color), alpha);
    }

    public static int getRGB(int color, int alpha) {
        Validate.isTrue(alpha >= 0 && alpha <= 255, "alpha out of range " + alpha);
        return getRGB(getRed(color), getGreen(color), getBlue(color), alpha);
    }

    public static int getRGB(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF) << 0);
    }

    public static int getRGB(int red, int green, int blue) {
        return getRGB(red, green, blue, 0xFF);
    }

    public static void glColor(int color) {
        GlStateManager.color4f(getRed(color) / 255f, getGreen(color) / 255f, getBlue(color) / 255f, getAlpha(color) / 255f);
    }

    public static int getRed(int color) {
        return ((0xFF000000 | color) >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return ((0xFF000000 | color) >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return ((0xFF000000 | color) >> 0) & 0xFF;
    }

    public static int getAlpha(int color) {
        return ((color) >> 24) & 0xFF;
    }

    public static int getRainbow(int frequence) {
        if (frequence <= 0)
            frequence = 1;
        return Color.getHSBColor(((System.currentTimeMillis() / frequence) % 360l) / 360f, 1, 1).getRGB();
    }

    public static int brighter(int color, double factor) {
        factor = MathHelper.clamp(factor, 0, 1);
        int red = (int) Math.round(Math.min(255, getRed(color) + 255 * factor));
        int green = (int) Math.round(Math.min(255, getGreen(color) + 255 * factor));
        int blue = (int) Math.round(Math.min(255, getBlue(color) + 255 * factor));
        return new Color(red, green, blue, getAlpha(color)).getRGB();
    }

    public static int darker(int color, double factor) {
        factor = MathHelper.clamp(factor, 0, 1);
        int red = (int) Math.round(Math.max(0, getRed(color) - 255 * factor));
        int green = (int) Math.round(Math.max(0, getGreen(color) - 255 * factor));
        int blue = (int) Math.round(Math.max(0, getBlue(color) - 255 * factor));
        return new Color(red, green, blue, getAlpha(color)).getRGB();
    }
}
