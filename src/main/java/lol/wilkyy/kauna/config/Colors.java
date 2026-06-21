package lol.wilkyy.kauna.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class Colors {

    public static final int[] RAINBOW_THEME = {0xFF5656, 0xFFAA00, 0xFFFF55, 0x57FF57, 0x55FFFF, 0xAA00AA, 0xFF55FF};
    public static final int[] GAY_THEME = {0x3D1A77, 0x5049CB, 0x7BADE2, 0xFFFFFF, 0x98E9C1, 0x26CFAA, 0x078E70};
    public static final int[] LESBIAN_THEME = {0xD62800, 0xFF7600, 0xFF9B55, 0xFFFFFF, 0xD462A6, 0xB51368, 0x8C1B4F};
    public static final int[] TRANS_THEME = {0x5BCFFB, 0xF5A9B8, 0xFFFFFF, 0xF5A9B8, 0x5BCFFB};

    public static Component getFormattedThemeName(String value) {
        return switch (value) {
            case "Gay" -> Component.empty()
                    .append(c("G", 0x078E70)).append(c("a", 0x26CFAA)).append(c("y", 0x98E9C1))
                    .append(c(" ", 0xFFFFFF)).append(c("(", 0xFFFFFF)).append(c("M", 0x7BADE2))
                    .append(c("L", 0x5049CB)).append(c("M", 0x3D1A77)).append(c(")", 0x3D1A77));

            case "Lesbian" -> Component.empty()
                    .append(c("L", 0xD62800)).append(c("e", 0xFF7600)).append(c("s", 0xFF9B55))
                    .append(c("b", 0xFFFFFF)).append(c("i", 0xD462A6)).append(c("a", 0xB51368))
                    .append(c("n", 0x8C1B4F));

            case "Trans" -> Component.empty()
                    .append(c("T", 0x5BCFFB)).append(c("r", 0xF5A9B8)).append(c("a", 0xFFFFFF))
                    .append(c("n", 0xF5A9B8)).append(c("s", 0x5BCFFB));

            default -> Component.empty()
                    .append(c("R", 0xFF5656)).append(c("a", 0xFFAA00)).append(c("i", 0xFFFF55))
                    .append(c("n", 0x57FF57)).append(c("b", 0x55FFFF)).append(c("o", 0xAA00AA))
                    .append(c("w", 0xFF55FF));
        };
    }

    private static Component c(String character, int rgb) {
        return Component.literal(character).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true));
    }
}