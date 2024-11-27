package com.azli.clearlag;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ClearLag extends JavaPlugin {

    private BukkitTask clearLagTask;
    private int timeUntilNextClear;
    private boolean isPlaceholderApiAvailable;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        isPlaceholderApiAvailable = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        // Register custom placeholders if PlaceholderAPI is available
        if (isPlaceholderApiAvailable) {
            new ClearLagPlaceholder(this).register();
        }

        startAutoClearLag();
        getLogger().info("ClearLag Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (clearLagTask != null) {
            clearLagTask.cancel();
        }
        getLogger().info("ClearLag Plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("clearlag")) {
            if (sender.hasPermission("clearlag.command")) {
                if (args.length == 1) {
                    try {
                        int seconds = Integer.parseInt(args[0]);
                        startClearLagTimer(seconds);
                        sender.sendMessage(ChatColor.GREEN + "ClearLag timer started. Items will be cleared in " + seconds + " seconds.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number format. Please enter an integer value for the timer.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /clearlag <seconds>");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("clearlagreload")) {
            if (sender.hasPermission("clearlag.reload")) {
                reloadConfig();
                if (clearLagTask != null) {
                    clearLagTask.cancel();
                }
                startAutoClearLag();
                sender.sendMessage(ChatColor.GREEN + "ClearLag configuration reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
            return true;
        }
        return false;
    }

    private void startClearLagTimer(int seconds) {
        timeUntilNextClear = seconds;

        new BukkitRunnable() {
            @Override
            public void run() {
                int removedItems = clearLag();
                String finalMessage = getConfig().getString("final-message", "[ClearLag] %count% items have been cleared from the world.");
                Bukkit.broadcastMessage(applyPlaceholders(finalMessage.replace("%count%", String.valueOf(removedItems))));
            }
        }.runTaskLater(this, seconds * 20L); // Convert seconds to ticks

        // Update countdown timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeUntilNextClear > 0) {
                    timeUntilNextClear--;
                }
            }
        }.runTaskTimer(this, 0, 20L); // Update every second
    }

    private int clearLag() {
        int removedItems = 0;
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            if (entity instanceof Item) {
                entity.remove();
                removedItems++;
            }
        }
        return removedItems;
    }

    private void startAutoClearLag() {
        int interval = getConfig().getInt("auto-clear-interval", 299); // Default to 299 seconds

        clearLagTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeUntilNextClear = interval;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int removedItems = clearLag();
                        String finalMessage = getConfig().getString("final-message", "[ClearLag] %count% items have been cleared from the world.");
                        Bukkit.broadcastMessage(applyPlaceholders(finalMessage.replace("%count%", String.valueOf(removedItems))));
                    }
                }.runTaskLater(ClearLag.this, interval * 20L); // Convert seconds to ticks
            }
        }.runTaskTimer(this, 0, interval * 20L);

        // Update countdown timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeUntilNextClear > 0) {
                    timeUntilNextClear--;
                }
            }
        }.runTaskTimer(this, 0, 20L); // Update every second
    }

    private String applyPlaceholders(String message) {
        if (isPlaceholderApiAvailable) {
            return PlaceholderAPI.setPlaceholders(null, message.replace("%time%", String.valueOf(timeUntilNextClear)));
        }
        return ChatColor.translateAlternateColorCodes('&', message.replace("%time%", String.valueOf(timeUntilNextClear)));
    }

    public int getTimeUntilNextClear() {
        return timeUntilNextClear;
    }
}