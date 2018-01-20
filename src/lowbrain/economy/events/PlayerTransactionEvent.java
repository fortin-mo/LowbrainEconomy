package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public abstract class PlayerTransactionEvent  extends PlayerEvent implements Cancellable {

    protected static final HandlerList handlers = new HandlerList();
    protected boolean cancel = false;
    protected boolean bypass = false;
    protected double price = 0.0;
    protected ArrayList<ItemStack> itemStacks = new ArrayList<>();
    protected TransactionType type;
    protected TransactionStatus status = TransactionStatus.VALID;
    protected BukkitRunnable task;


    /**
     * constructor for PlayerBeginTransactionEvent
     * @param who the player who bought or selled items
     * @param itemStacks list of items
     * @param price total price (combines all items together)
     * @param method buying or selling
     */
    public PlayerTransactionEvent(final Player who, ArrayList<ItemStack> itemStacks, double price, TransactionType method){
        super(who);

        this.itemStacks = itemStacks;
        this.price = price;
        this.type = method;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        cancel = true;

        if (task != null)
            task.cancel();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * get the total price of the transaction
     * @return price
     */
    public double getPrice() {
        return price;
    }

    /**
     * return the complete qty of a single items
     * loops through the item stacks
     * @return quantity
     */
    public int getQuantity() {
        int qty = 0;
        if (this.itemStacks == null || this.itemStacks.isEmpty())
            return qty;

        for (ItemStack item :
                this.itemStacks) {
            qty += item.getAmount();
        }

        return qty;
    }

    /**
     * get the list of item stacks in the transaction
     * @return itemStacks
     */
    public ArrayList<ItemStack> getItemStacks() {
        return itemStacks;
    }

    /**
     * get transaction type (BUYING OR SELLING)
     * @return type
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * is the transaction valid or not
     * @return valid
     */
    public boolean isValid() {
        return this.status == TransactionStatus.VALID;
    }

    /**
     * is transaction validation bypassed
     * @return bypass
     */
    public boolean isBypass() {
        return bypass;
    }

    public BukkitRunnable getTask() { return this.task; }
    public void setTask(BukkitRunnable t) { this.task = t; }
    public void cancelTask() {
        if (this.task != null)
            this.task.cancel();
    }

}
