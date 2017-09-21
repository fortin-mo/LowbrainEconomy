package lowbrain.economy.handlers.economy;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import lowbrain.economy.main.LowbrainEconomy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public class EconEssential implements IEconomy{
    private Plugin plugin;

    public EconEssential(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasEnough(Player player, double amount) {
        try {
            return Economy.hasEnough(player.getName(), BigDecimal.valueOf(amount));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        try {
            Economy.substract(player.getName(), BigDecimal.valueOf(amount));
            return true;
        } catch (UserDoesNotExistException e) {
            return false;
        } catch (NoLoanPermittedException e) {
            return false;
        } catch (ArithmeticException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(Player player, double amount) {
        try {
            Economy.add(player.getName(), BigDecimal.valueOf(amount));
            return true;
        } catch (UserDoesNotExistException e) {
            return false;
        } catch (NoLoanPermittedException e) {
            return false;
        } catch (ArithmeticException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean setup() {
        return true;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
