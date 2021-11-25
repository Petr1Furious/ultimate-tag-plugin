package me._furiouspotato_.ultimatetag;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import me._furiouspotato_.ultimatetag.commands.TagCommand;
import me._furiouspotato_.ultimatetag.tabcompleters.TagCompleter;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {
	public boolean showY;
	public int defaultHuntersNumber;
	public int defaultBorderSize;
	public int defaultRoundLength;
	public int defaultRoundLengthPerPlayer;
	public int allowBreakingDown;
	public int allowPlacingDown;
	public int allowBreakingUp;
	public int allowPlacingUp;

	public int gameStatus;
	public HashMap<String, TagPlayer> players;
	int roundNumber;

	int x, z;
	int ymin, ymax;
	int borderSize;
	int roundLength;
	int roundLengthPerPlayer;

	World world;

	ItemStack[] hunterInv, runnerInv;

	ItemStack[] possibleItems;

	Biome[] forbiddenBiomes;

	int huntersCnt;
	String[] nicknames;
	List<Integer[]> huntersSets;
	Integer[] huntersOrder;

	@Override
	public void onEnable() {
		updateConfiguration();

		getServer().getPluginManager().registerEvents(this, (Plugin) this);
		getCommand("tag").setExecutor(new TagCommand(this));
		getCommand("tag").setTabCompleter(new TagCompleter(this));

		gameStatus = -1;
		players = new HashMap<>();
		roundNumber = -1;
		x = 0;
		z = 0;
		ymin = 0;
		ymax = 0;

		huntersCnt = 1;
		nicknames = new String[0];
		huntersSets = new ArrayList<Integer[]>();
		huntersOrder = new Integer[0];

		possibleItems = new ItemStack[] { new ItemStack(Material.WOODEN_PICKAXE, 1),
				new ItemStack(Material.WOODEN_AXE, 1), new ItemStack(Material.STONE_SHOVEL, 1),
				new ItemStack(Material.COBBLESTONE, 16) };
		for (int i = 0; i < 3; i++)
			possibleItems[i].addUnsafeEnchantment(Enchantment.DURABILITY, 3);

		runnerInv = new ItemStack[41];
		runnerInv[2] = new ItemStack(Material.COOKED_BEEF, 16);

		hunterInv = new ItemStack[41];
		hunterInv[0] = new ItemStack(Material.DIAMOND_AXE, 1);
		hunterInv[1] = new ItemStack(Material.DIAMOND_PICKAXE, 1);
		hunterInv[2] = new ItemStack(Material.DIAMOND_SHOVEL, 1);
		hunterInv[3] = new ItemStack(Material.COBBLESTONE, 64);
		hunterInv[4] = new ItemStack(Material.COOKED_BEEF, 16);
		hunterInv[8] = new ItemStack(Material.COMPASS, 1);

		forbiddenBiomes = new Biome[] { Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN,
				Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_OCEAN, Biome.DEEP_WARM_OCEAN, Biome.FROZEN_OCEAN,
				Biome.LUKEWARM_OCEAN, Biome.OCEAN, Biome.WARM_OCEAN, Biome.SWAMP, Biome.SWAMP_HILLS };
	}

	@Override
	public void onDisable() {

	}

	public void updateConfiguration() {
		if (getConfig().getKeys(false).isEmpty()) {
			getConfig().set("showY", false);
			getConfig().set("defaultHuntersNumber", 1);
			getConfig().set("defaultBorderSize", 100);
			getConfig().set("defaultRoundLength", 60);
			getConfig().set("defaultRoundLengthPerPlayer", 15);
			getConfig().set("allowBreakingDown", 20);
			getConfig().set("allowPlacingDown", 20);
			getConfig().set("allowBreakingUp", 20);
			getConfig().set("allowPlacingUp", 20);
			saveConfig();
		}
		showY = getConfig().getBoolean("showY");
		defaultHuntersNumber = getConfig().getInt("defaultHuntersNumber");
		defaultBorderSize = getConfig().getInt("defaultBorderSize");
		defaultRoundLength = getConfig().getInt("defaultRoundLength");
		defaultRoundLengthPerPlayer = getConfig().getInt("defaultRoundLengthPerPlayer");
		allowBreakingDown = getConfig().getInt("allowBreakingDown");
		allowPlacingDown = getConfig().getInt("allowPlacingDown");
		allowBreakingUp = getConfig().getInt("allowBreakingUp");
		allowPlacingUp = getConfig().getInt("allowPlacingUp");
	}

	public void setupGame(Player sender, int huntersCnt, int borderSize, int roundLength, int roundLengthPerPlayer) {
		if (sender != null) {
			world = sender.getWorld();
			players.clear();
		}
		gameStatus = 2;

		updateScoreboard();

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "New game of the Ultimate Tag is going to start soon!");
			player.sendMessage(ChatColor.GOLD + "Type " + ChatColor.BLUE + ChatColor.BOLD + "/tag join"
					+ ChatColor.RESET + ChatColor.GOLD + " to join.");
			player.sendMessage(ChatColor.GOLD + "Hunters count: " + ChatColor.BLUE + String.valueOf(huntersCnt)
					+ ChatColor.GOLD + ", border size: " + ChatColor.BLUE + String.valueOf(borderSize) + ChatColor.GOLD
					+ "x" + ChatColor.BLUE + String.valueOf(borderSize) + ChatColor.GOLD + ", round time: "
					+ ChatColor.BLUE + roundLength + ChatColor.GOLD + " s + " + ChatColor.BLUE + roundLengthPerPlayer
					+ ChatColor.GOLD + " s per player.");
		}

		this.huntersCnt = huntersCnt;
		this.borderSize = borderSize;
		this.roundLength = roundLength;
		this.roundLengthPerPlayer = roundLengthPerPlayer;

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				updateScoreboard();
			}
		}, 0L, 20L);
	}

	public int getRandCoord() {
		return (int) ((Math.random() - 0.5) * 100000);
	}

	public void getCoords() {
		boolean good = true;
		do {
			good = true;

			x = getRandCoord();
			z = getRandCoord();

			int[] X = new int[] { x - borderSize / 2 + 3, x + borderSize / 2 - 3 };
			int[] Z = new int[] { z - borderSize / 2 + 3, z + borderSize / 2 - 3 };
			for (int i = 0; i < 2; i++) {
				for (Biome biome : forbiddenBiomes) {
					Block temp = world.getHighestBlockAt(X[i], Z[i]);
					if (temp.getBiome() == biome) {
						good = false;
						break;
					}
				}
				if (!good)
					break;
			}
		} while (!good);
	}

	public void endRound() {
		gameStatus = 1;
		Bukkit.getScheduler().cancelTasks(this);

		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			if (!tplayer.isHunter) {
				if (!tplayer.wasCaught) {
					tplayer.newScore += huntersCnt;
				} else {
					for (Map.Entry<String, TagPlayer> entry2 : players.entrySet()) {
						TagPlayer tplayer2 = entry2.getValue();
						if (tplayer2.isHunter) {
							tplayer2.newScore++;
						}
					}
				}
			}
		}

		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			tplayer.score += tplayer.newScore;
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "The round of the Ultimate Tag has finished!");
			player.sendMessage(ChatColor.GOLD + "Current scores:");
			for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
				TagPlayer tplayer = entry.getValue();
				String curColor = "" + ChatColor.AQUA;
				if (tplayer.isHunter) {
					curColor = "" + ChatColor.RED;
				}
				player.sendMessage(curColor + tplayer.player.getName() + ChatColor.GOLD + " " + tplayer.score + " ("
						+ "+" + tplayer.newScore + ")");
			}
		}
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			tplayer.newScore = 0;
		}

		updateScoreboard();

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			int time = 5;
			boolean started = false, sentMessage = false;

			@Override
			public void run() {
				if (started)
					return;
				if (this.time == 0) {
					boolean allOnline = true;
					for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
						if (Bukkit.getPlayerExact(entry.getKey()) == null) {
							allOnline = false;
							break;
						}
					}

					if (allOnline) {
						started = true;
						newRound();
						return;
					}

					if (!sentMessage) {
						for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
							player.sendMessage(ChatColor.GOLD + "Waiting for all Ultimate Tag players to return.");
						}
						sentMessage = true;
					}
					this.time = 1;
				}
				this.time--;
			}
		}, 0L, 20L);
	}

	public void addPlayer(Player target) {
		if (gameStatus != 2) {
			return;
		}

		huntersSets.clear();
		players.put(target.getName(), new TagPlayer(target, this));

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + target.getName() + ChatColor.GOLD
					+ " has joined the game.");
		}

		updateScoreboard();
	}

	public void removePlayer(Player target) {
		if (gameStatus != 2) {
			return;
		}

		huntersSets.clear();
		players.remove(target.getName());

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "Player " + ChatColor.BLUE + target.getName() + ChatColor.GOLD
					+ " has left the game.");
		}

		updateScoreboard();
	}

	public void newRound() {
		gameStatus = 0;
		roundNumber++;

		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			tplayer.isHunter = false;
		}

		if (roundNumber == huntersSets.size()) {
			endGame(true);
			return;
		}

		getCoords();
		world.getWorldBorder().setCenter(x, z);
		world.getWorldBorder().setSize(borderSize);

		for (Integer hunter : huntersSets.get(huntersOrder[roundNumber])) {
			players.get(nicknames[hunter]).isHunter = true;
		}

		for (int i = 0; i < possibleItems.length; i++) {
			Random random = new Random();
			int j = random.nextInt(possibleItems.length);
			ItemStack temp = possibleItems[i].clone();
			possibleItems[i] = possibleItems[j].clone();
			possibleItems[j] = temp.clone();
		}
		for (int i = 0; i < 2; i++) {
			runnerInv[i] = possibleItems[i];
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			if (players.containsKey(player.getName())) {
				TagPlayer tplayer = players.get(player.getName());
				tplayer.score += tplayer.newScore;
				tplayer.newScore = 0;
				tplayer.wasCaught = false;

				tplayer.player.spigot().respawn();
				tplayer.player.setHealth(20.0);
				tplayer.player.setFoodLevel(20);
				tplayer.player.setSaturation(8);
				tplayer.player.setGameMode(GameMode.SURVIVAL);
				tplayer.player.setFallDistance(0);
				tplayer.player.setVelocity(new Vector(0, 0, 0));
				for (PotionEffect effect : tplayer.player.getActivePotionEffects())
					tplayer.player.removePotionEffect(effect.getType());
				tplayer.player.setRemainingAir(tplayer.player.getMaximumAir());

				if (tplayer.isHunter) {
					tplayer.player.teleport(world.getHighestBlockAt(x - borderSize / 2 + 3, z - borderSize / 2 + 3)
							.getLocation().add(0.5, 2, 0.5));
					tplayer.player.getInventory().setContents(hunterInv);
				} else {
					tplayer.player.teleport(world.getHighestBlockAt(x + borderSize / 2 - 3, z + borderSize / 2 - 3)
							.getLocation().add(0.5, 2, 0.5));
					tplayer.player.getInventory().setContents(runnerInv);
				}
			} else {
				player.teleport(world.getHighestBlockAt(x, z).getLocation().add(0, 20, 0));
				player.setGameMode(GameMode.SPECTATOR);
			}

			player.sendMessage(ChatColor.GOLD + "Round " + ChatColor.BLUE + String.valueOf(roundNumber + 1) + "/"
					+ String.valueOf(huntersSets.size()) + ChatColor.GOLD + " of the Ultimate Tag has started!");
			String message = new String();
			message = ChatColor.RED + "";
			for (Integer hunter : huntersSets.get(huntersOrder[roundNumber])) {
				message += nicknames[hunter] + ", ";
			}
			message = message.substring(0, message.length() - 2);
			message = message + ChatColor.GOLD;
			if (huntersCnt == 1) {
				player.sendMessage(message + " is the hunter!");
			} else {
				player.sendMessage(message + " are the hunters!");
			}
		}
		ymin = world.getHighestBlockAt(x - borderSize / 2 + 3, z - borderSize / 2 + 3).getLocation().add(0, 2, 0)
				.getBlockY();
		ymax = world.getHighestBlockAt(x + borderSize / 2 - 3, z + borderSize / 2 - 3).getLocation().add(0, 2, 0)
				.getBlockY();
		if (ymin > ymax) {
			int t = ymin;
			ymin = ymax;
			ymax = t;
		}

		updateScoreboard();

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			int time = roundLength + roundLengthPerPlayer * (int) players.size();
			int startTime = time;

			@Override
			public void run() {
				if (this.time == 0) {
					endRound();
					return;
				}

				if (this.time == this.startTime || this.time % 30 == 0 || this.time <= 10) {
					for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
						if (this.time > 1) {
							player.sendMessage(ChatColor.GOLD + "" + this.time + " seconds remain!");
						} else {
							player.sendMessage(ChatColor.GOLD + "" + "1 second remains!");
						}
					}
				}

				this.time--;
			}
		}, 0L, 20L);
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
					TagPlayer tplayer = entry.getValue();
					if (tplayer.isHunter) {
						updateCompass(tplayer.player, false);
					}
				}

				updateScoreboard();
			}
		}, 0L, 5L);
	}

	public void startGame() {
		roundNumber = -1;

		nicknames = new String[players.size()];
		int counter = 0;
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			nicknames[counter++] = entry.getKey();
		}

		if (huntersSets.isEmpty()) {
			huntersSets.clear();
			createHuntersSets(null, 0, 0);
			if (huntersSets.size() > 100) {
				for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
					player.sendMessage(
							ChatColor.DARK_RED + "Failed to start the game! Too many combinations of hunters!");
				}
				return;
			}

			huntersOrder = new Integer[huntersSets.size()];
			for (int i = 0; i < huntersOrder.length; i++) {
				huntersOrder[i] = i;
			}
			for (int i = 0; i < huntersOrder.length; i++) {
				Random random = new Random();
				int j = random.nextInt(huntersOrder.length);
				Integer temp = huntersOrder[i];
				huntersOrder[i] = huntersOrder[j];
				huntersOrder[j] = temp;
			}
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.DARK_GREEN + "New game of the Ultimate Tag is starting now!");
		}

		newRound();
	}

	public void updateScoreboard() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("test", "dummy", "Scoreboard");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();

			String name = new String();
			if (!tplayer.isHunter) {
				name = ChatColor.AQUA + "";
			} else {
				name = ChatColor.RED + "";
			}
			name += tplayer.player.getName();

			Score score = objective.getScore(name);
			score.setScore(tplayer.score);
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.setScoreboard(scoreboard);
		}
	}

	public void clearScoreboard() {
		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}

	public void resetScores() {
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			tplayer.score = 0;
		}

		updateScoreboard();
	}

	public void endGame(boolean startNewGame) {
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();

			tplayer.isHunter = false;
		}

		Bukkit.getScheduler().cancelTasks(this);
		gameStatus = -1;

		if (!players.isEmpty()) {
			Map.Entry<String, TagPlayer>[] scoreboard = (Map.Entry<String, TagPlayer>[]) new Map.Entry[players
					.entrySet().size()];
			players.entrySet().toArray(scoreboard);
			Arrays.sort(scoreboard, new PlayerComparator());

			for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
				Integer i = 0, old = -1, curPlace = 0;

				if (player.getGameMode() == GameMode.SPECTATOR) {
					player.setGameMode(GameMode.SURVIVAL);
					player.setFallDistance(0);
					player.setVelocity(new Vector(0, 0, 0));
					player.teleport(
							player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 2, 0));
				}
				player.sendMessage(ChatColor.GOLD + "The game of Ultimate Tag has finished! Scoreboard:");
				for (Map.Entry<String, TagPlayer> entry : scoreboard) {
					if (i == 0 || entry.getValue().score != old) {
						curPlace = i;
						old = entry.getValue().score;
					}
					player.sendMessage(ChatColor.GOLD + String.valueOf(Integer.valueOf(curPlace + 1)) + ". "
							+ entry.getKey() + " (" + entry.getValue().score + ")");
					i++;
				}
			}
		}

		// players.clear();

		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setSize(60000000);

		updateScoreboard();

		if (startNewGame) {
			setupGame(null, this.huntersCnt, this.borderSize, this.roundLength, this.roundLengthPerPlayer);
		} else {
			players.clear();
			clearScoreboard();
		}
	}

	public boolean checkAllCaught() {
		boolean allHuntersCaught = true, allRunnersCaught = true;
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			if (tplayer.isHunter) {
				if (tplayer.wasCaught == false) {
					allHuntersCaught = false;
				}
			} else {
				if (tplayer.wasCaught == false) {
					allRunnersCaught = false;
				}
			}
		}

		return allHuntersCaught || allRunnersCaught;
	}

	void tag(TagPlayer tplayer) {
		tplayer.wasCaught = true;
		tplayer.player.setGameMode(GameMode.SPECTATOR);

		if (checkAllCaught()) {
			endRound();
		}
	}

	void createHuntersSets(Integer[] l, int cur, int sz) {
		if (huntersSets.size() > 100)
			return;

		if (l == null)
			l = new Integer[huntersCnt];

		if (sz == huntersCnt) {
			huntersSets.add(l.clone());
			return;
		}
		if (cur == players.size())
			return;

		l[sz] = cur;
		createHuntersSets(l, cur + 1, sz + 1);
		createHuntersSets(l, cur + 1, sz);
	}

	public void updateCompass(Player p, boolean sendFeedback) {
		TagPlayer target = null;
		for (Map.Entry<String, TagPlayer> entry : players.entrySet()) {
			TagPlayer tplayer = entry.getValue();
			if (!tplayer.isHunter && !tplayer.wasCaught && p.getWorld() == tplayer.player.getWorld()) {
				if (target == null || p.getLocation().distance(tplayer.player.getLocation()) < p.getLocation()
						.distance(target.player.getLocation())) {
					target = tplayer;
				}
			}
		}
		if (target == null) {
			if (sendFeedback) {
				p.sendMessage(ChatColor.RED + "No runners found.");
			}
		} else {
			p.setCompassTarget(target.player.getLocation());
			if (sendFeedback) {
				String message = ChatColor.RED + "Compass is now pointing at " + target.player.getDisplayName();
				if (showY) {
					message += ", y = " + target.player.getLocation().getBlockY();
				}
				p.sendMessage(message);
			}
		}
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (gameStatus != 0)
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		Player entity = (Player) e.getEntity();
		Player damager = (Player) e.getDamager();
		String entityName = entity.getName();
		String damagerName = damager.getName();
		if (!players.containsKey(entityName) || players.get(entityName).isHunter)
			return;
		if (!players.containsKey(damagerName) || !players.get(damagerName).isHunter)
			return;

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.RED + damagerName + ChatColor.GOLD + " has tagged " + ChatColor.AQUA
					+ entityName + ChatColor.GOLD + "!");
		}

		tag(players.get(entityName));
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		if (gameStatus != 0)
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		Player entity = (Player) e.getEntity();
		String entityName = entity.getName();
		if (!players.containsKey(entityName) || players.get(entityName).wasCaught) {
			entity.setGameMode(GameMode.SPECTATOR);
			entity.teleport(world.getHighestBlockAt(x, z).getLocation().add(0, 20, 0));
			return;
		}

		players.get(entityName).wasCaught = true;
		String curColor = "" + ChatColor.AQUA;
		if (players.get(entityName).isHunter)
			curColor = "" + ChatColor.RED;

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(curColor + entityName + ChatColor.GOLD + " has died!");
		}

		tag(players.get(entityName));
	}

	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (gameStatus != 0)
			return;
		if (!players.containsKey(e.getPlayer().getName()) || !players.get(e.getPlayer().getName()).isHunter)
			return;
		Player p = e.getPlayer();
		if (e.getMaterial() != Material.COMPASS)
			return;
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		updateCompass(p, true);
	}

	@EventHandler
	public void onTestEntityDamage(EntityDamageByEntityEvent e) {
		if (gameStatus != 0)
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		Player entity = (Player) e.getEntity();
		Player damager = (Player) e.getDamager();
		String entityName = entity.getName();
		String damagerName = damager.getName();
		if (!players.containsKey(entityName) || !players.containsKey(damagerName))
			return;
		if (players.get(entityName).isHunter == players.get(damagerName).isHunter) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	private void onBlockPlace(BlockPlaceEvent e) {
		if (gameStatus != 0)
			return;
		if (!players.containsKey(e.getPlayer().getName()))
			return;

		if (ymin - allowPlacingDown > e.getBlock().getY()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Too low!");
		}

		if (ymax + allowPlacingUp < e.getBlock().getY()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Too high!");
		}
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		if (gameStatus != 0)
			return;
		if (!players.containsKey(e.getPlayer().getName()))
			return;

		if (ymin - allowBreakingDown > e.getBlock().getY()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Too low!");
		}
		if (ymax + allowBreakingUp < e.getBlock().getY()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Too high!");
		}
	}

	@EventHandler
	private void onPlayerLogin(PlayerLoginEvent e) {
		if (gameStatus == -1)
			return;
		if (!players.containsKey(e.getPlayer().getName())) {
			e.getPlayer().setGameMode(GameMode.SPECTATOR);
			e.getPlayer().teleport(world.getHighestBlockAt(x, z).getLocation().add(0, 20, 0));
		} else {
			players.get(e.getPlayer().getName()).player = e.getPlayer();
		}

		updateScoreboard();
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if (gameStatus == -1)
			return;
		if (!players.containsKey(e.getPlayer().getName()))
			return;
		Player p = e.getPlayer();

		if (gameStatus == 2) {
			removePlayer(p);
		}

		updateScoreboard();
	}
}
