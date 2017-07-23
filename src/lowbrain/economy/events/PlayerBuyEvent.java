package lowbrain.economy.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public final class PlayerBuyEvent extends PlayerEconEvent {

    public PlayerBuyEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, EventType.BUYING);
    }
}
