package me._furiouspotato_.ultimatetag.tabcompleters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me._furiouspotato_.ultimatetag.Main;

public class TagCompleter implements TabCompleter {
	Main plugin;

	public TagCompleter(Main plugin) {
		this.plugin = plugin;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();

		if (sender instanceof Player) {
			List<String> all = new ArrayList<>();
			List<String> operator = new ArrayList<>();

			if (args.length == 1) {
				all = Arrays.asList("join", "leave", "list");
				operator = Arrays.asList("setup", "start", "end", "reset");
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("leave")) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray((Object[]) players);
					for (Player player : players) {
						operator.add(player.getDisplayName());
					}
					operator.add("*");
				}
			}

			List<String> arguments = new ArrayList<>();
			for (String argument : all) {
				arguments.add(argument);
			}
			if (((Player) sender).hasPermission("tag.operator")) {
				for (String argument : operator) {
					arguments.add(argument);
				}
			}
			for (String argument : arguments) {
				if (argument.toLowerCase().indexOf(args[args.length - 1].toLowerCase()) == 0) {
					res.add(argument);
				}
			}
			Collections.sort(res);
			return res;
		}
		return new ArrayList<>();
	}
}
