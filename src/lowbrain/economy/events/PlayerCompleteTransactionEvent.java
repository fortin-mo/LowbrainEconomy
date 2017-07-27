package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public abstract class PlayerCompleteTransactionEvent extends PlayerTransactionEvent {

    /**
     * constructor for PlayerBeginTransactionEvent
     *
     * @param who        the player who bought or sold items
     * @param itemStacks list of items
     * @param price      total price (combines all items together)
     * @param method     buying or selling
     */
    public PlayerCompleteTransactionEvent(Player who, ArrayList<ItemStack> itemStacks, double price, TransactionType method) {
        super(who, itemStacks, price, method);
    }

    public PlayerCompleteTransactionEvent(PlayerBeginTransactionEvent e) {
        super(e.getPlayer(), e.getItemStacks(), e.getPrice(), e.getType());
    }
}
