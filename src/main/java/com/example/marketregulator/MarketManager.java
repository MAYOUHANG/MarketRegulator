package com.example.marketregulator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class MarketManager {
    private final HashMap<Material, MarketItem> items;
    private final DatabaseManager databaseManager;
    private final Economy economy;

    public MarketManager(DatabaseManager databaseManager, Economy economy) {
        this.databaseManager = databaseManager;
        this.economy = economy;
        this.items = new HashMap<>();
        loadExistingItems();
    }

    public Map<Material, MarketItem> getItems() {
        return new HashMap<>(items);
    }

    public void addItem(Material material,
                        double basePrice,
                        int targetStock) {
        addItem(material, basePrice, basePrice * 0.5, basePrice * 2, targetStock, targetStock,
                MarketItem.DEFAULT_VOLATILITY);
    }

    public void addItem(Material material,
                        double basePrice,
                        double minPrice,
                        double maxPrice,
                        int stock,
                        int targetStock,
                        double volatility) {
        MarketItem item = new MarketItem(material, basePrice, minPrice, maxPrice, stock, targetStock, volatility);
        items.put(material, item);
        databaseManager.saveItem(item);
    }

    public boolean processTransaction(Player player, MarketItem item, boolean isBuying, int amount) {
        if (amount <= 0) {
            return false;
        }

        if (isBuying) {
            return processBuy(player, item, amount);
        }

        return processSell(player, item, amount);
    }

    private boolean processBuy(Player player, MarketItem item, int amount) {
        double totalPrice = item.getBuyPrice() * amount;
        if (!economy.has(player, totalPrice)) {
            return false;
        }
        if (item.getStock() < amount) {
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, totalPrice);
        if (!response.transactionSuccess()) {
            return false;
        }

        ItemStack stack = new ItemStack(item.getMaterial(), amount);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        if (!leftovers.isEmpty()) {
            economy.depositPlayer(player, totalPrice);
            return false;
        }

        item.setStock(item.getStock() - amount);
        databaseManager.saveItem(item);
        return true;
    }

    private boolean processSell(Player player, MarketItem item, int amount) {
        ItemStack stack = new ItemStack(item.getMaterial(), amount);
        if (!player.getInventory().containsAtLeast(stack, amount)) {
            return false;
        }

        Map<Integer, ItemStack> removed = player.getInventory().removeItem(stack);
        if (!removed.isEmpty()) {
            return false;
        }

        double totalPrice = item.getSellPrice() * amount;
        EconomyResponse response = economy.depositPlayer(player, totalPrice);
        if (!response.transactionSuccess()) {
            player.getInventory().addItem(stack);
            return false;
        }

        item.setStock(item.getStock() + amount);
        databaseManager.saveItem(item);
        return true;
    }

    private void loadExistingItems() {
        try {
            Map<Material, MarketItem> loaded = databaseManager.loadItems();
            items.putAll(loaded);
        } catch (SQLException exception) {
            databaseManager.getPlugin().getLogger().severe("Failed to load market items: " + exception.getMessage());
        }
    }
}
