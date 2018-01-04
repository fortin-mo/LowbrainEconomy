package lowbrain.economy.handlers;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.events.*;
import lowbrain.economy.main.Helper;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.command.Command;
import lowbrain.library.fn;
import lowbrain.library.main.LowbrainLibrary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CommandHandler extends Command {

    private final LowbrainEconomy plugin;
    private HashMap<UUID, PlayerBeginTransactionEvent> confirmations;
    // commands
    private Command onPricey;
    private Command onCheapest;
    private Command onConfirm;
    private Command onCancel;
    private Command onWorth;
    private Command onSell;
    private Command onBuy;
    private Command onSave;
    private Command onSetValue;
    private Command onBank;
    private Listener listener;


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

        this.listener = new Listener() {
            /*@EventHandler
            public void onPlayerItemHeldEvent(PlayerItemHeldEvent e) {
                if (confirmations.containsKey(e.getPlayer().getUniqueId())) {
                    plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("cannot_switch_hand"));
                    onCancel(e.getPlayer());
                }
            }*/

            public void onPlayerDisconnect(PlayerQuitEvent e) {
                if (confirmations.containsKey(e.getPlayer().getUniqueId())) {
                    PlayerBeginTransactionEvent event = confirmations.get(e.getPlayer().getUniqueId());
                    if (event instanceof PlayerSellEvent)
                        hasCancelled((PlayerSellEvent)event);
                }
            }
        };
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
        msg += "\n/lb econ worth <item> : check item current value";
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

        // register cancel command
        this.register("cancel", onCancel = new Command("cancel") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onCancel((Player)who) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });

        // register check command
        this.register("worth", onWorth = new Command("worth") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                return onWorth((Player)who, args) ? CommandStatus.VALID : CommandStatus.INVALID;
            }
        });
        onWorth.addPermission("lb.econ.worth");

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

        this.register("setvalue", onSetValue = new Command("setvalue") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                plugin.getDataHandler().save();

                if (args.length != 2)
                    return CommandStatus.INVALID;

                String item = args[0].toUpperCase();

                item = Helper.getItemFullName(item);

                if (item.isEmpty()) {
                    plugin.sendTo(who, plugin.getLocalize().format("invalid_item_name"));
                    return CommandStatus.VALID;
                }

                BankData data = plugin.getDataHandler().getSingle(item);

                if (data == null) {
                    plugin.sendTo(who, plugin.getLocalize().format("item_not_available"));
                    return CommandStatus.VALID;
                }

                double value = fn.toDouble(args[1], -1);

                if (value < 0) {
                    plugin.sendTo(who, plugin.getLocalize().format("invalid_value"));
                    who.sendMessage("invalid value");
                    return CommandStatus.VALID;
                }

                if (value < data.getMinValue() || value > data.getMaxValue()) {
                    plugin.sendTo(who, plugin.getLocalize().format("value_out_of_range", new Object[] {data.getMinValue(), data.getMaxValue()}));
                    return CommandStatus.VALID;
                }

                data.setCurrentValue(value);

                plugin.sendTo(who, plugin.getLocalize().format("value_set_to", new Object[] {data.getName(), data.getCurrentValue()}));

                return CommandStatus.VALID;
            }
        });
        onSetValue.addPermission("lb.econ.setvalue");

        this.register("bank", onBank = new Command("bank") {
            private Command onBalance;
            private Command onDeposit;
            private Command onWithdraw;
            {
                this.register("balance", onBalance = new Command("balance") {
                    @Override
                    public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                        BankInfo b = plugin.getDataHandler().getBank();
                        switch (args.length) {
                            case 0:
                                if (!who.hasPermission("lb.core.bank.balance")) {
                                    plugin.sendTo(who, plugin.getLocalize().format("insufficient_permission"));
                                    return CommandStatus.VALID;
                                }
                                plugin.sendTo(who, plugin.getLocalize()
                                        .format("bank_current_balance", fn.toMoney(b.getCurrentBalance())));
                                break;
                            case 1:
                                if (!who.hasPermission("lb.core.bank.balance.set")) {
                                    plugin.sendTo(who, plugin.getLocalize().format("insufficient_permission"));
                                    return CommandStatus.VALID;
                                }

                                Double balance = fn.toDouble(args[1], null);

                                if (balance == null || balance < b.getMinBalance() || balance > b.getMaxBalance()) {
                                    plugin.sendTo(who, plugin.getLocalize().format("invalid_balance_amount"));
                                    return CommandStatus.VALID;
                                }

                                b.setCurrentBalance(balance);
                                plugin.sendTo(who, plugin.getLocalize().format("bank_balance_set_to", balance));
                                break;
                        }
                        return CommandStatus.VALID;
                    }
                });

                this.register("deposit", onDeposit = new Command("deposit") {
                    @Override
                    public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                        if (args.length == 0)
                            return CommandStatus.VALID;

                        double amount = fn.toDouble(args[0], -1);

                        if (amount < 1) {
                            plugin.sendTo(who, plugin.getLocalize().format("invalid_deposit_amount"));
                            return CommandStatus.VALID;
                        }

                        BankInfo b = plugin.getDataHandler().getBank();
                        if (b.getCurrentBalance() + amount > b.getMaxBalance()) {
                            plugin.sendTo(who, plugin.getLocalize().format("deposit_exceeds_max_balance", b.getMaxBalance()));
                            return CommandStatus.VALID;
                        }
                        b.deposit(amount);
                        plugin.sendTo(who, plugin.getLocalize().format("deposit_complete", amount));
                        return CommandStatus.VALID;
                    }
                });
                onDeposit.addPermission("lb.core.bank.deposit");

                this.register("withdraw", onWithdraw = new Command("withdraw") {
                    @Override
                    public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                        if (args.length == 0)
                            return CommandStatus.VALID;

                        double amount = fn.toDouble(args[0], -1);

                        if (amount < 1) {
                            plugin.sendTo(who, plugin.getLocalize().format("invalid_withdraw_amount"));
                            return CommandStatus.VALID;
                        }

                        BankInfo b = plugin.getDataHandler().getBank();
                        if (b.getCurrentBalance() - amount < b.getMinBalance()) {
                            plugin.sendTo(who, plugin.getLocalize().format("withdraw_exceeds_min_balance"));
                            return CommandStatus.VALID;
                        }
                        b.withdraw(amount);
                        plugin.sendTo(who, plugin.getLocalize().format("withdraw_complete", amount));
                        return CommandStatus.VALID;
                    }
                });
                onWithdraw.addPermission("lb.core.bank.withdraw");
            }

            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                String msg = ChatColor.WHITE + "/lb econ bank <cmd> <...args>";
                msg += "\n/lb econ bank balance : check global bank balance";
                msg += "\n/lb econ bank balance <[amount]> : set global bank balance";
                msg += "\n/lb econ bank deposit <amount> : add money to the bank";
                msg += "\n/lb econ bank withdraw <amount> : remove money from the bank";
                who.sendMessage(msg);
                return CommandStatus.VALID;
            }
        });
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

        int index = 0;
        for (BankData d :
                list) {

            if (index == limit)
                break;

            plugin.sendTo(who, plugin.getLocalize().format("pricey_list_item", new Object[]{
                    d.getName(),
                    fn.toMoney(d.getCurrentValue()),
                    fn.toMoney(d.getCurrentValue() * (1 - d.getProfit())),
                    d.getCurrentQuantity(),
            }));
            index++;
        }

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

        int index = 0;
        for (BankData d :
                list) {

            if (index == limit)
                break;

            plugin.sendTo(who, plugin.getLocalize().format("cheapest_list_item", new Object[]{
                    d.getName(),
                    fn.toMoney(d.getCurrentValue()),
                    fn.toMoney(d.getCurrentValue() * (1 - d.getProfit())),
                    d.getCurrentQuantity(),
            }));
            index++;
        }

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

        if (event instanceof PlayerSellEvent)
            hasCancelled((PlayerSellEvent)event);

        return true;
    }

    /**
     * on check command
     * check the price of an item
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onWorth(Player who, String[] args) {
        String item = "";

        if (args.length == 0) {
            ItemStack inHand = who.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                plugin.sendTo(who, plugin.getLocalize().format("no_item_in_hand"));
                return true;
            }
            item = plugin.getDataHandler().nameFrom(inHand);
        } else {
            item = Helper.getItemFullName(args[0].toUpperCase());
        }

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
        ItemStack inHand = who.getInventory().getItemInMainHand();

        if (inHand == null || inHand.getType() == Material.AIR) {
            plugin.sendTo(who, plugin.getLocalize().format("no_item_in_hand"));
            return true;
        }

        switch (args.length) {
            case 0:
                qty = inHand.getAmount();
                break;
            case 1:
                qty = Math.max(0, fn.toInteger(args[0], 1));
                qty = qty > inHand.getAmount() ? inHand.getAmount() : qty;
                break;
            default:
                plugin.sendTo(who, "usage : /lb econ sell <[+qty]>");
                return true;
        }

        if (qty <= 0) {
            plugin.sendTo(who, "usage : /lb econ sell <[+qty]>");
            return true;
        }

        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who,plugin.getLocalize().format("confirm_cancel_first"));
            return true;
        }

        /*
        if (plugin.getDataHandler().getBlacklist().containsKey(item)) {
            plugin.sendTo(who,plugin.getLocalize().format("item_blacklisted", item));
            return true;
        }

        ArrayList<ItemStack> items = getItems(item, qty);

        if (items == null || items.isEmpty())
            return true;
        */

        BankData iData = plugin.getDataHandler().getSingle(inHand);

        if (iData == null) {
            plugin.sendTo(who, plugin.getLocalize().format("not_saleable"));
            return true;
        }

        double devaluation = 1; // none for now

        if (plugin.getConfig().getBoolean("allow_damaged_item", true)) {
            devaluation = plugin.getConfig().getBoolean("devalue_damaged_item", true)
                    && inHand.getType().getMaxDurability() > 0
                    && inHand.getDurability() != 0
                        ? (inHand.getType().getMaxDurability() - inHand.getDurability()) / inHand.getType().getMaxDurability()
                        : 1;
        } else if (inHand.getType().getMaxDurability() > 0
                && inHand.getDurability() != 0) { // 0 = never used
            plugin.sendTo(who, plugin.getLocalize().format("cannot_sell_damaged_item"));
            return true;
        }

        double price = iData.getCurrentValue() * qty * devaluation;

        /*
        Double price = getPrice(items, false);

        if (price == null)
            return true;

        if (!PlayerSellEvent.playerHasItems(who, items.get(0), qty)) {
            plugin.sendTo(who, plugin.getLocalize().format("missing_from_inventory"));
            return true;
        }
        */

        ItemStack selling = new ItemStack(inHand);
        selling.setAmount(qty);

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.add(selling);

        PlayerSellEvent event = new PlayerSellEvent(who, items, price);

        if (!event.check(true))
            return true;

        double timeToConfirm = plugin.getConfig().getDouble("time_to_confirm", -1);
        if (timeToConfirm > 0) {

            if (qty == inHand.getAmount())
                who.getInventory().setItemInMainHand(null); // remove item
            else
                who.getInventory().getItemInMainHand().setAmount(inHand.getAmount() - qty);

            confirmations.put(who.getUniqueId(), event);

            plugin.sendTo(event.getPlayer(), plugin.getLocalize().format("confirm_sell_now", new Object[]{
                    inHand.getType().name(),
                    event.getQuantity(),
                    fn.toMoney(event.getPrice())
            }));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (confirmations.containsKey(who.getUniqueId())) {
                        PlayerSellEvent e = (PlayerSellEvent) confirmations.get(who.getUniqueId());
                        confirmations.remove(who.getUniqueId());
                        hasCancelled(e);
                        plugin.sendTo(who,plugin.getLocalize().format("did_not_confirm"));
                    }
                }
            }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

        } else {
            if (qty == inHand.getAmount())
                who.getInventory().setItemInMainHand(null); // remove item
            else
                who.getInventory().getItemInMainHand().setAmount(inHand.getAmount() - qty);
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
        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who, plugin.getLocalize().format("confirm_cancel_first"));
            return true;
        }

        String item = args[0].toUpperCase();

        item = Helper.getItemFullName(item);

        // get quantity from second arguments, if not, default to 1
        int qty = args.length > 1 ? fn.toInteger(args[1], -1) : 1;

        if (qty < 1) {
            plugin.sendTo(who, plugin.getLocalize().format("invalid_quantity"));
            return true;
        }

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
        confirmations.remove(e.getPlayer().getUniqueId());
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
            hasCancelled(e);
            return;
        }

        BankInfo bank = plugin.getDataHandler().getBank();

        boolean success = plugin.getEconHandler().get().deposit(e.getPlayer(), e.getPrice());

        if (!success) {
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("unable_to_deposit", e.getPrice()));
            plugin.sendTo(e.getPlayer(), plugin.getLocalize().format("transaction_cancelled"));
            hasCancelled(e);
            return;
        }

        // update data
        data.setCurrentQuantity(data.getCurrentQuantity() + e.getQuantity());
        data.decreaseValueBy(qty * data.getPriceDrop());
        data.setLastSold(new Date());
        bank.withdraw(e.getPrice());

        // save data
        data.save();
        bank.save();

        int removed = 0;
        ItemStack itemSold = e.getItemStacks().get(0);

        String name = DataHandler.getNameFrom(itemSold);

        // remove item from player's inventory
        /*
        for (int i = 0; i < e.getPlayer().getInventory().getSize(); i++) {
            ItemStack itemStack = e.getPlayer().getInventory().getItem(i);

            if (removed >= qty) // no need for further process
                break;

            // itemStack can be null if slot is empty
            if (itemStack == null || itemStack.getType() != itemSold.getType())
                continue;

            int c = 0;

            // String compare = DataHandler.getNameFrom(itemStack);

            // if (!name.equals(compare))
            //     continue;

            if (!fn.same(itemSold, itemStack))
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
        */


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
        data.setLastBought(new Date());
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

    private void hasCancelled(PlayerSellEvent e) {
        // give items back to player if transaction was cancelled
        for (ItemStack i : e.getItemStacks())
            e.getPlayer().getInventory().addItem(i);

        e.getPlayer().updateInventory();
    }
}
