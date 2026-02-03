package com.example.marketregulator;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketCommand implements CommandExecutor {
    private final MarketManager marketManager;
    private final MarketGUI marketGui;

    public MarketCommand(MarketManager marketManager, MarketGUI marketGui) {
        this.marketManager = marketManager;
        this.marketGui = marketGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can open the market.");
                return true;
            }
            marketGui.openMarket((Player) sender);
            return true;
        }

        if (args.length == 5
                && args[0].equalsIgnoreCase("admin")
                && args[1].equalsIgnoreCase("add")) {
            Material material = Material.matchMaterial(args[2]);
            if (material == null) {
                sender.sendMessage("Unknown material: " + args[2]);
                return true;
            }

            double basePrice;
            int targetStock;
            try {
                basePrice = Double.parseDouble(args[3]);
                targetStock = Integer.parseInt(args[4]);
            } catch (NumberFormatException exception) {
                sender.sendMessage("Base price and target stock must be numeric.");
                return true;
            }

            marketManager.addItem(material, basePrice, targetStock);
            sender.sendMessage("Added market item: " + material.name());
            return true;
        }

        sender.sendMessage("Usage: /" + label + " open");
        sender.sendMessage("Usage: /" + label + " admin add <material> <basePrice> <targetStock>");
        return true;
    }
}
