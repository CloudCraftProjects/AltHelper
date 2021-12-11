package tk.booky.althelper.commands;
// Created by booky10 in AltHelper (14:22 15.12.20)

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import tk.booky.althelper.AltHelperMain;
import tk.booky.althelper.utils.AltHelperUtils;
import tk.booky.althelper.utils.InetAddressValidator;
import tk.booky.althelper.utils.UUIDFetcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("NullableProblems")
public class AltHelperCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("althelper.use")) {
            AtomicBoolean showAll = new AtomicBoolean(true);
            AtomicReference<UUID> targetUUID = new AtomicReference<>();
            AtomicReference<String> targetAddress = new AtomicReference<>();

            sender.sendMessage("Please wait... (Could take some time)");
            if (args.length == 1) {
                if (!args[0].equalsIgnoreCase("all")) try {
                    targetUUID.set(UUID.fromString(args[0]));
                    if (targetUUID.get() == null) throw new IllegalArgumentException();
                } catch (IllegalArgumentException exception) {
                    targetUUID.set(UUIDFetcher.getUUIDAt(args[0].toLowerCase(), System.currentTimeMillis()));
                    if (targetUUID.get() == null) {
                        InetAddressValidator validator = InetAddressValidator.getInstance();
                        if (validator.isValidInet4Address(args[0]))
                            targetAddress.set(args[0]);
                        else if (args[0].endsWith("*") && validator.isValidInet4Address(args[0].replace('*', '0')))
                            targetAddress.set(args[0]);
                        else return false;
                    }
                }
                else if (!sender.hasPermission("althelper.use.all"))
                    return false;
            } else if (args.length != 0) return false;
            else showAll.set(false);

            Bukkit.getScheduler().runTaskAsynchronously(AltHelperMain.main, () -> {
                HashMap<String, List<UUID>> userAddresses = AltHelperUtils.getAddressesWithUsers();

                if (targetAddress.get() != null) {
                    for (String address : new ArrayList<>(userAddresses.keySet())) {
                        if (!matchesCompiled(address, targetAddress.get())) continue;
                        userAddresses.remove(address);
                    }
                }

                if (targetAddress.get() == null && targetUUID.get() == null) {
                    for (String address : new ArrayList<>(userAddresses.keySet())) {
                        if (userAddresses.get(address).size() > 1) continue;
                        userAddresses.remove(address);
                    }
                }

                String targetPlayer = null;
                if (targetUUID.get() != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(targetUUID.get());
                    if (!player.hasPlayedBefore()) {
                        sender.sendMessage("§cPlayer hasn't played on this server yet!");
                        return;
                    } else {
                        String name = player.getName() + "§r";
                        if (player.isBanned()) targetPlayer = "§4" + name;
                        else if (player.isOnline()) targetPlayer = "§a" + name;
                        else targetPlayer = "§c" + name;
                    }
                }

                List<String> output = new ArrayList<>();
                for (String address : userAddresses.keySet()) {
                    List<String> users = new ArrayList<>();
                    boolean online = false, show = false;

                    for (UUID uuid : userAddresses.get(address)) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        String name = player.getName() + "§r";
                        if (player.isBanned()) {
                            users.add("§4" + name);
                            show = true;
                        } else if (player.isOnline()) {
                            users.add("§a" + name);
                            online = true;
                        } else users.add("§c" + name);
                    }
                    if (targetPlayer != null && !users.contains(targetPlayer)) continue;

                    String color = Bukkit.getIPBans().contains(address) ? "§4" : online ? "§a" : "§c";
                    if ((showAll.get() || (!color.equals("§4") && show)) && users.size() > 0) output.add(String.format(AltHelperUtils.FORMAT, users.size(), color + address + "§r", String.join(", ", users)));
                }

                if (output.size() > 0) for (String outputPart : output) sender.sendMessage(outputPart);
                else sender.sendMessage("§cThere is nothing to print!");
            });
        } else
            return false;
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private static boolean matchesCompiled(String address1, String address2) {
        if (address1.equals(address2)) return true;

        if (address1.endsWith("*") || address2.endsWith("*")) {
            String[] addressSplit1 = address1.split("\\."), addressSplit2 = address2.split("\\.");
            return addressSplit1[0].equals(addressSplit2[0]) && addressSplit1[1].equals(addressSplit2[1]) && addressSplit1[2].equals(addressSplit2[2]);
        }

        return false;
    }
}