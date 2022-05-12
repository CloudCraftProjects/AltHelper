package dev.booky.alts.util;
// Created by booky10 in AltHelper (19:51 11.05.22)

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class WrappedYamlConfig {

    private final ReentrantLock lock = new ReentrantLock();
    private final File file;
    private YamlConfiguration config;

    public WrappedYamlConfig(File file) {
        this.file = file;
    }

    public void clearConfig() {
        config = new YamlConfiguration();
    }

    public synchronized void reload() {
        if (!file.exists()) file.getParentFile().mkdirs();
        config = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public ReentrantLock lock() {
        return lock;
    }

    public YamlConfiguration config() {
        return config;
    }
}
