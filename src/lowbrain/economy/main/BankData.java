package lowbrain.economy.main;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BankData {

    private static final String INITIAL_VALUE = "initial_value";
    private static final String CURRENT_VALUE = "current_value";
    private static final String MIN_VALUE = "min_value";
    private static final String MAX_VALUE = "max_value";
    private static final String CURRENT_QUANTITY = "current_quantity";
    private static final String MAX_QUANTITY = "max_quantity";
    private static final String MIN_QUANTITY = "min_quantity";
    private static final String TIME_DIFF_DROP = "time_diff_drop";
    private static final String DIFF_PRICE_DROP = "diff_price_drop";
    private static final String PRICE_DROP = "price_drop";
    private static final String PRICE_INCREASE = "price_increase";
    private static final String LAST_SOLD = "last_sold";
    private static final String LAST_BOUGHT = "last_bought";

    private String name;
    private double initialValue;
    private double currentValue;
    private double minValue;
    private double maxValue;
    private int currentQuantity;
    private int maxQuantity;
    private int minQuantity;
    private Date lastSold;
    private Date lastBought;
    private int timeDiffDrop;
    private int diffPriceDrop;
    private int priceDrop;
    private int priceIncrease;

    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy mm:ss");

    public BankData(Material material) {
        this(material.getData().getName());
    }

    public BankData(String name) {
        this.name = name.toUpperCase();
        load();
    }

    private void load() {
        FileConfiguration bankFile = LowbrainEconomy.getInstance().getBankConfig();
        FileConfiguration configFile = LowbrainEconomy.getInstance().getConfig();

        ConfigurationSection itemSec = bankFile.getConfigurationSection(this.name);
        ConfigurationSection defSec = bankFile.getConfigurationSection("DEFAULT");

        Material mat = Material.getMaterial(this.name);

        if (mat == null)
            throw new Error("Material does not exists !");

        if (itemSec == null && defSec == null)
                throw new Error("Item wasn't found in the bank file and no DEFAULT is set !!");

        if (itemSec == null)
            itemSec = defSec; // use default

        initialValue = itemSec.getDouble(INITIAL_VALUE, defSec.getDouble(INITIAL_VALUE, 10));
        currentValue = itemSec.getDouble(CURRENT_VALUE, initialValue);
        minValue = itemSec.getDouble(MIN_VALUE, defSec.getDouble(MIN_VALUE, 1));

        if (minValue < 0)
            minValue = 1; // minimum value cannot be set lower than 0.0000001

        maxValue = itemSec.getDouble(MAX_VALUE, defSec.getDouble(MAX_VALUE, -1));

        if (maxValue < 0)
            maxValue = Double.MAX_VALUE; // set to infinite.. kind of

        currentQuantity = itemSec.getInt(CURRENT_QUANTITY, defSec.getInt(CURRENT_QUANTITY, 500));
        maxQuantity = itemSec.getInt(MAX_QUANTITY, defSec.getInt(MAX_QUANTITY, 100000));

        if (maxQuantity < 0)
            maxQuantity = Integer.MAX_VALUE; // set to infinite.. kind of

        minQuantity = itemSec.getInt(MIN_QUANTITY, defSec.getInt(MIN_QUANTITY, 500));

        if (minQuantity < 0)
            minQuantity = Integer.MIN_VALUE; // set to minus infinite.. kind of

        try {
            lastBought = dateFormat.parse(itemSec.getString(LAST_BOUGHT));
        } catch (Exception e) {
            lastBought = null;
        }

        try {
            lastSold = dateFormat.parse(itemSec.getString(LAST_SOLD));
        } catch (Exception e) {
            lastSold = null;
        }

        timeDiffDrop = itemSec.getInt(TIME_DIFF_DROP, configFile.getInt(TIME_DIFF_DROP, 1440));
        diffPriceDrop = itemSec.getInt(DIFF_PRICE_DROP, configFile.getInt(DIFF_PRICE_DROP, 1));

        if (diffPriceDrop < 0)
            diffPriceDrop = 1; // cannot be lower than zero

        priceDrop = itemSec.getInt(PRICE_DROP, configFile.getInt("default_" + PRICE_DROP, 1));

        if (priceDrop < 0)
            priceDrop = Math.abs(priceDrop);

        priceIncrease = itemSec.getInt(PRICE_INCREASE, configFile.getInt("default_" + PRICE_INCREASE, 1));

        if (priceIncrease < 0)
            priceIncrease = Math.abs(priceDrop);

    }

    private void save(FileConfiguration bankFile) {

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(double initialValue) {
        this.initialValue = initialValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Date getLastSold() {
        return lastSold;
    }

    public void setLastSold(Date lastSold) {
        this.lastSold = lastSold;
    }

    public Date getLastBought() {
        return lastBought;
    }

    public void setLastBought(Date lastBought) {
        this.lastBought = lastBought;
    }

    public int getTimeDiffDrop() {
        return timeDiffDrop;
    }

    public void setTimeDiffDrop(int timeDiffDrop) {
        this.timeDiffDrop = timeDiffDrop;
    }

    public int getDiffPriceDrop() {
        return diffPriceDrop;
    }

    public void setDiffPriceDrop(int diffPriceDrop) {
        this.diffPriceDrop = diffPriceDrop;
    }

    public int getPriceDrop() {
        return priceDrop;
    }

    public void setPriceDrop(int priceDrop) {
        this.priceDrop = priceDrop;
    }

    public int getPriceIncrease() {
        return priceIncrease;
    }

    public void setPriceIncrease(int priceIncrease) {
        this.priceIncrease = priceIncrease;
    }
}
