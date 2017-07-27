package lowbrain.economy.events;

import lowbrain.economy.main.BankData;
import lowbrain.economy.main.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public final class PlayerBuyEvent extends PlayerBeginTransactionEvent {

    public PlayerBuyEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, TransactionType.BUYING);
    }

    @Override
    public boolean check(boolean log) {
        BankData data = LowbrainEconomy.getInstance().getDataHandler().getSingle(this.getItemStacks().get(0).getType().name());

        if (data == null) {
            this.status = TransactionStatus.INVALID_DATA;
            return isValid();
        }

        int qty = this.getQuantity();

        if (data.getCurrentQuantity() - qty < data.getMinQuantity()) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), ChatColor.YELLOW + "The bank can no longer sell this item !");

            this.status = TransactionStatus.BANK_STOCK_LOW;
            return isValid();
        }

        BankInfo bank = LowbrainEconomy.getInstance().getDataHandler().getBank();

        if (bank.getCurrentAmount() + this.getPrice() > bank.getMaxAmount()) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), ChatColor.YELLOW + "The bank as reach is maximum capacity of coin !");

            this.status = TransactionStatus.BANK_CAPACITY_MAXED;
            return isValid();
        }

        return isValid();
    }
}
