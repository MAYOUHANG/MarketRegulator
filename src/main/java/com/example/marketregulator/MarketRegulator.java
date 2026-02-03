package com.example.marketregulator;

import net.milkbowl.vault.economy.Economy;
import java.sql.Connection;
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

        marketManager = new MarketManager(databaseManager, economy);
        marketGui = new MarketGUI(marketManager);
        getServer().getPluginManager().registerEvents(new MarketListener(), this);
        if (getCommand("mr") != null) {
            getCommand("mr").setExecutor(new MarketCommand(marketManager, marketGui));
        }
    }

    @Override
    public void onDisable() {
    }

    public MarketGUI getMarketGui() {
        return marketGui;
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
