package com.bmbecker.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PlayerJoin implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		createScoreboard(e.getPlayer());
		updateScoreboard();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		updateScoreboard();
	}
	
	public void createScoreboard(Player player) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective objective = board.registerNewObjective("Party:", "dummy", "Stats"); // name, criteria, displayname
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		Score score = objective.getScore("players:");
		score.setScore(Bukkit.getOnlinePlayers().size());
		player.setScoreboard(board);
		
	}
	
	public void updateScoreboard() {
		for(Player online : Bukkit.getOnlinePlayers()) {
			Score score = online.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore("players:");
			score.setScore(Bukkit.getOnlinePlayers().size());
		}
	}
	
	
	
	
	
	
	
	
	
}
