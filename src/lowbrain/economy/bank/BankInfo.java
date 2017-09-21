package lowbrain.economy.bank;

import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.config.YamlConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.Yaml;

public class BankInfo {

    private double minBalance;
    private double maxBalance;
    private double currentBalance;
    private double profit;

    /**
     * constructor
     */
    public BankInfo() {
        FileConfiguration infoFile = LowbrainEconomy.getInstance().getConfig();
        profit = infoFile.getDouble("profit", 0.10);
        minBalance = infoFile.getDouble("min_bank_balance", 0);
        maxBalance = infoFile.getDouble("max_bank_balance", -1);
        currentBalance = infoFile.getDouble("current_bank_balance", Math.abs(infoFile.getDouble("initial_bank_balance", 5000)));

        if (minBalance < 0)
            minBalance = Double.MIN_VALUE;

        if (maxBalance < 0)
            maxBalance = Double.MAX_VALUE;
    }

    /**
     * save data to file
     * saves => current_value, current_bank_amount
     * @return true if succeed
     */
    public boolean save() {
        YamlConfig infoFile = LowbrainEconomy.getInstance().getConfig();
        infoFile.set("current_bank_balance", this.currentBalance);

        infoFile.save();

        return true;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public void setMinBalance(double minBalance) {
        this.minBalance = minBalance;
    }

    public double getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void withdraw(double amount) {
        setCurrentBalance(this.getCurrentBalance() - (+amount));
    }

    public void deposit(double amount) {
        setCurrentBalance(this.getCurrentBalance() + (+amount));
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getProfit() {
        return profit;
    }
}
