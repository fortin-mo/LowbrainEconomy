package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class PlayerEconEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private double price = 0.0;
    private ArrayList<ItemStack> itemStacks = new ArrayList<>();
    private PlayerSellEvent.EventType method;

    /**
     * constructor for PlayerEconEvent
     * @param who the player who bought or selled items
     * @param itemStacks list of items
     * @param price total price (combines all items together)
     * @param method buying or selling
     */
    public PlayerEconEvent(final Player who, ArrayList<ItemStack> itemStacks, double price, EventType method){
        super(who);

        this.itemStacks = itemStacks;
        this.price = price;
        this.method = method;
    }

    @Contract(pure = true)
    @Override
    public boolean isCancelled() {
        return false;
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

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public ArrayList<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public EventType getMethod() {
        return method;
    }

    public enum EventType {
        SELLING,
        BUYING
    }
}
