package com.example.marketregulator;

import java.sql.Connection;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MarketRegulator extends JavaPlugin {
    private Economy economy;
    private DatabaseManager databaseManager;
    private MarketManager marketManager;
    private MarketGUI marketGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            getLogger().severe("Vault dependency not found. Disabling MarketRegulator.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            getLogger().severe("Vault economy provider not found. Disabling MarketRegulator.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(this);
        try {
            try (Connection connection = databaseManager.connect()) {
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("Database connection failed.");
                }
            }
            databaseManager.initialize();
        } catch (Exception exception) {
            getLogger().severe("Failed to initialize database: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        marketManager = new MarketManager(this, databaseManager, economy);
        marketGui = new MarketGUI(this, marketManager);
        getServer().getPluginManager().registerEvents(new MarketListener(), this);
        if (getCommand("mr") != null) {
            getCommand("mr").setExecutor(new MarketCommand(this, marketManager, marketGui));
        }
    }

    @Override
    public void onDisable() {
    }

    public MarketGUI getMarketGui() {
        return marketGui;
    }

    public String getMsg(String path) {
        String message = getConfig().getString(path, path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String formatMsg(String path, String... replacements) {
        String message = getMsg(path);
        if (replacements == null) {
            return message;
        }
        String formatted = message;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            formatted = formatted.replace(replacements[i], replacements[i + 1]);
        }
        return formatted;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }
        economy = provider.getProvider();
        return economy != null;
    }
}
