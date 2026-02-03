package com.example.marketregulator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:market_data.db";

    private final JavaPlugin plugin;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public void initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS market_items ("
                + "material_name TEXT PRIMARY KEY,"
                + "stock INTEGER NOT NULL,"
                + "base_price REAL NOT NULL,"
                + "min_price REAL NOT NULL,"
                + "max_price REAL NOT NULL,"
                + "target_stock INTEGER NOT NULL"
                + ")";

        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void saveItem(MarketItem item) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT OR REPLACE INTO market_items "
                    + "(material_name, stock, base_price, min_price, max_price, target_stock) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, item.getMaterial().name());
                statement.setInt(2, item.getStock());
                statement.setDouble(3, item.getBasePrice());
                statement.setDouble(4, item.getMinPrice());
                statement.setDouble(5, item.getMaxPrice());
                statement.setInt(6, item.getTargetStock());
                statement.executeUpdate();
            } catch (SQLException exception) {
                plugin.getLogger().severe("Failed to save market item: " + exception.getMessage());
            }
        });
    }

    public Map<Material, MarketItem> loadItems() throws SQLException {
        Map<Material, MarketItem> items = new HashMap<>();
        String sql = "SELECT material_name, stock, base_price, min_price, max_price, target_stock "
                + "FROM market_items";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String materialName = resultSet.getString("material_name");
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning("Unknown material in database: " + materialName);
                    continue;
                }
                int stock = resultSet.getInt("stock");
                double basePrice = resultSet.getDouble("base_price");
                double minPrice = resultSet.getDouble("min_price");
                double maxPrice = resultSet.getDouble("max_price");
                int targetStock = resultSet.getInt("target_stock");

                MarketItem item = new MarketItem(material, basePrice, minPrice, maxPrice, stock, targetStock);
                items.put(material, item);
            }
        }

        return items;
    }
}
