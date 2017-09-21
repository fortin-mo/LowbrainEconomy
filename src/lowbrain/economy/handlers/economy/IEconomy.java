package lowbrain.economy.handlers.economy;

import org.bukkit.entity.Player;

public interface IEconomy {
    boolean hasEnough(Player player, double amount);
    boolean withdraw(Player player, double amount);
    boolean deposit(Player player, double amount);
    boolean setup();
}
