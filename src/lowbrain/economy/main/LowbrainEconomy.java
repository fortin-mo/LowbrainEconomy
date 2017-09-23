package lowbrain.economy.main;

import lowbrain.economy.handlers.CommandHandler;
import lowbrain.economy.handlers.DataHandler;
import lowbrain.economy.handlers.TaskHandler;
import lowbrain.economy.handlers.economy.EconHandler;
import lowbrain.library.config.YamlConfig;
import lowbrain.library.config.YamlLocalize;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

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
    private YamlConfig defaultConfig;
    private YamlLocalize localize;
    private DataHandler dataHandler;
    private EconHandler econHandler;
    private CommandHandler commandHandler;

    @Override
    public void onEnable()
    {
        this.getLogger().info("Loading LowbrainEconomy ...");
        instance = this;

        this.config = new YamlConfig("config.yml", this);
        this.bankConfig = new YamlConfig("bank-data.yml", this);
        this.defaultConfig = new YamlConfig("default.yml", this);
        this.localize = new YamlLocalize("localization.yml", this);

        if (!setupEconomy() ) {
            this.getLogger().severe(String.format("[%s] - Failed to setup any supported Economy system", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.dataHandler = new DataHandler(this);
        dataHandler.load();

        this.commandHandler = new CommandHandler(this);

        new TaskHandler(this).startNow( config.getInt("overtime_drop_interval", 720));

        this.getLogger().info(getDescription().getVersion() + " enabled!");
    }

    private boolean setupEconomy() {
        econHandler = new EconHandler(this);
        return econHandler.setup();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unloading LowbrainEconomy ...");
        this.getDataHandler().save();
    }

    @Contract(pure = true)
    public static LowbrainEconomy getInstance() {
        return instance;
    }

    @Override
    public YamlConfig getConfig() {
        return config;
    }

    public YamlConfig getBankConfig() {
        return bankConfig;
    }

    public YamlConfig getDefaultConfig() {
        return defaultConfig;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public YamlLocalize getLocalize() {
        return localize;
    }

    public void sendTo(Player who, String msg) {
        if (who == null || msg == null || msg.isEmpty())
            return;

        String fmt = ChatColor.AQUA + "[LowbrainEconomy] " + ChatColor.GREEN;
        fmt += msg;

        who.sendMessage(fmt);
    }

    public EconHandler getEconHandler() {
        return econHandler;
    }
}
