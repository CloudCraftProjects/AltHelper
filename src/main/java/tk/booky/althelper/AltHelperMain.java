package tk.booky.althelper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tk.booky.althelper.commands.AltHelperCommand;
import tk.booky.althelper.listener.AddressSaveListener;

import java.util.Objects;

public final class AltHelperMain extends JavaPlugin {

    public static AltHelperMain main;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        main = this;

        Bukkit.getPluginManager().registerEvents(new AddressSaveListener(), this);
        Objects.requireNonNull(getCommand("althelper")).setExecutor(new AltHelperCommand());
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveConfig, 20 * 15, 20 * 10);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }
}
