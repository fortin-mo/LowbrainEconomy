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

public abstract class PlayerTransactionEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private boolean valid = true;
    private boolean bypass = false;
    private double price = 0.0;
    private ArrayList<ItemStack> itemStacks = new ArrayList<>();
    private PlayerSellEvent.EventType method;

    /**
     * constructor for PlayerTransactionEvent
     * @param who the player who bought or selled items
     * @param itemStacks list of items
     * @param price total price (combines all items together)
     * @param method buying or selling
     */
    public PlayerTransactionEvent(final Player who, ArrayList<ItemStack> itemStacks, double price, EventType method){
        super(who);

        this.itemStacks = itemStacks;
        this.price = price;
        this.method = method;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        cancel = true;
    }

    @Contract(pure = true)
    @Override
    public HandlerList getHandlers() {
        return handlers;
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
     * get transaction method (BUYING OR SELLING)
     * @return method
     */
    public EventType getMethod() {
        return method;
    }

    /**
     * is the transaction valid or not
     * @return valid
     */
    public boolean isValid() {
        return valid;
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

    /**
     * set transaction valid property
     * @param valid true or false
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * is transaction validation bypassed
     * @return bypass
     */
    public boolean isBypass() {
        return bypass;
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

    public enum EventType {
        SELLING,
        BUYING
    }
}
