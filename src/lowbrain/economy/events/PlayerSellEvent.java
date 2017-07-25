package lowbrain.economy.events;

import lowbrain.economy.main.BankData;
import lowbrain.economy.main.BankInfo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;


public final class PlayerSellEvent extends PlayerTransactionEvent {

    public PlayerSellEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, EventType.SELLING);
    }

    @Override
    public boolean check(boolean log) {
        BankData data = new BankData(this.getItemStacks().get(0).getType());

        int qty = this.getQuantity();

        if (data.getCurrentQuantity() + qty > data.getMaxQuantity()) {
            if (log)
                this.getPlayer().sendMessage(ChatColor.YELLOW + "The bank can no longer accept sells for this item !");

            setValid(false);
            return isValid();
        }

        BankInfo bank = new BankInfo();

        if (bank.getCurrentAmount() + this.getPrice() > bank.getMaxAmount()) {
            if (log)
                this.getPlayer().sendMessage(ChatColor.YELLOW + "The bank doesn't have enough coin to buy this amount of product!");

            setValid(false);
            return isValid();
        }

        return isValid();
    }
}
