package dev.booky.alts.listener;
// Created by booky10 in AltHelper (18:04 21.02.21)

import dev.booky.alts.AltHelperMain;
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.net.InetSocketAddress;
import java.util.UUID;

public class LoginListener implements Listener {

    private final AltHelperMain plugin;

    public LoginListener(AltHelperMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerConnectionInitialConfigureEvent event) {
        UUID playerId = event.getConnection().getProfile().getId();
        InetSocketAddress address = event.getConnection().getClientAddress();
        this.plugin.data().put(playerId, address.getAddress());
    }
}
