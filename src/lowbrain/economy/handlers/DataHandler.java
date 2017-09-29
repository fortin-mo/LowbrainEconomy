package lowbrain.economy.handlers;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.config.YamlConfig;
import lowbrain.library.fn;
import net.milkbowl.vault.chat.Chat;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.*;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Comparator;

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

            // check for additional sub types
            if (MATERIAL_TYPES.containsKey(mat.getId())) {
                Object value = MATERIAL_TYPES.get(mat.getId());

                if (value instanceof Integer)
                    for (int i = 1; i <= (int)value; i++)
                        addType(mat, mat.name() + ":" + i, (byte)i);
                else  if (value instanceof int[])
                    for (int _id : (int[])value)
                        addType(mat, mat.name() + ":" + _id, (byte)_id);
            }

            if (blacklist.containsKey(mat.name()))
                continue;

            data.put(mat.name(), new BankData(mat));
        }
    }
    private void addType(Material mat, String name, byte type) {
        if (blacklist.containsKey(name))
            return;

        ItemStack i = new ItemStack(mat, 1, type);
        MaterialData d = i.getData();
        d.setData(type);
        i.setData(d);
        i.setDurability(type);
        data.put(name, new BankData(name, i));
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

    /*public BankData getSingle(ItemStack i) {
        if (i == null)
            return null;

        String name = i.hasItemMeta() && !fn.StringIsNullOrEmpty(i.getItemMeta().getDisplayName())
                ? i.getItemMeta().getDisplayName()
                : i.getType().name();

        return getData().get(name);
    }*/

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
        }

        else if (itemStack.getType() == Material.WOOD_STEP && itemStack.getData() instanceof WoodenStep) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((WoodenStep) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        }

        else if (itemStack.getType() == Material.WOOD_DOUBLE_STEP && itemStack.getData() instanceof WoodenStep) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((WoodenStep) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        }

        else if (itemStack.getType() == Material.LOG && itemStack.getData() instanceof Tree) {
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((Tree) itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : ((Wood) itemStack.getData()).getSpecies().name();
            item = prefix + suffix;
        }

        else if (itemStack.getType() == Material.WOOD && itemStack.getData() instanceof Wood){
            String suffix = ((Wood)itemStack.getData()).getItemType().name();
            String prefix = ((Wood)itemStack.getData()).getSpecies() == TreeSpecies.GENERIC ? "" : (((Wood) itemStack.getData()).getSpecies().name()) + "_";
            item = prefix + suffix;
        }

        else { // regular item
            item = itemStack.getType().name();
        }

        return ChatColor.stripColor(item.toUpperCase());
    }

    public BankData getSingle(ItemStack i) {
        if (i == null)
            return null;

        return getData().get(this.nameFrom(i));
    }

    public String nameFrom(ItemStack i) {
        if (i == null || i.getType() == Material.AIR)
            return "";

        Material mat = i.getType();
        int id = mat.getId();
        String name = mat.name();

        if (hasTypes(mat) && i.getData().getData() > 0)
            name += ":" + i.getData().getData();

        return name;
    }

    public void generateOrderedData() throws IOException, InvalidConfigurationException {
        File file = new File(plugin.getDataFolder(), "generated-data.yml");

        if (!file.exists()) {
            file.getParentFile().mkdir();
            file.createNewFile();
        }

        FileConfiguration yml = new YamlConfiguration();
        yml.load(file);

        Comparator<Material> comparator = new Comparator<Material>() {
            @Override
            public int compare(Material a, Material b) {
                double va = a.getId();
                double vb = b.getId();

                return va == vb ? 0 : va < vb ? -1 : 1;
            }
        };

        Material[] values = Material.values();
        Arrays.sort(values, comparator);

        for (Material mat : values ) {

            ConfigurationSection sec = yml.createSection(mat.name());
            sec.set("current_value", 5);
            sec.set("current_quantity", 100);

            // check for additional sub types
            if (MATERIAL_TYPES.containsKey(mat.getId())) {
                Object value = MATERIAL_TYPES.get(mat.getId());

                if (value instanceof Integer)
                    for (int i = 1; i <= (int)value; i++) {
                        ConfigurationSection subsec= yml.createSection(mat.name() + ":" + i);
                        subsec.set("current_value", 5);
                        subsec.set("current_quantity", 100);
                    }
                else  if (value instanceof int[])
                    for (int _id : (int[])value) {
                        ConfigurationSection subsec= yml.createSection(mat.name() + ":" + _id);
                        subsec.set("current_value", 5);
                        subsec.set("current_quantity", 100);
                    }
            }
        }
        yml.save(file);
    }

    public static boolean hasTypes(Material mat) {
        return DataHandler.MATERIAL_TYPES.containsKey(mat.getId());
    }

    public final static HashMap<Integer, Object> MATERIAL_TYPES = new HashMap<Integer, Object>() {{
        put(1,6);
        put(3,2);
        put(5,5);
        put(6,5);
        put(12,1);
        put(17,3);
        put(18,3);
        put(19,1);
        put(24,2);
        put(31,2);
        put(35,15);
        put(38,8);
        put(43,7);
        put(44,7);
        put(95,15);
        put(97,5);
        put(98,3);
        put(125,5);
        put(126,5);
        put(139,1);
        put(155,2);
        put(159,15);
        put(160,15);
        put(161,1);
        put(162,1);
        put(168,2);
        put(171,15);
        put(175,5);
        put(179,2);
        put(251,15);
        put(252,15);
        put(263,1);
        put(322,1);
        put(349,3);
        put(350,1);
        put(351,15);
        put(383,new int[]{4,5,6,23,27,28,29,31,32,34,35,36,50,51,52,54,55,56,57,58,59,60,61,62,65,66,67,68,69,90,91,92,93,94,95,96,98,100,101,102,103,120});
        put(397,5);
        put(373, new int[]{16,32,64,8193,8194,8195,8196,8197,8198,8200,8201
                ,8202,8203,8204,8205,8206,8225,8226,8228,8229,8233,8235
                ,8236,8257,8258,8259,8260,8262,8264,8265,8266,8269,8270
                ,16385,16386,16387,16388,16389,16390,16392,16393,16934
                ,16395,16396,16397,16398,16417,16418,16420,16421,16425
                ,16427,16428,16449,16450,16451,16452,16454,16456,16457
                ,16458,16461,16462,});
        put(425, 15);
    }};
}
