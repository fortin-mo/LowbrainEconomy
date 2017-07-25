package lowbrain.economy.main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LowbrainEconomy extends JavaPlugin {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy mm:ss");

    private static LowbrainEconomy instance;

    private FileConfiguration config;
    private FileConfiguration bankConfig;

    @Override
    public void onEnable()
    {
        this.getLogger().info("Loading LowbrainEconomy ...");
        instance = this;

        loadConfig();

        this.getCommand("lbeconn").setExecutor(new CommandHandler(this));

        new TaskHandler(this).startNow( config.getInt("diff_drop_interval", 720));

        this.getLogger().info("[LowbrainEconomy] " + getDescription().getVersion() + " enabled!");
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

    private void loadConfig() {
        File configFile = new File(this.getDataFolder(),"config.yml");
        File staffFile = new File(this.getDataFolder(),"bank.yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        if (!staffFile.exists()) {
            staffFile.getParentFile().mkdirs();
            saveResource("bank.yml", false);
        }

        config = new YamlConfiguration();
        bankConfig = new YamlConfiguration();

        try {
            config.load(configFile);
            bankConfig.load(staffFile);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
