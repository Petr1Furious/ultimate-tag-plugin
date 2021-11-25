package me._furiouspotato_.ultimatetag.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me._furiouspotato_.ultimatetag.Main;
import me._furiouspotato_.ultimatetag.TagPlayer;
import net.md_5.bungee.api.ChatColor;

public class TagCommand implements CommandExecutor {
	private Main plugin;

	public TagCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (args.length == 0) {
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(ChatColor.BLUE + "/tag setup [number of hunters] [border size] [roundLength]"
							+ ChatColor.GOLD + " - setup the game with given parameters.");
				}
				player.sendMessage(ChatColor.BLUE + "/tag join" + ChatColor.GOLD + " - join the game.");
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(
							ChatColor.BLUE + "/tag join <player name>" + ChatColor.GOLD + " - force player to join.");
				}
				player.sendMessage(ChatColor.BLUE + "/tag leave" + ChatColor.GOLD + " - leave the game.");
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(
							ChatColor.BLUE + "/tag leave <player name>" + ChatColor.GOLD + " - force player to leave.");
				}
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(ChatColor.BLUE + "/tag start" + ChatColor.GOLD + " - start the game.");
				}
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(ChatColor.BLUE + "/tag end" + ChatColor.GOLD + " - end current game.");
				}
				player.sendMessage(ChatColor.BLUE + "/tag list" + ChatColor.GOLD + " - show players in game.");
				if (player.hasPermission("tag.operator")) {
					player.sendMessage(
							ChatColor.BLUE + "/tag reset" + ChatColor.GOLD + " - reset scores in the current game.");
				}

				return false;
			}

			if (args[0].equalsIgnoreCase("setup")) {
				if (player.hasPermission("tag.operator")) {
					int huntersCnt = plugin.defaultHuntersNumber, borderSize = plugin.defaultBorderSize,
							roundLength = plugin.defaultRoundLength,
							roundLengthPerPlayer = plugin.defaultRoundLengthPerPlayer;
					if (args.length >= 2) {
						huntersCnt = Integer.valueOf(args[1]);
					}
					if (args.length >= 3) {
						borderSize = Integer.valueOf(args[2]);
					}
					if (args.length >= 4) {
						roundLength = Integer.valueOf(args[3]);
					}
					if (args.length >= 5) {
						roundLengthPerPlayer = Integer.valueOf(args[4]);
					}
					plugin.setupGame((Player) sender, huntersCnt, borderSize, roundLength, roundLengthPerPlayer);

					return false;
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
					return true;
				}
			}

			if (args[0].equalsIgnoreCase("join")) {
				if (args.length == 1) {
					if (plugin.gameStatus == 2) {
						plugin.addPlayer(player);

						return false;
					} else {
						if (plugin.gameStatus == -1) {
							sender.sendMessage(ChatColor.RED + "The game hasn't started yet.");
						} else {
							sender.sendMessage(ChatColor.RED + "The game is already running.");
						}

						return true;
					}
				}
				if (args.length == 2) {
					if (player.hasPermission("tag.operator")) {
						if (plugin.gameStatus == 2) {
							boolean found = false;
							for (Player player2 : (List<Player>) Bukkit.getOnlinePlayers()) {
								if (player2.getName().equalsIgnoreCase(args[1]) || args[1].equalsIgnoreCase("*")) {
									plugin.addPlayer(player2);
									found = true;
								}
							}
							if (!found) {
								sender.sendMessage(ChatColor.RED + "The player " + args[1] + " is not found!");
							}

							return false;
						} else {
							if (plugin.gameStatus == -1) {
								sender.sendMessage(ChatColor.RED + "The game hasn't started yet.");
							} else {
								sender.sendMessage(ChatColor.RED + "The game is already running.");
							}

							return true;
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("leave")) {
				if (args.length == 1) {
					if (plugin.gameStatus == 2) {
						plugin.removePlayer(player);

						return false;
					} else {
						if (plugin.gameStatus == -1) {
							sender.sendMessage(ChatColor.RED + "The game hasn't started yet.");
						} else {
							sender.sendMessage(ChatColor.RED + "The game is already running.");
						}

						return true;
					}
				}
				if (args.length == 2) {
					if (player.hasPermission("tag.operator")) {
						if (plugin.gameStatus == 2) {
							boolean found = false;
							for (Player player2 : (List<Player>) Bukkit.getOnlinePlayers()) {
								if (player2.getName().equalsIgnoreCase(args[1]) || args[1].equalsIgnoreCase("*")) {
									plugin.removePlayer(player2);
									found = true;
								}
							}
							if (!found) {
								sender.sendMessage(ChatColor.RED + "The player " + args[1] + " is not found!");
							}

							return false;
						} else {
							if (plugin.gameStatus == -1) {
								sender.sendMessage(ChatColor.RED + "The game hasn't started yet.");
							} else {
								sender.sendMessage(ChatColor.RED + "The game is already running.");
							}

							return true;
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("start")) {
				if (args.length == 1) {
					if (player.hasPermission("tag.operator")) {
						if (plugin.gameStatus == 2) {
							plugin.startGame();
						} else {
							if (plugin.gameStatus == -1) {
								sender.sendMessage(ChatColor.RED + "First setup the game.");
							} else {
								sender.sendMessage(ChatColor.RED + "First finish current game.");
							}

							return false;
						}

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("end")) {
				if (args.length == 1) {
					if (player.hasPermission("tag.operator")) {
						if (plugin.gameStatus != -1) {
							plugin.endGame(false);
							for (Player player2 : (List<Player>) Bukkit.getOnlinePlayers()) {
								player2.sendMessage(ChatColor.GOLD + "Current game has been ended.");
							}
						} else {
							player.sendMessage(ChatColor.RED + "The game is not running!");
						}

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("list")) {
				if (args.length == 1) {
					if (plugin.players.isEmpty()) {
						sender.sendMessage(ChatColor.GOLD + "There are no players in game.");
					} else {
						sender.sendMessage(ChatColor.GOLD + "Players in game:");
						String res = new String();
						for (Map.Entry<String, TagPlayer> entry : plugin.players.entrySet()) {
							res += entry.getKey() + ", ";
						}
						res = res.substring(0, (int) res.length() - 2);
						sender.sendMessage(ChatColor.GOLD + res);
					}

					return false;
				}
			}

			if (args[0].equalsIgnoreCase("reset")) {
				if (args.length == 1) {
					if (player.hasPermission("tag.operator")) {
						if (plugin.gameStatus != -1) {
							plugin.resetScores();
							player.sendMessage(ChatColor.GOLD + "Successfully reset scores of current game.");
						} else {
							player.sendMessage(ChatColor.RED + "The game is not running!");
						}

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			sender.sendMessage(ChatColor.DARK_RED + "Wrong command syntax!");
			return true;
		}

		return false;
	}
}
