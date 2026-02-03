package com.example.marketregulator;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MarketGUI implements InventoryHolder {
    private static final int INVENTORY_SIZE = 54;

    private final MarketRegulator plugin;
    private final MarketManager marketManager;
    private Inventory inventory;

    public MarketGUI(MarketRegulator plugin, MarketManager marketManager) {
        this.plugin = plugin;
        this.marketManager = marketManager;
    }

    public void openMarket(Player player) {
        inventory = Bukkit.createInventory(this, INVENTORY_SIZE, plugin.getMsg("gui-title"));
        int slot = 0;
        for (MarketItem item : marketManager.getItems().values()) {
            if (slot >= INVENTORY_SIZE) {
                break;
            }
            inventory.setItem(slot, createItemStack(item));
            slot++;
        }
        player.openInventory(inventory);
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public void updateItem(int slot, MarketItem item) {
        if (inventory == null || slot < 0 || slot >= INVENTORY_SIZE) {
            return;
        }
        inventory.setItem(slot, createItemStack(item));
    }

    private ItemStack createItemStack(MarketItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(formatMaterialName(item.getMaterial()));
            List<String> lore = new ArrayList<>();
            lore.add(plugin.formatMsg("buy-price-label", "{price}", String.format("%.2f", item.getBuyPrice())));
            lore.add(plugin.formatMsg("sell-price-label", "{price}", String.format("%.2f", item.getSellPrice())));
            lore.add(plugin.formatMsg("stock-level-label", "{stock}", String.valueOf(item.getStock())));
            lore.add(plugin.getMsg("click-buy"));
            lore.add(plugin.getMsg("click-sell"));
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String formatMaterialName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(" ");
        }
        return builder.toString().trim();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
