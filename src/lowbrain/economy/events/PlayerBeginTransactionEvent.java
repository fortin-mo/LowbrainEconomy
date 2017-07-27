package lowbrain.economy.events;

import lowbrain.economy.main.BankData;
import lowbrain.economy.main.BankInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public abstract class PlayerBeginTransactionEvent extends PlayerTransactionEvent {

    /**
     * constructor for PlayerBeginTransactionEvent
     *
     * @param who        the player who bought or selled items
     * @param itemStacks list of items
     * @param price      total price (combines all items together)
     * @param method     buying or selling
     */
    public PlayerBeginTransactionEvent(Player who, ArrayList<ItemStack> itemStacks, double price, TransactionType method) {
        super(who, itemStacks, price, method);
    }

    public void setItemStacks(ArrayList<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    /**
     * set the price of the transaction
     * @param price price
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * set transaction bypass property
     * if bypass is true, when transaction is confirm, validation will be bypassed
     * and data will still be saved
     * @param bypass bypass or not
     */
    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    /**
     * check if the bank can complete the transaction
     * if not, valid is set to false
     * @return check(true)
     */
    public boolean check() {
        return this.check(false);
    }

    /**
     * check if the bank can complete the transaction
     * if not, valid is set to false
     * @param log send message to user
     * @return isValid()
     */
    abstract public boolean check(boolean log);
}
