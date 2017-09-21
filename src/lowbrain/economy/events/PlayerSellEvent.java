package lowbrain.economy.events;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;


public final class PlayerSellEvent extends PlayerBeginTransactionEvent {

    public PlayerSellEvent(Player who, ArrayList<ItemStack> itemStacks, double price) {
        super(who, itemStacks, price, TransactionType.SELLING);
    }

    @Override
    public boolean check(boolean log) {
        BankData data = LowbrainEconomy.getInstance().getDataHandler().getSingle(this.getItemStacks().get(0).getType().name());

        if (data == null) {
            this.status = TransactionStatus.INVALID_DATA;
            return isValid();
        }

        int qty = this.getQuantity();

        if (data.getCurrentQuantity() + qty > data.getMaxQuantity()) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), LowbrainEconomy.getInstance().getLocalize().format("bank_cant_buy"));

            this.status = TransactionStatus.BANK_STOCK_MAXED;
            return isValid();
        }

        BankInfo bank = LowbrainEconomy.getInstance().getDataHandler().getBank();

        if (bank.getCurrentBalance() - this.getPrice() < bank.getMinBalance()) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), LowbrainEconomy.getInstance().getLocalize().format("bank_topped_balance"));

            this.status = TransactionStatus.BANK_BALANCE_LOW;
            return isValid();
        }

        if (!playerHasItems(this.getPlayer(), this.getItemStacks().get(0), qty)) {
            if (log)
                LowbrainEconomy.getInstance().sendTo(this.getPlayer(), LowbrainEconomy.getInstance().getLocalize().format("missing_from_inventory"));

            this.status = TransactionStatus.PLAYER_MISSING_INVENTORY;
            return isValid();
        }

        return isValid();
    }

    public static boolean playerHasItems(Player player, ItemStack itemSold, int quantity) {
        if (quantity <= 0)
            return true;

        int pQty = 0;

        ItemMeta itemMeta = itemSold.getItemMeta();

        String itemName = itemMeta != null ? itemMeta.getDisplayName() : null;

        if (itemName != null && itemName.trim().length() != 0)
            itemName = ChatColor.stripColor(itemName);

        for (ItemStack itemStack : player.getInventory()) {

            if (pQty >= quantity) // no need for further process
                break;

            if (itemStack.getType() != itemSold.getType())
                continue;

            int c = 0;

            if (itemName != null) { // check custom item
                ItemMeta iMeta = itemStack.getItemMeta();

                String iName = iMeta != null ? iMeta.getDisplayName() : null;

                if (iName != null && ChatColor.stripColor(iName) == itemName)
                    c = itemStack.getAmount();

            } else {
                c = itemStack.getAmount();
            }

            pQty += c;
        }

        return pQty >= quantity;
    }
}
