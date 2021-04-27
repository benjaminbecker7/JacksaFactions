package com.bmbecker.plugin;

import com.bmbecker.plugin.commands.FactionCommands;
import com.bmbecker.plugin.events.ChatListener;
import com.bmbecker.plugin.events.ChunkListener;
import com.bmbecker.plugin.events.PlayerHitListener;
import com.bmbecker.plugin.events.PlayerJoinListener;
import com.bmbecker.plugin.utilities.Cache;
import com.bmbecker.plugin.utilities.SQLUtilities;
import com.bmbecker.plugin.utilities.ScoreboardUtilities;
import com.bmbecker.plugin.utilities.WorldGuardUtilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class JacksaFactions extends JavaPlugin {

    private BukkitRunnable update;

    @Override
    public void onLoad() {
        WorldGuardUtilities.createFlags(); // have to create FACTION_PVP flag before WorldGuard is enabled
    }

    // Enable method
    @Override
    public void onEnable() {
        saveDefaultConfig();

        SQLUtilities.plugin = this;

        Cache.load(SQLUtilities.getPlayerFactions(), SQLUtilities.getInvites(), SQLUtilities.getPerms(), SQLUtilities.getClaimedChunks());
        ScoreboardUtilities.init(SQLUtilities.getFactions());

        getCommand("faction").setExecutor(new FactionCommands(this));
        getCommand("f").setExecutor(new FactionCommands(this));
        getCommand("fadmin").setExecutor(new FactionCommands(this));


        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerHitListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        update = new BukkitRunnable() {
            public void run() {
                updateCache();
                getServer().getConsoleSender().sendMessage("[JacksaFactions]: Updated Cache");
            }
        };

        update.runTaskTimerAsynchronously(this, 12000, 12000);

        getServer().getConsoleSender().sendMessage("[JacksaFactions]: Plugin enabled");
    }

    // Disable method
    @Override
    public void onDisable() {
        update.cancel();
        getServer().getConsoleSender().sendMessage("[JacksaFactions]: Plugin disabled");
    }

    private void updateCache() {
        Cache.load(SQLUtilities.getPlayerFactions(), SQLUtilities.getInvites(), SQLUtilities.getPerms(), SQLUtilities.getClaimedChunks());
    }
}