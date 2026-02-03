package com.example.marketregulator;

import org.bukkit.Material;

public class MarketItem {
    public static final double DEFAULT_VOLATILITY = 0.05;

    private final Material material;
    private final double basePrice;
    private final double minPrice;
    private final double maxPrice;
    private int stock;
    private final int targetStock;
    private final double volatility;

    public MarketItem(
            Material material,
            double basePrice,
            double minPrice,
            double maxPrice,
            int stock,
            int targetStock
    ) {
        this(material, basePrice, minPrice, maxPrice, stock, targetStock, DEFAULT_VOLATILITY);
    }

    public MarketItem(
            Material material,
            double basePrice,
            double minPrice,
            double maxPrice,
            int stock,
            int targetStock,
            double volatility
    ) {
        this.material = material;
        this.basePrice = basePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.stock = stock;
        this.targetStock = targetStock;
        this.volatility = volatility;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getTargetStock() {
        return targetStock;
    }

    public double getVolatility() {
        return volatility;
    }

    public double getBuyPrice() {
        double rawPrice = basePrice * Math.pow(((double) targetStock) / Math.max(1, stock), volatility);
        return clamp(rawPrice, minPrice, maxPrice);
    }

    public double getSellPrice() {
        return clamp(getBuyPrice() * 0.95, minPrice, maxPrice);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
