package me._furiouspotato_.ultimatetag;

import org.bukkit.entity.Player;

public class TagPlayer {
	Main plugin;

	public Player player;
	boolean isHunter;
	public int score;
	int newScore;
	boolean wasCaught;

	TagPlayer(Player player, Main plugin) {
		this.plugin = plugin;
		this.player = player;
		isHunter = false;
		score = 0;
		newScore = 0;
		wasCaught = false;
	}
}
