package lowbrain.economy.main;

import com.mysql.fabric.xmlrpc.base.Array;
import lowbrain.economy.events.PlayerBuyEvent;
import lowbrain.economy.events.PlayerSellEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CommandHandler implements CommandExecutor {

    private final LowbrainEconomy plugin;

    public CommandHandler(LowbrainEconomy plugin) {
        this.plugin = plugin;
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
        }

        return false;
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

        ArrayList<ItemStack> items = getItems(args);

        if (items.isEmpty())
            return false;

        Double price = getPrice(items);

        if (price == null)
            return false;

        PlayerSellEvent event = new PlayerSellEvent(who, items, price);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            event.setCancelled(true);
        // else
            //update

        return true;
    }

    private boolean onBuy(Player who, String[] args) {
        if (!checkPermission(who, "lbeconn.buy"))
            return false;

        if (args.length < 3)
            return false; // /lbeconn sell material amount

        ArrayList<ItemStack> items = getItems(args);

        if (items.isEmpty())
            return false;

        Double price = getPrice(items);

        if (price == null)
            return false;

        PlayerBuyEvent event = new PlayerBuyEvent(who, items, price);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            event.setCancelled(true);
        // else
            //update

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
