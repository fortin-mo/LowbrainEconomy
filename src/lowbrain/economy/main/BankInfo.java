package lowbrain.economy.main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class BankInfo {

    private double minAmount;
    private double maxAmount;
    private double currentAmount;

    /**
     * constructor
     */
    public BankInfo() {
        FileConfiguration infoFile = LowbrainEconomy.getInstance().getConfig();

        minAmount = infoFile.getDouble("min_bank_amount", 0);
        maxAmount = infoFile.getDouble("max_bank_amount", -1);
        currentAmount = infoFile.getDouble("current_bank_amount", Math.abs(infoFile.getDouble("initial_bank_amount", 5000)));

        if (minAmount < 0)
            minAmount = Double.MIN_VALUE;

        if (maxAmount < 0)
            maxAmount = Double.MAX_VALUE;
    }

    /**
     * save data to file
     * saves => current_value, current_bank_amount
     * @return true if succeed
     */
    public boolean save() {
        FileConfiguration infoFile = LowbrainEconomy.getInstance().getConfig();

        infoFile.set("current_bank_amount", this.currentAmount);

        return true;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }
}
