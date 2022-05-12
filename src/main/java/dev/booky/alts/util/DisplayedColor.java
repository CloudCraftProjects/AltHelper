package dev.booky.alts.util;
// Created by booky10 in AltHelper (21:16 11.05.22)

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.net.InetAddress;

public enum DisplayedColor {

    ONLINE(TextColor.color(0, 255, 0)),
    OFFLINE(TextColor.color(255, 0, 0)),
    BANNED(TextColor.color(127, 0, 0));

    private final TextColor color;

    DisplayedColor(TextColor color) {
        this.color = color;
    }

    public static TextColor color(OfflinePlayer player) {
        return getFor(player).color();
    }

    public static TextColor color(InetAddress address) {
        return getFor(address).color();
    }

    public static DisplayedColor getFor(OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) return ONLINE;
        if (player.isBanned()) return BANNED;
        return OFFLINE;
    }

    public static DisplayedColor getFor(InetAddress address) {
        // Invoking isBanned with the host address always results in false
        // TODO: This doesn't handle ipv6 addresses properly
        String hostAddress = address.getHostAddress();
        for (BanEntry entry : Bukkit.getBanList(BanList.Type.IP).getBanEntries()) {
            if (!hostAddress.equals(entry.getTarget())) continue;
            return BANNED;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getAddress() == null) continue;
            if (!player.getAddress().getAddress().equals(address)) continue;
            return ONLINE;
        }

        return OFFLINE;
    }

    public TextColor color() {
        return color;
    }
}
