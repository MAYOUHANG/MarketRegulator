package com.example.marketregulator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MarketListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MarketGUI)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        MarketGUI gui = (MarketGUI) holder;
        MarketManager manager = gui.getMarketManager();
        MarketItem item = manager.getItems().get(clickedItem.getType());
        if (item == null) {
            return;
        }

        int amount = resolveAmount(event.getClick());
        boolean isBuying = isBuying(event.getClick());
        if (amount <= 0) {
            return;
        }

        boolean success = manager.processTransaction(player, item, isBuying, amount);
        if (success) {
            gui.updateItem(event.getSlot(), item);
        }
    }

    private int resolveAmount(ClickType clickType) {
        if (clickType == ClickType.LEFT) {
            return 1;
        }
        if (clickType == ClickType.SHIFT_LEFT) {
            return 64;
        }
        if (clickType == ClickType.RIGHT) {
            return 1;
        }
        if (clickType == ClickType.SHIFT_RIGHT) {
            return 64;
        }
        return 0;
    }

    private boolean isBuying(ClickType clickType) {
        return clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT;
    }
}
