package com.bmbecker.plugin.events;

import com.bmbecker.plugin.utilities.Cache;
import com.bmbecker.plugin.utilities.ScoreboardUtilities;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.bmbecker.plugin.utilities.SQLUtilities;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private JavaPlugin plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player joiner = e.getPlayer();

        UUID joinerUUID = joiner.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                if (!Cache.contains(joinerUUID)) {
                    SQLUtilities.createPlayer(joiner);
                    Cache.putFaction(joinerUUID, SQLUtilities.getPlayerFaction(joiner));
                    Cache.putInvite(joinerUUID, SQLUtilities.getPlayerInvite(joiner));
                    // TODO: ADD PLAYER PERMSET INITIALIZATION HERE
                }

                String invite = Cache.getInvite(joinerUUID);

                if (invite != null) {

                    TextComponent message1 = new TextComponent(ChatColor.BOLD + "" + ChatColor.GREEN + "You have been invited to join the faction " + invite + ". You may only have one pending invite at a time.");
                    TextComponent message2 = new TextComponent(ChatColor.GREEN + "To accept this invitation, click this message or type '/factions accept' or '/f accept'");
                    message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept"));
                    message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept invite!")));
                    TextComponent message3 = new TextComponent(ChatColor.GREEN + "To decline this invitation, click this message or type '/factions decline' or '/f decline'");
                    message3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f decline"));
                    message3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to decline invite!")));

                    joiner.spigot().sendMessage(message1);
                    joiner.spigot().sendMessage(message2);
                    joiner.spigot().sendMessage(message3);
                }

                String currFaction = Cache.getFaction(joinerUUID);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {
                        joiner.setScoreboard(ScoreboardUtilities.board);

                        if (currFaction != null) {
                            Team t = ScoreboardUtilities.board.getTeam(currFaction);

                            if (t != null) {
                                t.addEntry(joiner.getName());
                            }
                        }
                    }
                });

            }
        });

    }
}