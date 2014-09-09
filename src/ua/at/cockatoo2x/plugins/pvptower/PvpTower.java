package ua.at.cockatoo2x.plugins.pvptower;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class PvpTower extends JavaPlugin implements Listener{
	
	public static Economy econ = null;
	
	//On Enable
		public void onEnable() {
			Bukkit.getServer().getPluginManager().registerEvents(this, this);
			if (!setupEconomy() ) {
	            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
	            getServer().getPluginManager().disablePlugin(this);
	            return;
	        }
			getConfig().options().copyDefaults(true);
			saveConfig();
			getLogger().info("PvP Tower Now Active!");
		}

	//On Disable
		public void onDisable() {
			getLogger().info("PvP Tower Now Disabled!");
			saveConfig();
		}
		
	//Setup Economy
		private boolean setupEconomy() {
	        if (getServer().getPluginManager().getPlugin("Vault") == null) {
	            return false;
	        }
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
	            return false;
	        }
	        econ = rsp.getProvider();
	        return econ != null;
	    }
		
		
	//Commands
		@SuppressWarnings("deprecation")
		public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
			Player player = (Player) sender;
			if(commandLabel.equalsIgnoreCase("pvptcreatetower")){
				if(player.hasPermission("pvpt.setup.createtower")){
					if(args.length == 1){
						getConfig().createSection("towers." + args[0]);
						player.sendMessage(ChatColor.GREEN + "[PvP Tower] Created the Tower: " + args[0]);
						saveConfig();
					}else{
						player.sendMessage(ChatColor.RED + "[PvP Tower] OOPS! Need one argument! Try: /pvptcreatetower <tower name>");
					}
				}else{
					player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry but you need the permission: pvpt.setup.createtower to use this command!");
				}
			}else if(commandLabel.equalsIgnoreCase("pvptsettowerspawn")){
				if(player.hasPermission("pvpt.setup.settowerspawn")){
					if(args.length == 1){
						if(getConfig().contains("towers." + args[0])){
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".X", player.getLocation().getBlockX());
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".Y", player.getLocation().getBlockY());
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".Z", player.getLocation().getBlockZ());
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".Yaw", player.getLocation().getYaw());
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".Pitch", player.getLocation().getPitch());
							this.getConfig().set("towers." + args[0] + ".towerspawn" + ".World", player.getLocation().getWorld().getName());
							player.sendMessage(ChatColor.GREEN + "[PvP Tower] Set Tower Spawn for Tower: " + args[0]);
							saveConfig();
						}else{
							player.sendMessage(ChatColor.RED + "[PvP Tower] Cannot set spawn for a non-existant tower! Use /pvptcreatetower <tower name> to create a tower!");
						}
					}else{
						player.sendMessage(ChatColor.RED + "[PvP Tower] OOPS! Need one argument! Try: /pvptsettowerspawn <tower name>");
					}
				}else{
					player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry but you need the permission: pvpt.setup.settowerspawn to use this command!");
				}
			}else if(commandLabel.equalsIgnoreCase("pvptleave")){
				if(this.getConfig().contains("players." + player.getName() + ".towerIN")){
					int exitX = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".X");
					int exitY = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Y");
					int exitZ = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Z");
					int exitYaw = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Yaw");
					int exitPitch = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Pitch");
					World exitWorld = Bukkit.getWorld(this.getConfig().getString("players." + player.getName() + ".exitCoords" + ".World"));
					Location playerExit = new Location(exitWorld, exitX, exitY, exitZ, exitYaw, exitPitch);
					player.teleport(playerExit);
					this.getConfig().set("players." + player.getName() + ".towerIN", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", null);
					this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", null);
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "[PvP Tower] Successfully left the tower!");
					player.getInventory().clear();
					player.updateInventory();
				}else{
					player.sendMessage(ChatColor.RED + "[PvP Tower] You can't leave an tower you are not in!");
				}
			}else if(commandLabel.equalsIgnoreCase("pvptfood")){
				if(this.getConfig().contains("players." + player.getName() + ".towerIN")){
					player.setFoodLevel(20);
					player.setHealth(20.0);
					player.sendMessage(ChatColor.GREEN + "[PvP Tower] Food Healed!");
				}else{
					player.sendMessage(ChatColor.RED + "[PvP Tower] You are not allowed to use this command unless you are in a tower!");
				}
			}
			return false;
		}
		
	//Registers Sign is a Sign
		public boolean issign(Block block){
			if(block.getType()==Material.SIGN || block.getType()==Material.SIGN_POST || block.getType()==Material.WALL_SIGN){
				return true;
			}
			return false;
		}
		
	//Check if player interacts with the sign
		@SuppressWarnings("deprecation")
		@EventHandler
		public void onInteract(PlayerInteractEvent event){
			Player player = event.getPlayer();
			PlayerInventory pi = event.getPlayer().getInventory();
			if(event.getClickedBlock()==null){
				return;
			}
			if(issign(event.getClickedBlock())){
				Sign s =(Sign) event.getClickedBlock().getState();
				if(s.getLine(0).equalsIgnoreCase("newpvptjoin")){
					if(this.getConfig().contains("towers."+s.getLine(1))){
					    if(player.hasPermission("pvpt.setup.newjoinsigns")){
							s.setLine(0, ChatColor.GOLD + "[PvP Tower]");
							s.update();
							player.sendMessage(ChatColor.GREEN + "[PvP Tower] New Join Sign Created! Checking if useable...");
							saveConfig();
							if(getConfig().contains("towers." + s.getLine(1) + ".towerspawn")){
								s.setLine(3, ChatColor.GREEN + "JOIN NOW!");
								player.sendMessage(ChatColor.GREEN + "[PvP Tower] Sign Usable!");
								s.update();
							}else{
								s.setLine(3, ChatColor.RED + "Need Spawn!");
								player.sendMessage(ChatColor.RED + "[PvP Tower] Sign Not Usable!");
								s.update();
							}
					    }else{
					    	player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry you don't have permission to make join signs: pvpt.setup.newjoinsigns");
					    }
					 }else{
						 player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry that tower doesn't exist! Can't make Join Sign!");
					 }
				}else if(s.getLine(0).equals(ChatColor.GOLD + "[PvP Tower]")){
					if(s.getLine(3).equals(ChatColor.RED + "Need Spawn!")){
							if(this.getConfig().contains("towers." + s.getLine(1) + ".towerspawn")){
								s.setLine(3, ChatColor.GREEN + "JOIN NOW!");
								player.sendMessage(ChatColor.GREEN + "[PvP Tower] Sign Now Usable!");
								s.update();
							}else{
								player.sendMessage(ChatColor.RED + "[PvP Tower] Sign not able to be updated!");
							}
					}else if(s.getLine(3).equals(ChatColor.GREEN + "JOIN NOW!")){
						if(this.getConfig().contains("towers." + s.getLine(1) + ".towerspawn")){
							player.setGameMode(GameMode.SURVIVAL);
							player.getInventory().clear();
							this.getConfig().set("players." + player.getName() + ".towerIN", s.getLine(1));
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", player.getLocation().getBlockX());
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", player.getLocation().getBlockY());
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", player.getLocation().getBlockZ());
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", player.getLocation().getYaw());
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", player.getLocation().getPitch());
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", player.getLocation().getWorld().getName());
							int joinX = this.getConfig().getInt("towers." + s.getLine(1) + ".towerspawn" + ".X");
							int joinY = this.getConfig().getInt("towers." + s.getLine(1) + ".towerspawn" + ".Y");
							int joinZ = this.getConfig().getInt("towers." + s.getLine(1) + ".towerspawn" + ".Z");
							int joinYaw = this.getConfig().getInt("towers." + s.getLine(1) + ".towerspawn" + ".Yaw");
							int joinPitch = this.getConfig().getInt("towers." + s.getLine(1) + ".towerspawn" + ".Pitch");
							World joinWorld = Bukkit.getWorld(this.getConfig().getString("towers." + s.getLine(1) + ".towerspawn" + ".World"));
							Location towerSpawn = new Location(joinWorld, joinX, joinY, joinZ, joinYaw, joinPitch);
							player.teleport(towerSpawn);
							player.sendMessage(ChatColor.GREEN + "[PvP Tower] You have joined the tower: " + s.getLine(1));
							saveConfig();
							if(player.hasPermission("pvpt.ingame.donor")){
								if(player.hasPermission("pvpt.ingame.donorplus")){
									if(player.hasPermission("pvpt.ingame.admin")){
										player.sendMessage(ChatColor.GREEN + "[PvP Tower] Thanks for being a ADMIN! Please enjoy these items to use ingame!");
										pi.addItem(new ItemStack(Material.BOW, 1));
										pi.addItem(new ItemStack(Material.ARROW, 32));
										pi.addItem(new ItemStack(Material.IRON_SWORD, 1));
										Potion potion = new Potion(PotionType.FIRE_RESISTANCE);
										pi.addItem(potion.toItemStack(3));
										player.updateInventory();
									}else{
										player.sendMessage(ChatColor.GREEN + "[PvP Tower] Thanks for being a DONOR+! Please enjoy these items to use ingame!");
										pi.addItem(new ItemStack(Material.STONE_SWORD, 1));
										pi.addItem(new ItemStack(Material.BOW, 1));
										pi.addItem(new ItemStack(Material.ARROW, 32));
										Potion potion = new Potion(PotionType.FIRE_RESISTANCE);
										pi.addItem(potion.toItemStack(2));
										player.updateInventory();
									}
								}else{
									player.sendMessage(ChatColor.GREEN + "[PvP Tower] Thanks for being a DONOR! Please enjoy these items to use ingame!");
									pi.addItem(new ItemStack(Material.WOOD_SWORD, 1));
									Potion potion = new Potion(PotionType.FIRE_RESISTANCE);
									pi.addItem(potion.toItemStack(1));
									player.updateInventory();
								}
							}
						}else{
							s.setLine(3, ChatColor.RED + "Need Spawn!");
							s.update();
						}
					}else if(s.getLine(2).equals(ChatColor.LIGHT_PURPLE + "CLICK HERE")){
						if(s.getLine(3).equals(ChatColor.LIGHT_PURPLE + "TO WIN!")){
							EconomyResponse r = econ.depositPlayer(player.getName(), 50.0);
							if(r.transactionSuccess()){
								player.sendMessage(ChatColor.GREEN + "[PvP Tower] You just won $50 for compleating the PvP Tower!!!");
								Bukkit.broadcastMessage(ChatColor.AQUA + "[PvP Tower] " + player.getName() + " has just won the tower: " + s.getLine(1));
								int exitX = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".X");
								int exitY = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Y");
								int exitZ = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Z");
								int exitYaw = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Yaw");
								int exitPitch = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Pitch");
								World exitWorld = Bukkit.getWorld(this.getConfig().getString("players." + player.getName() + ".exitCoords" + ".World"));
								Location playerExit = new Location(exitWorld, exitX, exitY, exitZ, exitYaw, exitPitch);
								player.teleport(playerExit);
								this.getConfig().set("players." + player.getName() + ".towerIN", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", null);
								saveConfig();
								player.getInventory().clear();
								player.updateInventory();
							}else{
								Bukkit.broadcastMessage(ChatColor.GREEN + "[PvP Tower] " + player.getName() + " has just won the tower: " + s.getLine(1));
								player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry an error ocurred and we were unable to award you your money!");
								int exitX = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".X");
								int exitY = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Y");
								int exitZ = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Z");
								int exitYaw = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Yaw");
								int exitPitch = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Pitch");
								World exitWorld = Bukkit.getWorld(this.getConfig().getString("players." + player.getName() + ".exitCoords" + ".World"));
								Location playerExit = new Location(exitWorld, exitX, exitY, exitZ, exitYaw, exitPitch);
								player.teleport(playerExit);
								this.getConfig().set("players." + player.getName() + ".towerIN", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", null);
								this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", null);
								saveConfig();
								player.getInventory().clear();
								player.updateInventory();
							}
						}
					}else if(s.getLine(2).equals(ChatColor.DARK_PURPLE + "Click Me")){
						if(s.getLine(3).equals(ChatColor.DARK_PURPLE + "To Exit!")){
							int exitX = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".X");
							int exitY = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Y");
							int exitZ = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Z");
							int exitYaw = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Yaw");
							int exitPitch = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Pitch");
							World exitWorld = Bukkit.getWorld(this.getConfig().getString("players." + player.getName() + ".exitCoords" + ".World"));
							Location playerExit = new Location(exitWorld, exitX, exitY, exitZ, exitYaw, exitPitch);
							player.teleport(playerExit);
							this.getConfig().set("players." + player.getName() + ".towerIN", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", null);
							this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", null);
							saveConfig();
							player.sendMessage(ChatColor.GREEN + "[PvP Tower] Successfully left the tower!");
							player.getInventory().clear();
							player.updateInventory();
						}
					}else if(s.getLine(2).equals(ChatColor.AQUA + "Heal Your")){
						if(s.getLine(3).equals(ChatColor.AQUA + "Food Here!")){
							player.setFoodLevel(20);
							player.setHealth(20.0);
							player.sendMessage(ChatColor.GREEN + "[PvP Tower] Food Healed!");
						}
					}
				}else if(s.getLine(0).equalsIgnoreCase("newpvptwin")){
					if(this.getConfig().contains("towers."+s.getLine(1))){
						if(this.getConfig().contains("towers." + s.getLine(1) + ".towerspawn")){
							if(player.hasPermission("pvpt.setup.newwinsigns")){
								s.setLine(0, ChatColor.GOLD + "[PvP Tower]");
								s.setLine(2, ChatColor.LIGHT_PURPLE + "CLICK HERE");
								s.setLine(3, ChatColor.LIGHT_PURPLE + "TO WIN!");
								player.sendMessage(ChatColor.GREEN + "[PvP Tower] New Victory Sign set for arena: " + s.getLine(1));
								s.update();
							}else{
								player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry you need this permission to make a victory sign: pvpt.setup.newwinsigns");
							}
						}else{
							player.sendMessage(ChatColor.RED + "[PvP Tower] Can't make the victory sign because the tower still needs a: Spawn Point!");
						}
					}else{
						player.sendMessage(ChatColor.RED + "[PvP Tower] Can't make the victory sign because the tower doesn't exist at all!");
					}
				}else if(s.getLine(0).equalsIgnoreCase("newpvptleave")){
					if(player.hasPermission("pvpt.setup.newleavesign")){
						s.setLine(0, ChatColor.GOLD + "[PvP Tower]");
						s.setLine(2, ChatColor.DARK_PURPLE + "Click Me");
						s.setLine(3, ChatColor.DARK_PURPLE + "To Exit!");
						s.update();
					}else{
						player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry you need this permission to make a PvPT Leave Sign: pvpt.setup.newleavesign");
					}
				}else if(s.getLine(0).equalsIgnoreCase("newpvptfood")){
					if(player.hasPermission("pvpt.setup.food")){
						s.setLine(0, ChatColor.GOLD + "[PvP Tower]");
						s.setLine(2, ChatColor.AQUA + "Heal Your");
						s.setLine(3, ChatColor.AQUA + "Food Here!");
						s.update();
					}else{
						player.sendMessage(ChatColor.RED + "[PvP Tower] Sorry you need this permission to make a player food heal sign: pvpt.setup.newfoodhealsign");
					}
				}
			}
		}
		
	//Death Checker
		@EventHandler
		public void onEntityDeath(EntityDeathEvent e){
		    if (e.getEntity() instanceof Player){
		        Player player = (Player)e.getEntity();
		        String towerName = this.getConfig().getString("players." + player.getName() + ".towerIN");
		        if(getConfig().contains("players." + player.getName() + ".towerIN")){
		        	player.setHealth(20.0);
		        	int joinX = this.getConfig().getInt("towers." + towerName + ".towerspawn" + ".X");
					int joinY = this.getConfig().getInt("towers." + towerName + ".towerspawn" + ".Y");
					int joinZ = this.getConfig().getInt("towers." + towerName + ".towerspawn" + ".Z");
					int joinYaw = this.getConfig().getInt("towers." + towerName + ".towerspawn" + ".Yaw");
					int joinPitch = this.getConfig().getInt("towers." + towerName + ".towerspawn" + ".Pitch");
					World joinWorld = player.getWorld();
					Location towerSpawn = new Location(joinWorld, joinX, joinY, joinZ, joinYaw, joinPitch);
					player.teleport(towerSpawn);
		        }
		    }
		}
		
	//Server Disconnect
		@SuppressWarnings("deprecation")
		@EventHandler
		public void onDisconnect(PlayerQuitEvent e){
			Player player = (Player) e.getPlayer();
			if(this.getConfig().contains("players." + player.getName() + ".towerIN")){
				int exitX = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".X");
				int exitY = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Y");
				int exitZ = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Z");
				int exitYaw = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Yaw");
				int exitPitch = this.getConfig().getInt("players." + player.getName() + ".exitCoords" + ".Pitch");
				World exitWorld = Bukkit.getWorld(this.getConfig().getString("players." + player.getName() + ".exitCoords" + ".World"));
				Location playerExit = new Location(exitWorld, exitX, exitY, exitZ, exitYaw, exitPitch);
				player.teleport(playerExit);
				this.getConfig().set("players." + player.getName() + ".towerIN", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".X", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Y", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Z", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Yaw", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".Pitch", null);
				this.getConfig().set("players." + player.getName() + ".exitCoords" + ".World", null);
				saveConfig();
				player.getInventory().clear();
				player.updateInventory();
			}
		}
}