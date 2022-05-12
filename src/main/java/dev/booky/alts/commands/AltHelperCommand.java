package dev.booky.alts.commands;
// Created by booky10 in AltHelper (14:22 15.12.20)

import dev.booky.alts.AltHelperMain;
import dev.booky.alts.util.DisplayedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public record AltHelperCommand(AltHelperMain main) implements TabExecutor {

    private static final Pattern USERNAME_REGEX = Pattern.compile("^\\w{2,16}$");

    private static final Pattern IP4_REGEX = Pattern.compile("^(\\b25[0-5]|\\b2[0-4]\\d|\\b[01]?\\d\\d?)(\\.(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)){3}$");
    private static final Pattern IP6_REGEX = Pattern.compile("^(([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}|([\\da-fA-F]{1,4}:){1,7}:|([\\da-fA-F]{1,4}:){1,6}:[\\da-fA-F]{1,4}|([\\da-fA-F]{1,4}:){1,5}(:[\\da-fA-F]{1,4}){1,2}|([\\da-fA-F]{1,4}:){1,4}(:[\\da-fA-F]{1,4}){1,3}|([\\da-fA-F]{1,4}:){1,3}(:[\\da-fA-F]{1,4}){1,4}|([\\da-fA-F]{1,4}:){1,2}(:[\\da-fA-F]{1,4}){1,5}|[\\da-fA-F]{1,4}:((:[\\da-fA-F]{1,4}){1,6})|:((:[\\da-fA-F]{1,4}){1,7}|:)|fe80:(:[\\da-fA-F]{0,4}){0,4}%[\\da-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}(25[0-5]|(2[0-4]|1?\\d)?\\d)|([\\da-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?\\d)?\\d)\\.){3}(25[0-5]|(2[0-4]|1?\\d)?\\d))$");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("althelper.use")) return false;

        if (Bukkit.isPrimaryThread()) {
            sender.sendMessage(Component.text("Please wait, searching...", NamedTextColor.GRAY));
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> onCommand(sender, command, label, args));
            return true;
        }

        if (args.length == 0) {
            executeAll(sender, label, true);
            return true;
        }

        if ("all".equalsIgnoreCase(args[0])) {
            executeAll(sender, label, false);
            return true;
        }

        try {
            UUID targetUniqueId = UUID.fromString(args[0]);
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUniqueId);

            executePlayer(sender, label, target);
            return true;
        } catch (IllegalArgumentException exception) {
            if (!USERNAME_REGEX.matcher(args[0]).matches()) {
                executeIp(sender, label, args[0]);
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            executePlayer(sender, label, target);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length != 1) return Collections.emptyList();
        if (!"all".startsWith(args[0].toLowerCase())) return Collections.emptyList();
        return Collections.singletonList("all");
    }

    private void executeAll(CommandSender sender, String label, boolean requireBanned) {
        TextComponent.Builder builder = Component.text().color(NamedTextColor.WHITE);
        Component comma = null;

        Set<InetAddress> addresses = new HashSet<>(main.data().values());
        for (InetAddress address : addresses) {
            Collection<UUID> uniqueIds = main.uniqueIds(address);
            if (uniqueIds.size() < 2) continue;

            if (requireBanned) {
                boolean banned = false;
                for (UUID uuid : uniqueIds) {
                    if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(uuid.toString())) continue;
                    banned = true;
                }

                if (!banned) {
                    continue;
                }
            }

            if (comma == null) comma = Component.text(", ");
            else builder.append(comma);

            String host = address.getHostAddress();
            if (address instanceof Inet6Address) {
                int scopeIndex = host.indexOf('%');
                if (scopeIndex != -1) {
                    host = host.substring(0, scopeIndex);
                }
            }

            builder.append(Component.text(host, DisplayedColor.color(address))
                    .clickEvent(ClickEvent.runCommand("/" + label + " " + host))
                    .hoverEvent(HoverEvent.showText(Component.text(uniqueIds.size() + " players"))));
        }

        if (builder.children().size() == 0) {
            sender.sendMessage(Component.text("No addresses have been found", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(builder.build());
    }

    private void executePlayer(CommandSender sender, String label, OfflinePlayer target) {
        Collection<InetAddress> addresses = main.addresses(target.getUniqueId());
        if (addresses.size() == 0) {
            Component name = componentName(target);
            sender.sendMessage(Component.text("No addresses have been found for ", NamedTextColor.RED).append(name));
            return;
        }

        TextComponent.Builder builder = Component.text().color(NamedTextColor.WHITE);
        builder.append(componentName(target));
        builder.append(Component.text(" has logged in using the following ip addresses: "));

        Component comma = null;
        for (InetAddress address : addresses) {
            if (comma == null) comma = Component.text(", ", NamedTextColor.WHITE);
            else builder.append(comma);

            int seenTimes = main.uniqueIds(address).size();
            Component seen = Component.text("Seen " + seenTimes + " time" + (seenTimes == 1 ? "" : "s"), NamedTextColor.AQUA);

            String host = address.getHostAddress();
            if (address instanceof Inet6Address) {
                int scopeIndex = host.indexOf('%');
                if (scopeIndex != -1) {
                    host = host.substring(0, scopeIndex);
                }
            }

            builder.append(Component.text(host, DisplayedColor.color(address))
                    .hoverEvent(HoverEvent.showText(seen)).clickEvent(ClickEvent.runCommand("/" + label + " " + host)));
        }

        sender.sendMessage(builder.build());
    }

    private void executeIp(CommandSender sender, String label, String ip) {
        if (ip.contains("*")) {
            executeIp0(sender, label, ip);
            return;
        }

        if (!IP4_REGEX.matcher(ip).matches() && !IP6_REGEX.matcher(ip).matches()) {
            sender.sendMessage(Component.text("Please enter a proper ipv4 address, ipv6 address or username", NamedTextColor.RED));
            return;
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            executeIp0(sender, label, address);
        } catch (UnknownHostException exception) {
            sender.sendMessage(Component.text("Please enter a proper ipv4 address, ipv6 address or username", NamedTextColor.RED));
        }
    }

    private void executeIp0(CommandSender sender, String label, String ip) {
        Collection<UUID> uniqueIds = main.uniqueIds(ip);
        if (uniqueIds.size() == 0) {
            sender.sendMessage(Component.text("No users have been found for ", NamedTextColor.RED).append(Component.text(ip, NamedTextColor.GREEN)));
            return;
        }

        TextComponent.Builder builder = Component.text().color(NamedTextColor.WHITE);
        builder.append(Component.text("The following users have logged in with "));
        builder.append(Component.text(ip, NamedTextColor.GREEN));
        builder.append(Component.text(": "));

        Component comma = null;
        for (UUID uniqueId : uniqueIds) {
            if (comma == null) comma = Component.text(", ", NamedTextColor.WHITE);
            else builder.append(comma);

            int addresses = main.addresses(uniqueId).size();
            Component usedIps = Component.text("Used " + addresses + " " + (addresses == 1 ? "ip" : "different ips"), NamedTextColor.AQUA);

            Component name = componentName(Bukkit.getOfflinePlayer(uniqueId));
            String plainName = PlainTextComponentSerializer.plainText().serialize(name);
            builder.append(name.hoverEvent(HoverEvent.showText(usedIps)).clickEvent(ClickEvent.runCommand("/" + label + " " + plainName)));
        }

        sender.sendMessage(builder.build());
    }

    private void executeIp0(CommandSender sender, String label, InetAddress address) {
        String host = address.getHostAddress();
        if (address instanceof Inet6Address) {
            int scopeIndex = host.indexOf('%');
            if (scopeIndex != -1) {
                host = host.substring(0, scopeIndex);
            }
        }

        Collection<UUID> uniqueIds = main.uniqueIds(address);
        if (uniqueIds.size() == 0) {
            sender.sendMessage(Component.text("No users have been found for ", NamedTextColor.RED).append(Component.text(host, DisplayedColor.color(address))));
            return;
        }

        TextComponent.Builder builder = Component.text().color(NamedTextColor.WHITE);
        builder.append(Component.text("The following users have logged in with "));
        builder.append(Component.text(host, DisplayedColor.color(address)));
        builder.append(Component.text(": "));

        Component comma = null;
        for (UUID uniqueId : uniqueIds) {
            if (comma == null) comma = Component.text(", ", NamedTextColor.WHITE);
            else builder.append(comma);

            int addresses = main.addresses(uniqueId).size();
            Component usedIps = Component.text("Used " + addresses + " " + (addresses == 1 ? "ip" : "different ips"), NamedTextColor.AQUA);

            Component name = componentName(Bukkit.getOfflinePlayer(uniqueId));
            String plainName = PlainTextComponentSerializer.plainText().serialize(name);
            builder.append(name.hoverEvent(HoverEvent.showText(usedIps)).clickEvent(ClickEvent.runCommand("/" + label + " " + plainName)));
        }

        sender.sendMessage(builder.build());
    }

    private static Component componentName(OfflinePlayer player) {
        Player online = player.getPlayer();
        if (online != null) return online.teamDisplayName();
        if (player.getName() != null) return Component.text(player.getName(), DisplayedColor.color(player));
        return Component.text(player.getUniqueId().toString(), DisplayedColor.color(player));
    }
}
