package dev.booky.alts.util;
// Created by booky10 in AltHelper (20:11 11.05.22)

import com.google.common.collect.SetMultimap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class MigrationUtil {

    public static void migrate(Logger logger, File oldFile, SetMultimap<UUID, InetAddress> dataMap) {
        if (!oldFile.exists()) throw new IllegalArgumentException(oldFile.getAbsolutePath() + " doesn't exist, can't migrate");

        try {
            logger.info("Starting migration...");

            YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldFile);
            ConfigurationSection section = oldConfig.getConfigurationSection("data");

            if (section == null) {
                logger.warn("Invalid old config data (data section not found)");
                return;
            }

            int count = 0, errors = 0;
            for (String key : section.getKeys(false)) {
                // Why was this saving thing so shit?

                try {
                    String[] adrSplit = key.split("-");

                    byte[] adrBytes = new byte[4];
                    for (int i = 0; i < 4; i++) {
                        adrBytes[i] = (byte) Integer.parseInt(adrSplit[i]);
                    }

                    List<String> uniqueIds = section.getStringList(key);
                    for (String uniqueId : uniqueIds) {
                        UUID uuid = UUID.fromString(uniqueId);
                        dataMap.put(uuid, InetAddress.getByAddress(adrBytes));
                    }

                    count++;
                } catch (Throwable throwable) {
                    errors++;
                    logger.warn("Migration error for key {}", key, throwable);
                }
            }

            logger.info("Migrated {} entries, {} threw errors", count, errors);
        } catch (Throwable throwable) {
            logger.warn("Migration of old data failed", throwable);
        }

        try {
            Path oldFilePath = oldFile.toPath();
            Files.move(oldFilePath, oldFilePath.resolveSibling("old_config.yml"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
