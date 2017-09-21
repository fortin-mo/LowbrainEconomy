package lowbrain.economy.handlers.economy;

import lowbrain.economy.main.LowbrainEconomy;
import org.bukkit.plugin.Plugin;

public class EconHandler {
    public static final String ESSENTIALSX = "Essentials";
    public static final String VAULT = "Vault";

    private EconVault econVault;
    private EconEssential econEssential;

    private LowbrainEconomy plugin;
    public EconHandler (LowbrainEconomy plugin) {
        this(plugin, false);
    }

    public EconHandler(LowbrainEconomy plugin, boolean setup) {
        this.plugin = plugin;
        if (setup)
            this.setup();
    }

    public boolean setup() {
        Plugin vault;
        Plugin essentialsX;

        boolean success = false;

        if ((essentialsX = plugin.getServer().getPluginManager().getPlugin(ESSENTIALSX)) != null)
            success = (econEssential = new EconEssential(essentialsX)).setup();
        else if ((vault = plugin.getServer().getPluginManager().getPlugin(VAULT)) != null)
            success = (econVault = new EconVault(vault)).setup();

        return success;
    }

    public IEconomy get() {
        return econEssential != null ? econEssential : econVault != null ? econVault : null;
    }
}
