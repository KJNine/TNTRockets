package net.kjnine.tntrockets;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class TNTRockets extends JavaPlugin implements Listener {

	private double config_small_explosionRadius;
	private String config_small_explodeFire;
	private boolean config_small_crossbowOnly;
	private String config_small_explodeBlocks;
	private double config_large_explosionRadius;
	private String config_large_explodeFire;
	private boolean config_large_crossbowOnly;
	private String config_large_explodeBlocks;

	private double config_velocity_modifier;
	private double config_velocity_largeballModifier;
	private double config_velocity_trailModifier;
	private boolean config_velocity_crossbowOnly;
	private boolean config_velocity_piercingOnly;
	private double config_velocity_piercingModifier;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		
		try {
			this.config_small_explosionRadius = getConfig().getDouble("small.explosion-radius", 4.0);
			this.config_small_explodeFire = getConfig().getString("small.explode-fire", "false");
			this.config_small_explodeBlocks = getConfig().getString("small.explode-blocks", "true");
			this.config_small_crossbowOnly = getConfig().getBoolean("small.crossbow-only", false);
			this.config_large_explosionRadius = getConfig().getDouble("large.explosion-radius", 6.0);
			this.config_large_explodeFire = getConfig().getString("large.explode-fire", "true");
			this.config_large_explodeBlocks = getConfig().getString("large.explode-blocks", "true");
			this.config_large_crossbowOnly = getConfig().getBoolean("large.crossbow-only", false);

			this.config_velocity_modifier = getConfig().getDouble("velocity-modifier", 1.0);
			this.config_velocity_largeballModifier = getConfig().getDouble("velocity-largeball-add-modifier", 0.0);
			this.config_velocity_trailModifier = getConfig().getDouble("velocity-trail-add-modifier", 0.0);
			this.config_velocity_crossbowOnly = getConfig().getBoolean("velocity-crossbow-only", false);
			this.config_velocity_piercingOnly = getConfig().getBoolean("velocity-piercing-only", false);
			this.config_velocity_piercingModifier = getConfig().getDouble("velocity-piercing-add-modifier", 0.0);
		} catch(Exception e) {
			getLogger().severe("There is an error in the TNTRockets config! Compare the config with the default config before reporting this error to the plugin author.");
			e.printStackTrace();
			return;
		}
        
        getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length == 1) return Arrays.asList("reload")
				.stream()
				.filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
				.collect(Collectors.toList());
		return Arrays.asList();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("tntrockets")) {
			if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
				try {
					reloadConfig();
					this.config_small_explosionRadius = getConfig().getDouble("small.explosion-radius", 4.0);
					this.config_small_explodeFire = getConfig().getString("small.explode-fire", "false");
					this.config_small_explodeBlocks = getConfig().getString("small.explode-blocks", "true");
					this.config_small_crossbowOnly = getConfig().getBoolean("small.crossbow-only", false);
					this.config_large_explosionRadius = getConfig().getDouble("large.explosion-radius", 6.0);
					this.config_large_explodeFire = getConfig().getString("large.explode-fire", "true");
					this.config_large_explodeBlocks = getConfig().getString("large.explode-blocks", "true");
					this.config_large_crossbowOnly = getConfig().getBoolean("large.crossbow-only", false);

					this.config_velocity_modifier = getConfig().getDouble("velocity-modifier", 1.0);
					this.config_velocity_largeballModifier = getConfig().getDouble("velocity-largeball-add-modifier", 0.0);
					this.config_velocity_trailModifier = getConfig().getDouble("velocity-trail-add-modifier", 0.0);
					this.config_velocity_crossbowOnly = getConfig().getBoolean("velocity-crossbow-only", false);
					this.config_velocity_piercingOnly = getConfig().getBoolean("velocity-piercing-only", false);
					this.config_velocity_piercingModifier = getConfig().getDouble("velocity-piercing-add-modifier", 0.0);
				} catch(Exception e) {
					sender.sendMessage(ChatColor.RED + "There is an error in the TNTRockets config! Please fix the error and try reloading it again.");
					getLogger().severe("There is an error in the TNTRockets config! Compare the config with the default config before reporting this error to the plugin author.");
					e.printStackTrace();
					return true;
				}
				sender.sendMessage(ChatColor.BLUE + "TNTRockets >> " + ChatColor.GREEN + "Config Reloaded.");
			} else {
				sender.sendMessage(ChatColor.BLUE + "TNTRockets >> " + ChatColor.GOLD + "TNTRockets v" + this.getDescription().getVersion() + " by KJNine");
				sender.sendMessage(ChatColor.BLUE + "TNTRockets >> " + ChatColor.RED + "Website: " + ChatColor.AQUA + this.getDescription().getWebsite());
				sender.sendMessage(ChatColor.BLUE + "TNTRockets >> " + ChatColor.RED + "Plugin Command: " +  ChatColor.GREEN + "/tntrockets reload");
			}
		}
		return true;
	}
	
	@EventHandler
	public void onExplode(FireworkExplodeEvent event) {
		Firework fw = event.getEntity();
		boolean hasLarge = false;
		if(fw.getFireworkMeta().getEffectsSize() > 0) {
			if(fw.getFireworkMeta().getEffects().stream().anyMatch(effect -> effect.getType() == (Type.BALL_LARGE))) {
				hasLarge = true;
			}
		} else return;
		if(fw.hasMetadata("tntrockets:crossbow_shot")) {
			double radius = config_small_explosionRadius;
			boolean fire = (config_small_explodeFire.equalsIgnoreCase("true") || config_small_explodeFire.equalsIgnoreCase("crossbow-only")), blocks = (config_small_explodeBlocks.equalsIgnoreCase("true") || config_small_explodeBlocks.equalsIgnoreCase("crossbow-only"));
			if(hasLarge) {
				radius = config_large_explosionRadius;
				fire = (config_large_explodeFire.equalsIgnoreCase("true") || config_large_explodeFire.equalsIgnoreCase("crossbow-only"));
				blocks = (config_large_explodeBlocks.equalsIgnoreCase("true") || config_large_explodeBlocks.equalsIgnoreCase("crossbow-only"));
			}
			fw.getWorld().createExplosion(fw.getLocation(), (float) radius, fire, blocks, fw);
		} else {
			if(hasLarge && !config_large_crossbowOnly) {
				double radius = config_large_explosionRadius;
				boolean fire = config_large_explodeFire.equalsIgnoreCase("true"), blocks = config_large_explodeBlocks.equalsIgnoreCase("true");
				fw.getWorld().createExplosion(fw.getLocation(), (float) radius, fire, blocks, fw);
			} else if(!hasLarge && !config_small_crossbowOnly) {
				double radius = config_small_explosionRadius;
				boolean fire = config_small_explodeFire.equalsIgnoreCase("true"), blocks = config_small_explodeBlocks.equalsIgnoreCase("true");
				fw.getWorld().createExplosion(fw.getLocation(), (float) radius, fire, blocks, fw);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onShoot(EntityShootBowEvent event) {
		if(event.getProjectile().getType() == EntityType.FIREWORK) {
			event.getProjectile().setMetadata("tntrockets:crossbow_shot", new FixedMetadataValue(this, true));
			event.getProjectile().setMetadata("tntrockets:piercing_level", new FixedMetadataValue(this, event.getBow().getEnchantmentLevel(Enchantment.PIERCING)));
		}
	}
	
	@EventHandler
	public void onLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity().getType() != EntityType.FIREWORK) return;
		Firework fw = (Firework) event.getEntity();
		double totalModifier = 1.0;
		if(event.getEntity().hasMetadata("tntrockets:crossbow_shot")) {
			int piercing = event.getEntity().getMetadata("tntrockets:piercing_level").get(0).asInt();
			if(!config_velocity_piercingOnly || piercing > 0) {
				totalModifier = config_velocity_modifier;
				totalModifier += piercing * config_velocity_piercingModifier;
				if(fw.getFireworkMeta().getEffects().stream().anyMatch(effect -> effect.getType() == Type.BALL_LARGE))
					totalModifier += config_velocity_largeballModifier;
				if(fw.getFireworkMeta().getEffects().stream().anyMatch(effect -> effect.hasTrail()))
					totalModifier += config_velocity_trailModifier;
			}
		} else if(!config_velocity_crossbowOnly) {
			totalModifier = config_velocity_modifier;
			if(fw.getFireworkMeta().getEffects().stream().anyMatch(effect -> effect.getType() == Type.BALL_LARGE))
				totalModifier += config_velocity_largeballModifier;
			if(fw.getFireworkMeta().getEffects().stream().anyMatch(effect -> effect.hasTrail()))
				totalModifier += config_velocity_trailModifier;
		}
		fw.setVelocity(fw.getVelocity().multiply(totalModifier));
	}
}
