package lowbrain.economy.events;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import org.bukkit.Bukkit;
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
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), LowbrainEconomy.getInstance().getLocalize().format("bank_cant_sell"));

            this.status = TransactionStatus.BANK_STOCK_LOW;
            return isValid();
        }

        BankInfo bank = LowbrainEconomy.getInstance().getDataHandler().getBank();

        if (bank.getCurrentBalance() + this.getPrice() > bank.getMaxBalance()) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(),
                        LowbrainEconomy.getInstance().getLocalize().format("bank_topped_balance", bank.getMaxBalance()));

            this.status = TransactionStatus.BANK_BALANCE_MAXED;
            return isValid();
        }

        if (!LowbrainEconomy.getInstance().getEconHandler().get().hasEnough(this.getPlayer(), price)) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), LowbrainEconomy.getInstance().getLocalize().format("insufficient_funds"));

            this.status = TransactionStatus.PLAYER_INSUFFICIENT_FUNDS;
            return isValid();
        }

        return isValid();
    }
}
