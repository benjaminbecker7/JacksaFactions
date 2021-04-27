package com.bmbecker.plugin.events;

import com.bmbecker.plugin.utilities.Cache;
import com.bmbecker.plugin.utilities.SQLUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatListener implements Listener {

    private JavaPlugin plugin;

    public ChatListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        String format = e.getFormat();
        Player player = e.getPlayer();
        String factionTag = Cache.getFaction(player.getUniqueId());
        if(factionTag == null) {
            factionTag = "Ohne Faction";
        }
        String factionPrefix = ChatColor.translateAlternateColorCodes('&', "&c[&6" + factionTag + "&c]&f");
        format = factionPrefix + " " + format;
        e.setFormat(format);
    }
}