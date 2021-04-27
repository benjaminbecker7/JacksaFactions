package com.bmbecker.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;

public class ScoreboardUtilities {

    public static Scoreboard board;

    public static void init(ArrayList<String> factions) {
        board = Bukkit.getScoreboardManager().getMainScoreboard();

        if (factions == null) {
            return;
        }

        for (String name : factions) {
            Team existing = board.getTeam(name);
            if (existing == null) {
                Team newTeam = board.registerNewTeam(name);
                newTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', "&c[&6" + name + "&c]&f "));
                newTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                newTeam.setAllowFriendlyFire(true);
            } else {
                existing.setPrefix(ChatColor.translateAlternateColorCodes('&', "&c[&6" + name + "&c]&f "));
                existing.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                existing.setAllowFriendlyFire(true);
            }
        }
    }

    public static void addTeam(String name, Player init) {
        Team newTeam = board.registerNewTeam(name);
        newTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', "&c[&6" + name + "&c]&f "));
        newTeam.addEntry(init.getName());
        newTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        newTeam.setAllowFriendlyFire(true);
    }

}
