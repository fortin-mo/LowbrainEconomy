package lowbrain.economy.main;

import lowbrain.economy.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    private final LowbrainEconomy plugin;
    private HashMap<UUID, PlayerBeginTransactionEvent> confirmations;

    /**
     * constructor
     * @param plugin
     */
    public CommandHandler(LowbrainEconomy plugin) {
        this.plugin = plugin;
        this.confirmations = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("lbeconn"))
            return false;

        if (!(sender instanceof Player))
            return false;

        if (args.length <= 0)
            return false;

        Player who = (Player)sender;

        switch (args[0].toLowerCase()) {
            case "sell":
                return onSell(who, args);
            case "check":
                return onCheck(who, args);
            case "buy":
                return onBuy(who, args);
            case "confirm":
            case "yes":
                return onConfirm(who);
            case "cancel":
            case "no":
                return onCancel(who);
            case "pricey":
                return onPricey(who, args);
            case "cheapest":
                return onCheapest(who, args);
        }

        return false;
    }

    /**
     * on pricey command
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onPricey(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.pricey"))
            return true;

        int limit = 10;

        if (args.length >= 2) {
            try {
                limit = Integer.parseInt(args[1]);
            } catch (Exception e) {
                limit = 10;
            }
        }

        List<BankData> list = plugin.getDataHandler().pricey();

        plugin.sendTo(who, "---- Pricey items (limit: " + limit + ") ----");
        list.forEach(d -> {
            plugin.sendTo(who, d.getName() + " valued at " + LowbrainEconomy.DECIMAL_FORMAT.format(d.getCurrentValue()) + "$ with " + d.getCurrentQuantity() + " left in stock!");
        });
        plugin.sendTo(who, "---------------------------------------------");

        return true;
    }

    /**
     * on cheapest command
     * @param who player
     * @param args args
     * @return true if succeed
     */
    private boolean onCheapest(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.cheapest"))
            return true;

        int limit = 10;

        if (args.length >= 2) {
            try {
                limit = Integer.parseInt(args[1]);
            } catch (Exception e) {
                limit = 10;
            }
        }

        List<BankData> list = plugin.getDataHandler().cheapest();

        plugin.sendTo(who, "---- Cheapest items (limit: " + limit + ") ----");
        list.forEach(d -> {
            plugin.sendTo(who, d.getName() + " valued at " + LowbrainEconomy.DECIMAL_FORMAT.format(d.getCurrentValue()) + "$ with " + d.getCurrentQuantity() + " left in stock!");
        });
        plugin.sendTo(who, "---------------------------------------------");

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
            plugin.sendTo(who, ChatColor.RED + "You have nothing to confirm yet !");
            return true;
        }

        PlayerBeginTransactionEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(event); // remove from confirmation

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            event.setCancelled(true);
        else
            updateBank(event);

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
            plugin.sendTo(who, ChatColor.RED + "You have nothing to cancel yet !");
            return true;
        }

        PlayerBeginTransactionEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(event); // remove from confirmation

        plugin.sendTo(who, "Your transaction was cancelled !");

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
        if (!checkPermission(who, "lbeconn.check"))
            return false;

        if (args.length < 2)
            return false; // /lbeconn check diamond

        String material = args[1].toUpperCase();

        if (plugin.getDataHandler().getBlacklist().containsKey(material)) {
            plugin.sendTo(who, ChatColor.RED + "This item cannot be sold neither bought !");
            return true;
        }

        BankData data = plugin.getDataHandler().getSingle(material);

        if (data == null) {
            plugin.sendTo(who, ChatColor.RED + "This item is not available !");
            return true;
        }

        plugin.sendTo(who,material + "is currently valued at " + data.getCurrentValue() + "$ and there is " + data.getCurrentQuantity() + " left in the bank");

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
        if (!checkPermission(who, "lbeconn.sell"))
            return false;

        if (args.length < 3)
            return false; // /lbeconn sell material amount

        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who,ChatColor.RED + "You must first confirm or cancel your current transaction !!");
            return true;
        }

        String material = args[1].toUpperCase();
        String qty = args[2];

        if (plugin.getDataHandler().getBlacklist().containsKey(material)) {
            plugin.sendTo(who,ChatColor.RED + "This item cannot be sold neither bought !");
            return true;
        }


        ArrayList<ItemStack> items = getItems(material, qty);

        if (items.isEmpty())
            return true;

        Double price = getPrice(items);

        if (price == null)
            return true;

        PlayerSellEvent event = new PlayerSellEvent(who, items, price);

        confirmations.put(who.getUniqueId(), event);

        plugin.sendTo(event.getPlayer(), "You can now confirm your sell of " + event.getQuantity() + " x " + material + " for a total of " + LowbrainEconomy.DECIMAL_FORMAT.format(event.getPrice()) + "$");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (confirmations.containsKey(who.getUniqueId())) {
                    confirmations.remove(event);
                    plugin.sendTo(who,"You did not confirm your transaction. So it's been cancelled !");
                }
            }
        }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

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
        if (!checkPermission(who, "lbeconn.buy"))
            return false;

        if (args.length < 3)
            return false; // /lbeconn sell material amount

        if (confirmations.containsKey(who.getUniqueId())) {
            plugin.sendTo(who,ChatColor.RED + "You must first confirm or cancel your current sell/buy !!");
            return true;
        }

        String material = args[1].toUpperCase();
        String qty = args[2];

        if (plugin.getDataHandler().getBlacklist().containsKey(material)) {
            plugin.sendTo(who,ChatColor.RED + "This item cannot be sold neither bought !");
            return true;
        }

        ArrayList<ItemStack> items = getItems(material, qty);

        if (items.isEmpty())
            return true;

        Double price = getPrice(items);

        if (price == null)
            return true;

        PlayerBuyEvent event = new PlayerBuyEvent(who, items, price);

        confirmations.put(who.getUniqueId(), event);

        plugin.sendTo(event.getPlayer(), "You can now confirm your purchase of " + event.getQuantity() + " x " + material + " for a total of " + LowbrainEconomy.DECIMAL_FORMAT.format(event.getPrice()) + "$");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (confirmations.containsKey(who.getUniqueId())) {
                    confirmations.remove(event);
                    plugin.sendTo(who,"You did not confirm your purchase. So it's been cancelled !");
                }
            }
        }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

        return true;
    }

    /**
     * generate list of item stack based on material and quantity
     * @param mat string material name
     * @param qty string quantity
     * @return item stacks
     */
    private ArrayList<ItemStack> getItems(String mat, String qty) {
        ArrayList<ItemStack> items = new ArrayList<>();


        int quantity = 0;

        try {
            quantity = Integer.parseInt(qty);
        } catch (Exception e) {
            return items;
        }

        if (quantity <= 0)
            return items;

        Material matariel = Material.getMaterial(mat);

        if (mat == null)
            return items;

        while(quantity > 0) {
            int amount = quantity;
            if (amount > matariel.getMaxStackSize()) {
                quantity -= matariel.getMaxStackSize();
                amount = matariel.getMaxStackSize();
            }
            items.add(new ItemStack(matariel, amount));
        }

        return items;
    }

    /**
     * compute the total price base on itemStacks
     * @param items list of items => itemStacks
     * @return price
     */
    private Double getPrice(ArrayList<ItemStack> items) {
        double total = 0.0;
        BankData data = plugin.getDataHandler().getSingle(items.get(0).getType().name());

        for (ItemStack item :
                items) {
            total += (item.getAmount() * data.getCurrentValue());
        }

        return total;
    }

    /**
     * check user permission
     * @param who player
     * @param permission permission
     * @return access
     */
    private boolean checkPermission(Player who, String permission) {
        if(who.isOp())
            return true;

        if(who.hasPermission(permission))
            return true;

        plugin.sendTo(who, ChatColor.RED + "Insufficient permission !!");
        return false;
    }

    /**
     * update data from transaction event
     * @param e PlayerBeginTransactionEvent
     */
    private void updateBank(PlayerBeginTransactionEvent e) {
        if (e.getItemStacks().isEmpty())
            return;

        if (e instanceof PlayerBuyEvent)
            updateBuy((PlayerBuyEvent) e);
        else if (e instanceof PlayerSellEvent)
            updateSell((PlayerSellEvent) e);
    }

    /**
     * update data from sell event
     * @param e PlayerSellEvent
     */
    private void updateSell(PlayerSellEvent e) {
        BankData data = plugin.getDataHandler().getSingle(e.getItemStacks().get(0).getType().name());

        int qty = e.getQuantity();

        if (e.check(true) && !e.isBypass()) {
            plugin.sendTo(e.getPlayer(), ChatColor.YELLOW + "Your transaction has been cancelled !");
            return;
        }

        BankInfo bank = plugin.getDataHandler().getBank();

        // update data
        data.setCurrentQuantity(data.getCurrentQuantity() + e.getQuantity());
        data.decreaseValueBy(qty * data.getPriceDrop());
        bank.setCurrentAmount(bank.getCurrentAmount() - e.getPrice());

        // save data
        data.save();
        bank.save();

        PlayerCompleteTransactionEvent completed = new PlayerCompleteSellEvent(e);

        Bukkit.getServer().getPluginManager().callEvent(completed);

        if (!completed.isCancelled())
            completed.getItemStacks().forEach(itemStack -> completed.getPlayer().getInventory().addItem(itemStack));
    }

    /**
     * update data from buy event
     * @param e PlayerBuyEvent
     */
    private void updateBuy(PlayerBuyEvent e) {
        BankData data = plugin.getDataHandler().getSingle(e.getItemStacks().get(0).getType().name());

        int qty = e.getQuantity();

        if (e.check(true) && !e.isBypass()) {
            plugin.sendTo(e.getPlayer(), ChatColor.YELLOW + "Your transaction has been cancelled !");
            return;
        }

        BankInfo bank = plugin.getDataHandler().getBank();

        // update data
        data.increaseValueBy(qty * data.getPriceIncrease());
        data.setCurrentQuantity(data.getCurrentQuantity() + qty);
        bank.setCurrentAmount(bank.getCurrentAmount() + e.getPrice());

        // save data
        data.save();
        bank.save();

        PlayerCompleteTransactionEvent completed = new PlayerCompleteBuyEvent(e);

        Bukkit.getServer().getPluginManager().callEvent(completed);

        if (!completed.isCancelled())
            completed.getItemStacks().forEach(itemStack -> completed.getPlayer().getInventory().addItem(itemStack));
    }
}
