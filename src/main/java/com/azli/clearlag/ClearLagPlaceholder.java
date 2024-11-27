package com.azli.clearlag;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ClearLagPlaceholder extends PlaceholderExpansion {

    private final ClearLag plugin;

    public ClearLagPlaceholder(ClearLag plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "clearlag";
    }

    @Override
    public String getAuthor() {
        return "Azli";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Ensures the placeholder persists after reloads
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.equalsIgnoreCase("timer")) {
            return String.valueOf(plugin.getTimeUntilNextClear());
        }
        return null; // Return null if the placeholder is not recognized
    }
}