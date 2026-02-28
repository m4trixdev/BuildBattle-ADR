package br.com.m4trixdev.util;

import org.bukkit.ChatColor;

public final class ColorUtil {

    private ColorUtil() {}

    public static String c(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
