package dev.booky.alts;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import dev.booky.alts.commands.AltHelperCommand;
import dev.booky.alts.listener.LoginListener;
import dev.booky.alts.util.FastUniqueIdSansHyphens;
import dev.booky.alts.util.IpHexUtil;
import dev.booky.alts.util.MigrationUtil;
import dev.booky.alts.util.WildcardMatcher;
import dev.booky.alts.util.WrappedYamlConfig;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AltHelperMain extends JavaPlugin {

    private final SetMultimap<UUID, InetAddress> data = HashMultimap.create();
    private WrappedYamlConfig dataConfig;

    @Override
    public void onLoad() {
        new Metrics(this, 22525);
    }

    @Override
    public void onEnable() {
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists() && getDataFolder().exists()) {
            File configFile = new File(getDataFolder(), "config.yml");
            if (configFile.exists()) MigrationUtil.migrate(getSLF4JLogger(), configFile, data);
        }

        getLogger().info("Loading data...");
        int count = reloadData(dataFile);
        getSLF4JLogger().info("Loaded {} entries", count);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveData, 20 * 60 * 5, 20 * 60 * 5);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);
        Objects.requireNonNull(getCommand("althelper")).setExecutor(new AltHelperCommand(this));
    }

    @Override
    public void onDisable() {
        getSLF4JLogger().info("Saving {} entries...", data.size());
        saveData();
        getLogger().info("Done saving");
    }

    public int reloadData(File dataFile) {
        if (dataConfig == null) {
            dataConfig = new WrappedYamlConfig(dataFile);
        }
        dataConfig.lock().lock();

        try {
            dataConfig.reload();

            int count = 0;
            for (String key : dataConfig.config().getKeys(false)) {
                UUID uuid = FastUniqueIdSansHyphens.parseUuid(key);
                for (String hexedAddress : dataConfig.config().getStringList(key)) {
                    data.put(uuid, IpHexUtil.fromHex(hexedAddress));
                    count++;
                }
            }
            return count;
        } finally {
            dataConfig.lock().unlock();
        }
    }

    public void saveData() {
        if (!dataConfig.lock().tryLock()) return;

        try {
            dataConfig.clearConfig();
            for (UUID uuid : data.keySet()) {
                String key = FastUniqueIdSansHyphens.toString(uuid);
                dataConfig.config().set(key, data.get(uuid).stream().map(IpHexUtil::toHex).toList());
            }
            dataConfig.save();
        } finally {
            dataConfig.lock().unlock();
        }
    }

    public Collection<InetAddress> addresses(UUID uniqueId) {
        return Collections.unmodifiableCollection(data.get(uniqueId));
    }

    public Collection<UUID> uniqueIds(InetAddress address) {
        Set<UUID> uniqueIds = new HashSet<>();
        for (Map.Entry<UUID, InetAddress> entry : data.entries()) {
            if (!entry.getValue().equals(address)) continue;
            uniqueIds.add(entry.getKey());
        }
        return Collections.unmodifiableSet(uniqueIds);
    }

    public Collection<UUID> uniqueIds(String expression) {
        Set<UUID> uniqueIds = new HashSet<>();
        for (Map.Entry<UUID, InetAddress> entry : data.entries()) {
            if (!WildcardMatcher.matches(entry.getValue().getHostAddress(), expression)) continue;
            uniqueIds.add(entry.getKey());
        }
        return Collections.unmodifiableSet(uniqueIds);
    }

    public SetMultimap<UUID, InetAddress> data() {
        return data;
    }
}
