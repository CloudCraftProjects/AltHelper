package dev.booky.alts.listener;
// Created by booky10 in AltHelper (18:04 21.02.21)

import dev.booky.alts.AltHelperMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.InetAddress;
import java.util.UUID;

public record LoginListener(AltHelperMain main) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        InetAddress address = event.getAddress();
        main.data().put(uuid, address);
    }
}
