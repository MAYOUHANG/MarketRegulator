package com.example.marketregulator;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketCommand implements CommandExecutor {
    private final MarketRegulator plugin;
    private final MarketManager marketManager;
    private final MarketGUI marketGui;

    public MarketCommand(MarketRegulator plugin, MarketManager marketManager, MarketGUI marketGui) {
        this.plugin = plugin;
        this.marketManager = marketManager;
        this.marketGui = marketGui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("open")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMsg("prefix") + plugin.getMsg("only-player"));
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
                sender.sendMessage(plugin.getMsg("prefix")
                        + plugin.formatMsg("unknown-material", "{item}", args[2]));
                return true;
            }

            double basePrice;
            int targetStock;
            try {
                basePrice = Double.parseDouble(args[3]);
                targetStock = Integer.parseInt(args[4]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(plugin.getMsg("prefix") + plugin.getMsg("invalid-number"));
                return true;
            }

            marketManager.addItem(material, basePrice, targetStock);
            sender.sendMessage(plugin.getMsg("prefix")
                    + plugin.formatMsg("stock-updated",
                    "{item}", formatMaterialName(material),
                    "{stock}", String.valueOf(targetStock)));
            return true;
        }

        sender.sendMessage(plugin.getMsg("prefix")
                + plugin.formatMsg("usage-open", "{command}", label));
        sender.sendMessage(plugin.getMsg("prefix")
                + plugin.formatMsg("usage-admin", "{command}", label));
        return true;
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
}
