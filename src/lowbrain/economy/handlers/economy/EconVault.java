package lowbrain.economy.handlers.economy;

import lowbrain.economy.main.LowbrainEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconVault implements IEconomy{
    private Plugin plugin;
    private Economy economy;

    public EconVault(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasEnough(Player player, double amount) {
        return economy.has(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        EconomyResponse resp = economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
        return resp.transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount) {
        EconomyResponse resp = economy.depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), amount);
        return resp.transactionSuccess();
    }

    @Override
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
