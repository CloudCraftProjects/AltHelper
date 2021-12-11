package tk.booky.althelper.listener;
// Created by booky10 in AltHelper (18:04 21.02.21)

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import tk.booky.althelper.AltHelperMain;
import tk.booky.althelper.utils.AltHelperUtils;

public class AddressSaveListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(AltHelperMain.main, () -> {
            String[] split = event.getAddress().toString().split("/");
            AltHelperUtils.saveAddress(split[split.length - 1], event.getPlayer().getUniqueId());
        });
    }
}