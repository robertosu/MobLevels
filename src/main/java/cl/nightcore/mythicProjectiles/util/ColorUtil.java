package cl.nightcore.mythicProjectiles.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component parseColorCodes(String text) {
        // Convertir códigos hexadecimales del formato &#RRGGBB a <color:#RRGGBB>
        text = text.replaceAll("&#([0-9a-fA-F]{6})", "<color:#$1>");

        // Parsear el texto usando MiniMessage
        return MINI_MESSAGE.deserialize(text);
    }

}