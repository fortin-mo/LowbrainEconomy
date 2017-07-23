package lowbrain.economy.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

public class LowbrainEconomy extends JavaPlugin {

    private static LowbrainEconomy instance;

    @Override
    public void onEnable()
    {
        this.getLogger().info("Loading LowbrainEconomy ...");
        instance = this;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Unloading LowbrainEconomy ...");
    }

    @Contract(pure = true)
    public static LowbrainEconomy getInstance() {
        return instance;
    }
}
