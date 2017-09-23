package lowbrain.economy.handlers;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.config.YamlConfig;
import lowbrain.library.fn;
import net.milkbowl.vault.chat.Chat;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Tree;
import org.bukkit.material.Wood;
import org.bukkit.material.WoodenStep;
import org.jetbrains.annotations.Contract;

import java.util.*;

public class DataHandler {
    private LowbrainEconomy plugin;

    private HashMap<String, BankData> data = new HashMap<>();
    private HashMap<String, String> blacklist = new HashMap<>();

    private BankInfo bank;

    public DataHandler(LowbrainEconomy plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getLogger().info("loading data info's");
        this.loadBlacklist();
        this.loadBank();
        this.loadData();
        plugin.getLogger().info("loading complete");
    }

    public void save() {
        this.saveBank();
        this.saveData();
    }

    public List<BankData> cheapest () {
        Comparator<BankData> comparator = new Comparator<BankData>() {
            @Override
            public int compare(BankData a, BankData b) {
                double va = a.getCurrentValue();
                double vb = b.getCurrentValue();

                return va == vb ? 0 : va > vb ? 1 : -1;
            }
        };

        List<BankData> values = plugin.getDataHandler().getAllAsList();

        Collections.sort(values, comparator);

        return values;
    }

    public List<BankData> pricey () {
        Comparator<BankData> comparator = new Comparator<BankData>() {
            @Override
            public int compare(BankData a, BankData b) {
                double va = a.getCurrentValue();
                double vb = b.getCurrentValue();

                return va == vb ? 0 : va < vb ? 1 : -1;
            }
        };

        List<BankData> values = plugin.getDataHandler().getAllAsList();

        Collections.sort(values, comparator);

        return values;
    }

    public void saveData() {
        getData().values().forEach(bankData -> bankData.save(false));
        plugin.getBankConfig().save();
    }

    public void saveBank() {
        getBank().save();
    }

    public void loadBlacklist() {
        blacklist = new HashMap<>();

        List<String> l = plugin.getConfig().getStringList("blacklisted");

        if (l == null)
            return;

        l.forEach(s -> blacklist.put(s, s));
    }

    public void loadData() {
        data = new HashMap<>();

        for (Material mat : Material.values() ) {
            if (blacklist.containsKey(mat.name()))
                continue;

            data.put(mat.name(), new BankData(mat));
        }

        for (TreeSpecies ts : TreeSpecies.values()) {
            if (ts.equals(TreeSpecies.GENERIC))
                continue;


            String plankName = ts.name() + "_" + Material.WOOD.name();
            String logName = ts.name() + "_" + Material.LOG.name();
            String stepName = ts.name() + "_" + Material.WOOD_STEP.name();

            Wood plank = new Wood(Material.WOOD, ts);
            Tree log = new Tree(Material.LOG, ts);
            WoodenStep step = new WoodenStep(ts);

            ItemStack iPlank = new ItemStack(Material.WOOD, 1);
            iPlank.setData(plank);
            // iPlank.getItemMeta().setDisplayName(plankName);

            ItemStack iLog = new ItemStack(Material.LOG, 1);
            iLog.setData(log);

            ItemStack iStep = new ItemStack(Material.WOOD_STEP, 1);
            iStep.setData(step);
            // iLog.getItemMeta().setDisplayName(logName);

            this.add(plankName, iPlank);
            this.add(logName, iLog);
            this.add(stepName, iStep);
        }
    }

    public void loadBank() {
        bank = new BankInfo();
    }

    public BankInfo getBank() {
        return bank;
    }

    public HashMap<String, String> getBlacklist() {
        return blacklist;
    }

    public HashMap<String, BankData> getData() {
        return data;
    }

    public BankData getSingle(ItemStack i) {
        if (i == null)
            return null;

        String name = i.hasItemMeta() && !fn.StringIsNullOrEmpty(i.getItemMeta().getDisplayName())
                ? i.getItemMeta().getDisplayName()
                : i.getType().name();

        return getData().get(name);
    }

    public BankData getSingle(String n) {
        n = ChatColor.stripColor(n);
        return getData().get(n);
    }

    public List<BankData> getAllAsList() {
        return new ArrayList<BankData>(getData().values());
    }

    public void add(String name, ItemStack itemStack) {
        if (fn.StringIsNullOrEmpty(name))
            throw new NullArgumentException("name");
        if (itemStack == null)
            throw new NullArgumentException("itemStack");

        name = ChatColor.stripColor(name.toUpperCase());

        getData().put(name, new BankData(name, itemStack));
    }

    @Contract(value = "null -> null", pure = true)
    public static String getNameFrom(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        String item = "";

        // try to use custom item name
        if (itemStack.hasItemMeta() && !fn.StringIsNullOrEmpty(itemStack.getItemMeta().getDisplayName())) {
            item = itemStack.getItemMeta().getDisplayName();
        } else if (itemStack.getType() == Material.WOOD_STEP && itemStack.getData() instanceof WoodenStep) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((WoodenStep) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        } else if (itemStack.getType() == Material.WOOD_DOUBLE_STEP && itemStack.getData() instanceof WoodenStep) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((WoodenStep) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        } else if (itemStack.getType() == Material.LOG && itemStack.getData() instanceof Tree) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((Tree) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        } else if (itemStack.getType() == Material.WOOD && itemStack.getData() instanceof Wood){
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((Wood)itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : (((Wood) itemStack.getData()).getSpecies().name()) + "_";
            item = prefix + suffix;
        } else { // regular item
            item = itemStack.getType().name();
        }

        return ChatColor.stripColor(item.toUpperCase());
    }
}
