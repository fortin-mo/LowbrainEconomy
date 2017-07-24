package lowbrain.economy.main;

import com.mysql.fabric.xmlrpc.base.Array;
import lowbrain.economy.events.PlayerBuyEvent;
import lowbrain.economy.events.PlayerEconEvent;
import lowbrain.economy.events.PlayerSellEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    private final LowbrainEconomy plugin;
    private HashMap<UUID, PlayerEconEvent> confirmations;

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
        }

        return false;
    }

    private boolean onConfirm(Player who) {
        if (!confirmations.containsKey(who.getUniqueId())) {
            who.sendMessage(ChatColor.RED + "You have nothing to confirm yet !");
            return true;
        }

        PlayerEconEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(event); // remove from confirmation

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            event.setCancelled(true);
        // else
            // update data

        return true;
    }

    private boolean onCancel(Player who) {
        if (!confirmations.containsKey(who.getUniqueId())) {
            who.sendMessage(ChatColor.RED + "You have nothing to cancel yet !");
            return true;
        }

        PlayerEconEvent event = confirmations.get(who.getUniqueId());
        confirmations.remove(event); // remove from confirmation

        who.sendMessage("Your sell/buy was cancelled !");

        return true;
    }

    private boolean onCheck(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.check"))
            return false;

        if (args.length < 2)
            return false; // /lbeconn check diamond

        return true;
    }

    private boolean onSell(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.sell"))
            return false;

        if (args.length < 3)
            return false; // /lbeconn sell material amount

        if (confirmations.containsKey(who.getUniqueId())) {
            who.sendMessage(ChatColor.RED + "You must first confirm or cancel your current sell/buy !!");
            return true;
        }

        ArrayList<ItemStack> items = getItems(args);

        if (items.isEmpty())
            return true;

        Double price = getPrice(items);

        if (price == null)
            return true;

        PlayerSellEvent event = new PlayerSellEvent(who, items, price);

        confirmations.put(who.getUniqueId(), event);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (confirmations.containsKey(who.getUniqueId())) {
                    confirmations.remove(event);
                    who.sendMessage("You did not confirm your sale. So it's been cancelled !");
                }
            }
        }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

        return true;
    }

    private boolean onBuy(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.buy"))
            return false;

        if (args.length < 3)
            return false; // /lbeconn sell material amount

        if (confirmations.containsKey(who.getUniqueId())) {
            who.sendMessage(ChatColor.RED + "You must first confirm or cancel your current sell/buy !!");
            return true;
        }

        ArrayList<ItemStack> items = getItems(args);

        if (items.isEmpty())
            return true;

        Double price = getPrice(items);

        if (price == null)
            return true;

        PlayerBuyEvent event = new PlayerBuyEvent(who, items, price);

        confirmations.put(who.getUniqueId(), event);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (confirmations.containsKey(who.getUniqueId())) {
                    confirmations.remove(event);
                    who.sendMessage("You did not confirm your purchase. So it's been cancelled !");
                }
            }
        }.runTaskLater(plugin, 20 * 20); // 20 seconds to confirm before its cancelled

        return true;
    }

    private ArrayList<ItemStack> getItems(String[] args) {
        ArrayList<ItemStack> items = new ArrayList<>();

        String material = args[1].toUpperCase();
        int quantity = 0;

        try {
            quantity = Integer.parseInt(args[1]);
        } catch (Exception e) {
            return items;
        }

        if (quantity <= 0)
            return items;

        Material mat = Material.getMaterial(material);

        if (mat == null)
            return items;

        while(quantity > 0) {
            int amount = quantity;
            if (amount > mat.getMaxStackSize()) {
                quantity -= mat.getMaxStackSize();
                amount = mat.getMaxStackSize();
            }
            items.add(new ItemStack(mat, amount));
        }

        return items;
    }

    private Double getPrice(ArrayList<ItemStack> items) {
        return 100.0;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if(sender.isOp())
            return true;

        if(sender.hasPermission(permission))
            return true;

        sender.sendMessage(ChatColor.RED + "Insufficient permission !!");
        return false;
    }
}
