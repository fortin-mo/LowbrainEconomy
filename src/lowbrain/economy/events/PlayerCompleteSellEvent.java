package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerCompleteSellEvent extends PlayerCompleteTransactionEvent{
    public PlayerCompleteSellEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, TransactionType.SELLING);
    }

    public PlayerCompleteSellEvent(PlayerSellEvent e) {
        super(e);
    }
}
