package lowbrain.economy.main;

import lowbrain.library.config.YamlConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class LowbrainEconomy extends JavaPlugin {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy mm:ss");
    public final static NumberFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    private static LowbrainEconomy instance;
    private YamlConfig config;
    private YamlConfig bankConfig;
    private DataHandler dataHandler;
    private Economy economy;

    @Override
    public void onEnable()
    {
        this.getLogger().info("Loading LowbrainEconomy ...");
        instance = this;

        loadConfig();

        if (!setupEconomy() ) {
            this.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.dataHandler = new DataHandler(this);
        dataHandler.load();

        this.getCommand("lbeconn").setExecutor(new CommandHandler(this));

        new TaskHandler(this).startNow( config.getInt("overtime_drop_interval", 720));

        this.getLogger().info(getDescription().getVersion() + " enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unloading LowbrainEconomy ...");
    }

    @Contract(pure = true)
    public static LowbrainEconomy getInstance() {
        return instance;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getBankConfig() {
        return bankConfig;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    private void loadConfig() {
        this.config = new YamlConfig("config.yml", this);
        this.bankConfig = new YamlConfig("bank.yml", this);
    }

    public void sendTo(Player who, String msg) {
        if (who == null || msg == null || msg.isEmpty())
            return;

        String fmt = ChatColor.GOLD + "[LowbrainEconomy] " + ChatColor.GREEN;
        fmt += msg;

        who.sendMessage(fmt);
    }

    public Economy getEconomy() {
        return economy;
    }
}
