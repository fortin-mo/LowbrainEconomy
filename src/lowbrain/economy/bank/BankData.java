package lowbrain.economy.bank;

import lowbrain.economy.handlers.DataHandler;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.config.YamlConfig;
import lowbrain.library.fn;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.Date;

public class BankData {
    public final static String DEFAULT = "DEFAULT";

    private static final String INITIAL_VALUE = "initial_value";
    private static final String CURRENT_VALUE = "current_value";
    private static final String MIN_VALUE = "min_value";
    private static final String MAX_VALUE = "max_value";
    private static final String CURRENT_QUANTITY = "current_quantity";
    private static final String MAX_QUANTITY = "max_quantity";
    private static final String MIN_QUANTITY = "min_quantity";
    private static final String OVERTIME_DIFF_DROP = "overtime_diff_drop";
    private static final String OVERTIME_PRICE_DROP = "overtime_price_drop";
    private static final String PRICE_DROP = "price_drop";
    private static final String PRICE_INCREASE = "price_increase";
    private static final String LAST_SOLD = "last_sold";
    private static final String LAST_BOUGHT = "last_bought";
    private static final String TRANSACTION_LIMIT = "transaction_limit";
    private static final String PROFIT = "profit";

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
    private double overtimeDiffDrop;
    private double overtimePriceDrop;
    private double priceDrop;
    private double priceIncrease;
    private int transactionLimit;
    private double profit;
    private ItemStack itemStack;

    private BankData() {}

    public BankData(String name, ItemStack itemStack) {
        if (fn.StringIsNullOrEmpty(name))
            throw new NullArgumentException("name");
        if (itemStack == null)
            throw new NullArgumentException("itemStack");

        this.name = name.toUpperCase();
        this.itemStack = itemStack;
        this.load();
    }

    public BankData(Material material) {
        this(material == null ? null : material.name(), material == null ? null : new ItemStack(material, 1));
    }

    /*
    public BankData(String name, boolean minLoad) {
        this.name = name.toUpperCase();
        load(minLoad);
    }
    */

    /**
     * save data to file
     * saves => current_value, current_quantity, last_bought and last sold
     * @return true if succeed
     */
    public boolean save() {
        return this.save(true);
    }

    public boolean save(boolean saveToFile) {
        if (this.name == DEFAULT)
            return true; // do not save DEFAULT to bank-data.yml

        YamlConfig bankFile = LowbrainEconomy.getInstance().getBankConfig();

        ConfigurationSection saveTo = bankFile.getConfigurationSection(this.name);

        if (saveTo == null)
            saveTo = bankFile.createSection(this.name);

        saveTo.set("current_value", this.currentValue);
        saveTo.set("current_quantity", this.currentQuantity);
        saveTo.set("last_bought",
                this.lastBought == null ? "" :
                        LowbrainEconomy.DATE_FORMAT.format(this.lastBought));
        saveTo.set("last_sold",
                this.lastSold == null ? "" :
                        LowbrainEconomy.DATE_FORMAT.format(this.lastSold));

        if (saveToFile)
            bankFile.save();

        return true;
    }

    private void load() {
        YamlConfig bankFile = LowbrainEconomy.getInstance().getBankConfig();
        YamlConfig defaultFile = LowbrainEconomy.getInstance().getDefaultConfig();
        YamlConfig configFile = LowbrainEconomy.getInstance().getConfig();

        ConfigurationSection itemSec = bankFile.getConfigurationSection(this.name);
        ConfigurationSection defSec = defaultFile.getConfigurationSection("DEFAULT");

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

        profit = itemSec.getDouble(PROFIT, defSec.getDouble(PROFIT, configFile.getDouble(PROFIT, 0.10)));

        currentQuantity = itemSec.getInt(CURRENT_QUANTITY, defSec.getInt(CURRENT_QUANTITY, 500));
        maxQuantity = itemSec.getInt(MAX_QUANTITY, defSec.getInt(MAX_QUANTITY, 100000));

        if (maxQuantity < 0)
            maxQuantity = Integer.MAX_VALUE; // set to infinite.. kind of

        minQuantity = itemSec.getInt(MIN_QUANTITY, defSec.getInt(MIN_QUANTITY, 500));

        if (minQuantity < 0)
            minQuantity = Integer.MIN_VALUE; // set to minus infinite.. kind of

        try {
            lastBought = LowbrainEconomy.DATE_FORMAT.parse(itemSec.getString(LAST_BOUGHT));
        } catch (Exception e) {
            lastBought = null;
        }

        try {
            lastSold = LowbrainEconomy.DATE_FORMAT.parse(itemSec.getString(LAST_SOLD));
        } catch (Exception e) {
            lastSold = null;
        }

        overtimeDiffDrop = itemSec.getDouble(OVERTIME_DIFF_DROP, configFile.getDouble(OVERTIME_DIFF_DROP, 1440));
        overtimePriceDrop = itemSec.getDouble(OVERTIME_PRICE_DROP, configFile.getDouble(OVERTIME_PRICE_DROP, 1));

        if (overtimePriceDrop < 0)
            overtimePriceDrop = 1; // cannot be lower than zero

        priceDrop = Math.abs(itemSec.getDouble(PRICE_DROP, configFile.getDouble("default_" + PRICE_DROP, 1)));
        priceIncrease = Math.abs(itemSec.getDouble(PRICE_INCREASE, configFile.getDouble("default_" + PRICE_INCREASE, 1)));

        transactionLimit = itemSec.getInt(TRANSACTION_LIMIT, defSec.getInt(TRANSACTION_LIMIT, -1));
        if (transactionLimit < 0)
            transactionLimit = Integer.MAX_VALUE;
    }

    public String getName() {
        return name;
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
        if (this.currentValue < this.minValue)
            this.currentValue = this.minValue;
        else if (this.currentValue > this.maxValue)
            this.currentValue = this.maxValue;
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
        if (this.currentQuantity < this.minQuantity)
            this.currentQuantity = this.minQuantity;
        else if (this.currentQuantity > this.maxQuantity)
            this.currentQuantity = this.maxQuantity;
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

    public double getOvertimeDiffDrop() {
        return overtimeDiffDrop;
    }

    public void setOvertimeDiffDrop(int overtimeDiffDrop) {
        this.overtimeDiffDrop = overtimeDiffDrop;
    }

    public double getOvertimePriceDrop() {
        return overtimePriceDrop;
    }

    public void increaseValueBy (double v) {
        v = Math.abs(v);
        this.setCurrentValue(this.currentValue + v);
    }

    public void decreaseValueBy (double v) {
        v = Math.abs(v);
        this.setCurrentValue(this.currentValue - v);
    }

    public void increaseValue() {
        this.setCurrentValue(this.currentValue + this.priceIncrease);
    }

    public void decreaseValue() {
        this.setCurrentValue(this.currentValue - this.priceDrop);
    }

    public void setOvertimePriceDrop(int overtimePriceDrop) {
        this.overtimePriceDrop = overtimePriceDrop;
    }

    public double getPriceDrop() {
        return priceDrop;
    }

    public void setPriceDrop(int priceDrop) {
        this.priceDrop = priceDrop;
    }

    public double getPriceIncrease() {
        return priceIncrease;
    }

    public void setPriceIncrease(int priceIncrease) {
        this.priceIncrease = priceIncrease;
    }

    public double getProfit() {
        return profit;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
