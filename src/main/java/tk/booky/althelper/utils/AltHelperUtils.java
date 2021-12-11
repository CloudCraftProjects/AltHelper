package tk.booky.althelper.utils;
// Created by booky10 in AltHelper (15:00 15.12.20)

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tk.booky.althelper.AltHelperMain;

import java.io.File;
import java.util.*;

public class AltHelperUtils {

    public static final String FORMAT = "§r[%s§r] %s§r: %s§r";

    @Deprecated
    public static HashMap<UUID, String> getUserWithAddressesFromEssentials() {
        File pluginsFolder = new File(Bukkit.getWorldContainer(), "plugins");
        File essentialsFolder = new File(pluginsFolder, "Essentials");
        File userDataFolder = new File(essentialsFolder, "userdata");

        HashMap<UUID, String> map = new HashMap<>();
        File[] files = userDataFolder.listFiles();

        if (files != null) for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
            String address = data.getString("ipAddress", "null");
            map.put(uuid, address);
        }

        return map;
    }

    public static List<UUID> convertStringListToUUIDList(List<String> list) {
        List<UUID> uuidList = new ArrayList<>();
        list.forEach(uuid -> uuidList.add(UUID.fromString(uuid)));
        return uuidList;
    }

    public static HashMap<String, List<UUID>> getAddressesWithUsers() {
        FileConfiguration config = AltHelperMain.main.getConfig();

        HashMap<String, List<UUID>> map = new HashMap<>();
        Set<String> keys = Objects.requireNonNull(config.getConfigurationSection("data")).getKeys(false);

        keys.forEach(key -> map.put(key.replace("-", "."), convertStringListToUUIDList(config.getStringList("data." + key))));
        return map;
    }

    public static void saveAddress(String address, UUID uuid) {
        FileConfiguration config = AltHelperMain.main.getConfig();
        address = address.replace(".", "-");

        if (config.contains("data." + address)) {
            List<String> uuids = config.getStringList("data." + address);
            if (uuids.contains(uuid.toString())) return;

            uuids.add(uuid.toString());
            config.set("data." + address, uuids);
        } else {
            config.set("data." + address, Collections.singletonList(uuid.toString()));
        }
    }
}