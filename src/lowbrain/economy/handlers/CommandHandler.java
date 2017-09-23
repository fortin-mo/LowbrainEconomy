package lowbrain.economy.handlers;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.events.*;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.command.Command;
import lowbrain.library.fn;
import lowbrain.library.main.LowbrainLibrary;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Tree;
import org.bukkit.material.Wood;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandHandler extends Command {

    private final LowbrainEconomy plugin;
    private HashMap<UUID, PlayerBeginTransactionEvent> confirmations;
    // commands
    private Command onPricey;
    private Command onCheapest;
    private Command onConfirm;
    private Command onCancel;
    private Command onCheck;
    private Command onSell;
    private Command onBuy;
    private Command onSave;


    /**
     * constructor
     * @param plugin
     */
    public CommandHandler(LowbrainEconomy plugin) {
        super("econ");
        this.plugin = plugin;
        this.confirmations = new HashMap<>();
        this.onlyPlayer(true);
        LowbrainLibrary.getInstance().getBaseCmdHandler().register("econ", this);
        this.subbing();
    }

    @Override
    public CommandStatus execute(CommandSender who, String[] args, String cmd) {
        String msg = ChatColor.WHITE + "/lb econ <cmd> <...args>";
        msg += "\n/lb econ buy <item> <quantity> : buy item form bank";
        msg += "\n/lb econ sell <item> <quantity> : sell item to bank";
        msg += "\n/lb econ confirm : confirm current transaction";
        msg += "\n/lb econ cancel : cancel current transaction";
        msg += "\n/lb econ pricey [<limit>] : list pricey items";
        msg += "\n/lb econ cheapest [<limit>] : list cheapest items";
        msg += "\n/lb econ check <item> : check item current value";
        who.sendMessage(msg);
        return CommandStatus.VALID;
    }

    private void subbing() {
        // register pricey command
        this.register("pricey", onPricey = new Command("pricey") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onPricey((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onPricey.addPermission("lb.econ.cheapest");

        // register cheapest command
        this.register("cheapest", onCheapest = new Command("cheapest") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onCheapest((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onCheapest.addPermission("lb.econ.cheapest");

        // register confirm command
        this.register("confirm", onConfirm = new Command("confirm") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onConfirm((Player)who) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onConfirm.addPermission("lb.econ.confirm");

        // register cancel command
        this.register("cancel", onCancel = new Command("cancel") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onCancel((Player)who) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onCancel.addPermission("lb.econ.cancel");

        // register check command
        this.register("check", onCheck = new Command("check") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onCheck((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onCheck.addPermission("lb.econ.check");

        // register sell command
        this.register("sell", onSell = new Command("sell") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onSell((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onSell.addPermission("lb.econ.sell");

        // register buy command
        this.register("buy", onBuy = new Command("buy") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onBuy((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onBuy.addPermission("lb.econ.buy");

        this.register("save", onSave = new Command("save") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                plugin.getDataHandler().save();
                return CommandStatus.VALID;
            }
        });
        onSave.addPermission("lb.econ.save");
    }

    /**
     * on pricey command
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onPricey(Player who, String[] args) {
        int limit = 10;

        if (args.length > 0)
            limit = fn.toInteger(args[0], 10);

        List<BankData> list = plugin.getDataHandler().pricey();

        plugin.sendTo(who, plugin.getLocalize().format("pricey_list_header", limit));
        list.forEach(d -> {
            plugin.sendTo(who, plugin.getLocalize().format("pricey_list_item", new Object[]{
                    d.getName(),
                    fn.toMoney(d.getCurrentValue()),
                    fn.toMoney(d.getCurrentValue() * (1 - d.getProfit())),
                    d.getCurrentQuantity(),
            }));
        });
        plugin.sendTo(who, plugin.getLocalize().format("pricey_list_footer"));

        return true;
    }

    /**
     * on cheapest command
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onCheapest(Player who, String[] args) {
        int limit = 10;

        if (args.length > 0)
            limit = fn.toInteger(args[0], 10);

        List<BankData> list = plugin.getDataHandler().cheapest();

        plugin.sendTo(who, plugin.getLocalize().format("cheapest_list_header", limit));
        list.forEach(d -> {
            plugin.sendTo(who, plugin.getLocalize().format("cheapest_list_item", new Object[]{
                    d.getName(),
                    fn.toMoney(d.getCurrentValue()),
                    fn.toMoney(d.getCurrentValue() * (1 - d.getProfit())),
                    d.getCurrentQuantity(),
            }));
        });
        plugin.sendTo(who, plugin.getLocalize().format("cheapest_list_footer"));

        return true;
    }
    /**
     * on confirm command
     * confirm transaction
     * @param who player
     * @return true if succeed
     */
    private boolean onConfirm(Player who) {
        if (!confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who, plugin.getLocalize().format("nothing_to_confirm"));
            return true;
        }

        PlayerBeginTransactionEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(who.getUniqueId()); // remove from confirmation

        // Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            event.setCancelled(true);
        else
            completeTransaction(event);

        return true;
    }

    /**
     * on cancel command
     * cancel transaction
     * @param who player
     * @return true if succeed
     */
    private boolean onCancel(Player who) {
        if (!confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who, plugin.getLocalize().format("nothing_to_cancel"));
            return true;
        }

        PlayerBeginTransactionEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(who.getUniqueId()); // remove from confirmation

        plugin.sendTo(who, plugin.getLocalize().format("transaction_cancelled"));

        return true;
    }

    /**
     * on check command
     * check the price of an item
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onCheck(Player who, String[] args) {
        if (args.length < 1)
            return false;

        String item = args[0].toUpperCase();

        if (plugin.getDataHandler().getBlacklist().containsKey(item)) {
            plugin.sendTo(who, plugin.getLocalize().format("item_blacklisted", item));
            return true;
        }

        BankData data = plugin.getDataHandler().getSingle(item);

        if (data == null) {
            plugin.sendTo(who, plugin.getLocalize().format("item_not_available"));
            return true;
        }

        plugin.sendTo(who, plugin.getLocalize().format("check_item_value", new Object[]{
                item,
                fn.toMoney(data.getCurrentValue()),
                fn.toMoney(data.getCurrentValue() * (1 - data.getProfit())),
                data.getCurrentQuantity(),
        }));

        return true;
    }

    /**
     * on sell command
     * perform a sell transaction
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onSell(Player who, String[] args) {
        int qty = -1;
        String item = null;

        switch (args.length) {
            case 0: // sell all item in hand
            case 1: // sell <qty> item in hand
                ItemStack inHand = who.getInventory().getItemInMainHand();
                if (inHand == null || inHand.getType() == Material.AIR)
                    return true;

                item = DataHandler.getNameFrom(inHand);

                qty = item != null ? args.length == 0 ? inHand.getAmount() : fn.toInteger(args[0], -1) : -1;
                break;
            case 2: // sell specific item : lb econ sell item qty
                item = args[0].toUpperCase();
                qty = fn.toInteger(args[1], -1);
                break;
        }

        if (fn.StringIsNullOrEmpty(item) || qty < 1)
            return false;

        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who,plugin.getLocalize().format("confirm_cancel_first"));
            return true;
        }

        if (plugin.getDataHandler().getBlacklist().containsKey(item)) {
            plugin.sendTo(who,plugin.getLocalize().format("item_blacklisted", item));
            return true;
        }

        ArrayList<ItemStack> items = getItems(item, qty);

        if (items == null || items.isEmpty())
            return true;

        Double price = getPrice(items, false);

        if (price == null)
            return true;

        if (!PlayerSellEvent.playerHasItems(who, items.get(0), qty)) {
            plugin.sendTo(who, plugin.getLocalize().format("missing_from_inventory"));
            return true;
        }

        PlayerSellEvent event = new PlayerSellEvent(who, items, price);

        double timeToConfirm = plugin.getConfig().getDouble("time_to_confirm", -1);
        if (timeToConfirm > 0) {
            confirmations.put(who.getUniqueId(), event);

            plugin.sendTo(event.getPlayer(), plugin.getLocalize().format("confirm_sell_now", new Object[]{
                    item,
                    event.getQuantity(),
                    fn.toMoney(event.getPrice())
            }));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (confirmations.containsKey(who.getUniqueId())) {
                        confirmations.remove(who.getUniqueId());
                        plugin.sendTo(who,plugin.getLocalize().format("did_not_confirm"));
                    }
                }
            }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

        } else {
            // Bukkit.getServer().getPluginManager().callEvent(event);
            completeTransaction(event);
        }

        return true;
    }

    /**
     * on buy command
     * perform a buy transaction
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onBuy(Player who, String[] args) {
        if (args.length < 2)
            return false; // /lb econ buy material amount

        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who, plugin.getLocalize().format("confirm_cancel_first"));
            return true;
        }

        String item = args[0].toUpperCase();
        int qty = fn.toInteger(args[1], -1);

        if (plugin.getDataHandler().getBlacklist().containsKey(item)) {
            plugin.sendTo(who, plugin.getLocalize().format("item_blacklisted", item));
            return true;
        }

        ArrayList<ItemStack> items = getItems(item, qty);

        if (items == null || items.isEmpty())
            return true;

        Double price = getPrice(items, true);

        if (price == null)
            return true;

        if (!plugin.getEconHandler().get().hasEnough(who, price)) {
            plugin.sendTo(who, plugin.getLocalize().format("insufficient_funds", price));
            return true;
        }

        PlayerBuyEvent event = new PlayerBuyEvent(who, items, price);

        double timeToConfirm = plugin.getConfig().getDouble("time_to_confirm", -1);
        if (timeToConfirm > 0) {
            confirmations.put(who.getUniqueId(), event);

            plugin.sendTo(event.getPlayer(), plugin.getLocalize().format("confirm_purchase_now", new Object[]{
                    item,
                    event.getQuantity(),
                    fn.toMoney(event.getPrice())
            }));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (confirmations.containsKey(who.getUniqueId())) {
                        confirmations.remove(who.getUniqueId());
                        plugin.sendTo(who,plugin.getLocalize().format("did_not_confirm"));
                    }
                }
            }.runTaskLater(plugin, (long)timeToConfirm * 20); // 20 seconds to confirm before its cancelled
        } else {
            // Bukkit.getServer().getPluginManager().callEvent(event);
            completeTransaction(event);
        }

        return true;
    }

    /**
     * generate list of item stack based on material and quantity
     * @param name string material name
     * @param qty string quantity
     * @return item stacks
     */
    private ArrayList<ItemStack> getItems(String name, int qty) {
        ArrayList<ItemStack> items = new ArrayList<>();

        int quantity = qty;

        if (quantity <= 0 || fn.StringIsNullOrEmpty(name))
            return items;

        BankData data = plugin.getDataHandler().getSingle(name);

        if (data == null)
            return items;

        while(quantity > 0) {
            int amount = quantity;
            if (amount > data.getItemStack().getMaxStackSize())
                amount = data.getItemStack().getMaxStackSize();

            quantity -= amount;

            ItemStack i = new ItemStack(data.getItemStack());
            i.setAmount(amount);

            items.add(i);
        }

        return items;
    }

    /**
     * compute the total price base on itemStacks
     * @param items list of items => itemStacks
     * @return price
     */
    private Double getPrice(ArrayList<ItemStack> items, boolean isBuying) {
        double total = 0.0;
        BankData data = plugin.getDataHandler().getSingle(items.get(0));

        for (ItemStack item : items) {
            double m = !isBuying ? 1 - data.getProfit() : 1;
            total += (item.getAmount() * data.getCurrentValue() * m);
        }

        return total;
    }

    /**
     * update data from transaction event
     * @param e PlayerBeginTransactionEvent
     */
    private void completeTransaction(PlayerBeginTransactionEvent e) {
        if (e.getItemStacks().isEmpty())
            return;

        if (e instanceof PlayerBuyEvent)
            completeBuy((PlayerBuyEvent) e);
        else if (e instanceof PlayerSellEvent)
            completeSell((PlayerSellEvent) e);
    }

    /**
     * update data from sell event
     * @param e PlayerSellEvent
     */
    private void completeSell(PlayerSellEvent e) {
        BankData data = plugin.getDataHandler().getSingle(e.getItemStacks().get(0));

        int qty = e.getQuantity();

        if (!e.check(true) && !e.isBypass()) {
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_cancelled"));
            return;
        }

        BankInfo bank = plugin.getDataHandler().getBank();

        boolean success = plugin.getEconHandler().get().deposit(e.getPlayer(), e.getPrice());

        if (!success) {
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("unable_to_deposit", e.getPrice()));
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_cancelled"));
            return;
        }

        // update data
        data.setCurrentQuantity(data.getCurrentQuantity() + e.getQuantity());
        data.decreaseValueBy(qty * data.getPriceDrop());
        bank.withdraw(e.getPrice());

        // save data
        data.save();
        bank.save();

        int removed = 0;
        ItemStack itemSold = e.getItemStacks().get(0);

        String name = DataHandler.getNameFrom(itemSold);

        // remove item from player's inventory
        for (int i = 0; i < e.getPlayer().getInventory().getSize(); i++) {
            ItemStack itemStack = e.getPlayer().getInventory().getItem(i);

            if (removed >= qty) // no need for further process
                break;

            // itemStack can be null if slot is empty
            if (itemStack == null || itemStack.getType() != itemSold.getType())
                continue;

            int c = 0;

            String compare = DataHandler.getNameFrom(itemStack);

            if (!name.equals(compare))
                continue;

            c = itemStack.getAmount();

            if (c > qty) {
                c = qty;
                itemStack.setAmount(itemStack.getAmount() - c);
            } else {
                e.getPlayer().getInventory().setItem(i, null);
            }
            removed += c;
        }
        e.getPlayer().updateInventory();

        PlayerCompleteTransactionEvent completed = new PlayerCompleteSellEvent(e);

        Bukkit.getServer().getPluginManager().callEvent(completed);
        plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_completed"));
    }

    /**
     * update data from buy event
     * @param e PlayerBuyEvent
     */
    private void completeBuy(PlayerBuyEvent e) {
        BankData data = plugin.getDataHandler().getSingle(e.getItemStacks().get(0));

        int qty = e.getQuantity();

        if (!e.check(true) && !e.isBypass()) {
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_cancelled"));
            return;
        }

        BankInfo bank = plugin.getDataHandler().getBank();

        boolean success = plugin.getEconHandler().get().withdraw(e.getPlayer(), e.getPrice());

        if (!success) {
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("unable_to_withdraw", e.getPrice()));
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_cancelled"));
            return;
        }

        // update data
        data.increaseValueBy(qty * data.getPriceIncrease());
        data.setCurrentQuantity(data.getCurrentQuantity() + qty);
        bank.deposit(e.getPrice());

        // save data
        data.save();
        bank.save();

        PlayerCompleteTransactionEvent completed = new PlayerCompleteBuyEvent(e);

        completed.getItemStacks().forEach(itemStack -> completed.getPlayer().getInventory().addItem(itemStack));
        completed.getPlayer().updateInventory();

        Bukkit.getServer().getPluginManager().callEvent(completed);
        plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_completed"));
    }
}
