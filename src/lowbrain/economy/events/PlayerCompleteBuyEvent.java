package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerCompleteBuyEvent extends PlayerCompleteTransactionEvent{

    public PlayerCompleteBuyEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, TransactionType.BUYING);
    }

    public PlayerCompleteBuyEvent(PlayerBuyEvent e) {
        super(e);
    }
}
