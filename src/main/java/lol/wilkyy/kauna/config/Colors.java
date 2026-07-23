package lol.wilkyy.kauna.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class Colors {

    public static final int[] RAINBOW_THEME = {0xFF5656, 0xFFAA00, 0xFFFF55, 0x57FF57, 0x55FFFF, 0xAA00AA, 0xFF55FF};

    public static final int[] PASTEL_THEME = {0xEC8C8C, 0xFFC58B, 0xFFFF9E, 0x9AFF9A, 0x97CFF5, 0xA39AFF, 0xEEA0FF,
            0xA39AFF, 0x97CFF5, 0x9AFF9A, 0xFFFF9E, 0xFFC58B};

    public static final int[] GAY_THEME = {0x3D1A77, 0x5049CB, 0x7BADE2, 0xFFFFFF, 0x98E9C1, 0x26CFAA, 0x078E70,
            0x26CFAA, 0x98E9C1, 0xFFFFFF, 0x7BADE2, 0x5049CB};

    public static final int[] LESBIAN_THEME = {0xD62800, 0xFF7600, 0xFF9B55, 0xFFFFFF, 0xD462A6, 0xB51368, 0x8C1B4F,
            0xB51368, 0xD462A6, 0xFFFFFF, 0xFF9B55, 0xFF7600};

    public static final int[] TRANS_THEME = {0x5BCFFB, 0xF5A9B8, 0xFFFFFF, 0xF5A9B8, 0x5BCFFB}; // already loops correctly

    public static final int[] YELLER_THEME = {0xFFE385, 0xFFB68D, 0xFF8692, 0xFF4993, 0xE993B3, 0xC2C9D3, 0x70FAF5,
            0xC2C9D3, 0xE993B3, 0xFF4993, 0xFF8692, 0xFFB68D};

    public static final int[] AURORA_THEME = {0x68F589, 0x7CE2A5, 0x8ACFBB, 0x96BACF, 0x9FA4E0, 0xA68CF0, 0xAC6FFF,
            0xA68CF0, 0x9FA4E0, 0x96BACF, 0x8ACFBB, 0x7CE2A5};

    public static final int[] LOVE_THEME = {0xFA709A, 0xFB7FA7, 0xFC8DB5, 0xFD9BC2, 0xFDA8D0, 0xFCB5DD, 0xFBC2EB,
            0xFCB5DD, 0xFDA8D0, 0xFD9BC2, 0xFC8DB5, 0xFB7FA7};

    public static Component getFormattedThemeName(String value) {
        return switch (value) {
            case "Gay" -> Component.empty()
                    .append(cB("G", 0x078E70)).append(cB("a", 0x26CFAA)).append(cB("y", 0x98E9C1))
                    .append(cB(" ", 0xFFFFFF)).append(cB("(", 0xFFFFFF)).append(cB("M", 0x7BADE2))
                    .append(cB("L", 0x5049CB)).append(cB("M", 0x3D1A77)).append(cB(")", 0x3D1A77));

            case "Lesbian" -> Component.empty()
                    .append(cB("L", 0xD62800)).append(cB("e", 0xFF7600)).append(cB("s", 0xFF9B55))
                    .append(cB("b", 0xFFFFFF)).append(cB("i", 0xD462A6)).append(cB("a", 0xB51368))
                    .append(cB("n", 0x8C1B4F));

            case "Trans" -> Component.empty()
                    .append(cB("T", 0x5BCFFB)).append(cB("r", 0xF5A9B8)).append(cB("a", 0xFFFFFF))
                    .append(cB("n", 0xF5A9B8)).append(cB("s", 0x5BCFFB));

            case "Pastel" -> Component.empty()
                    .append(cB("P", 0xEC8C8C)).append(cB("a", 0xFFC58B)).append(cB("s", 0xFFFF9E))
                    .append(cB("t", 0x9AFF9A)).append(cB("e", 0x97CFF5)).append(cB("l", 0xA39AFF));

            case "Yeller" -> Component.empty()
                    .append(cB("Y", 0xFFE385)).append(cB("e", 0xFFB68D)).append(cB("l", 0xFF8692))
                    .append(cB("l", 0xFF4993)).append(cB("e", 0xE993B3)).append(cB("r", 0xC2C9D3));

            case "Aurora" -> Component.empty()
                    .append(cB("A", 0x68F589)).append(cB("u", 0x7CE2A5)).append(cB("r", 0x8ACFBB))
                    .append(cB("o", 0x96BACF)).append(cB("r", 0x9FA4E0)).append(cB("a", 0xA68CF0));

            case "Love" -> Component.empty()
                    .append(cB("L", 0xFA709A)).append(cB("o", 0xFB7FA7)).append(cB("v", 0xFC8DB5))
                    .append(cB("e", 0xFD9BC2));

            default -> Component.empty()
                    .append(cB("R", 0xFF5656)).append(cB("a", 0xFFAA00)).append(cB("i", 0xFFFF55))
                    .append(cB("n", 0x57FF57)).append(cB("b", 0x55FFFF)).append(cB("o", 0xAA00AA))
                    .append(cB("w", 0xFF55FF));
        };
    }

    public static Component getProxyName(String value) {
        return switch (value) {
            case "Aula" -> Component.empty()
                    .append(Component.literal("Aula").withStyle(ChatFormatting.GOLD));

            case "Kahakka" -> Component.empty()
                    .append(c("K", 0xf10000)).append(c("a", 0xf4220a))
                    .append(c("h", 0xf63313)).append(c("a", 0xf8401b)).append(c("k", 0xfb4b21))
                    .append(c("k", 0xfd5527)).append(c("a", 0xff5f2c));

            case "Survival" -> Component.empty()
                    .append(c("S", 0x29ce00)).append(c("u", 0x27d514)).append(c("r", 0x26dc20))
                    .append(c("v", 0x23e32a)).append(c("i", 0x21ea33))
                    .append(c("v", 0x1ef13b)).append(c("a", 0x1af842)).append(c("l", 0x15ff49));

            case "Tyrmä" -> Component.empty()
                    .append(c("T", 0x03acae)).append(c("y", 0x01b4ad)).append(c("r", 0x00bcac))
                    .append(c("m", 0x00c4ab)).append(c("ä", 0x00ccaa));

            case "Liitotaisto" -> Component.empty()
                    .append(c("L", 0xee41b0)).append(c("i", 0xef49b3)).append(c("i", 0xf150b6))
                    .append(c("t", 0xf256b9)).append(c("o", 0xf45dbc))
                    .append(c("t", 0xf563bf)).append(c("a", 0xf669c3)).append(c("i", 0xf86ec6)).append(c("s", 0xf974c9))
                    .append(c("t", 0xfa7acc)).append(c("o", 0xfb7fcf));

            case "Creative" -> Component.empty()
                    .append(c("C", 0xe78844)).append(c("r", 0xeb9241)).append(c("e", 0xef9c3d))
                    .append(c("a", 0xf2a639)).append(c("t", 0xf5b033)).append(c("i", 0xf9ba2c))
                    .append(c("v", 0xfcc423)).append(c("e", 0xffce13));

            default -> Component.literal(value).withStyle(ChatFormatting.GOLD);
        };
    }

    private static Component cB(String character, int rgb) { // Character bold
        return Component.literal(character).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true));
    }

    private static Component c(String character, int rgb) { // normal
        return Component.literal(character).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }
}