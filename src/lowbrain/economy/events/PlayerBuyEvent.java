package lowbrain.economy.events;

import lowbrain.economy.main.BankData;
import lowbrain.economy.main.BankInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class PlayerBuyEvent extends PlayerTransactionEvent {

    public PlayerBuyEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, EventType.BUYING);
    }

    @Override
    public boolean check(boolean log) {
        BankData data = new BankData(this.getItemStacks().get(0).getType());

        int qty = this.getQuantity();

        if (data.getCurrentQuantity() - qty < data.getMinQuantity()) {
            if (log)
                this.getPlayer().sendMessage(ChatColor.YELLOW + "The bank can no longer sells this item !");

            setValid(false);
            return isValid();
        }

        BankInfo bank = new BankInfo();

        if (bank.getCurrentAmount() - this.getPrice() < bank.getMinAmount()) {
            if (log)
                this.getPlayer().sendMessage(ChatColor.YELLOW + "The bank as reach is maximum capacity of coin !");

            setValid(false);
            return isValid();
        }

        return isValid();
    }
}
