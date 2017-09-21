package lowbrain.economy.handlers;

import lowbrain.economy.bank.BankData;
import lowbrain.economy.bank.BankInfo;
import lowbrain.economy.main.LowbrainEconomy;
import lowbrain.library.config.YamlConfig;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DataHandler {
    private LowbrainEconomy plugin;

    private HashMap<String, BankData> data = new HashMap<>();
    private HashMap<String, ExternalData> externalData = new HashMap<>();
    private HashMap<String, String> blacklist = new HashMap<>();

    private BankInfo bank;

    public DataHandler(LowbrainEconomy plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getLogger().info("[LowbrainEconomy] loading data infos");
        this.loadBlacklist();
        this.loadBank();
        this.loadData();
        plugin.getLogger().info("[LowbrainEconomy] loading complete");
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
        getExternalData().values().forEach(bankData -> bankData.getBankData().save(false));

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

    public BankData getSingle(String n) {
        ExternalData x = getExternalData().get(n);
        BankData d = null;

        if (x == null)
            d = getData().get(n);
        else
            d = x.getBankData();

        return d;
    }

    public HashMap<String, BankData> getAll() {
        HashMap<String, BankData> all = new HashMap<>();

        getData().values().forEach(d -> all.put(d.getName(), d));
        getExternalData().values().forEach(d -> all.put(d.getName(), d.getBankData()));

        return all;
    }

    public List<BankData> getAllAsList() {
        return new ArrayList<BankData>(getAll().values());
    }

    public HashMap<String, ExternalData> getExternalData() {
        return externalData;
    }

    public void addExternal(String name, ItemStack i) {
        if (name == null || name.isEmpty() || i == null)
            throw new NullArgumentException("Name and ItemStack must not be null !");

        getExternalData().put(name, new ExternalData(name, i));
    }

    public class ExternalData {
        private BankData bankData;
        private String name;
        private ItemStack itemStack;

        public ExternalData(String name, ItemStack i) {
            this.bankData = new BankData(name);
            this.itemStack = i;
            this.itemStack.setAmount(1);
            this.name = name;
        }

        public BankData getBankData() {
            return bankData;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public String getName() {
            return name;
        }
    }
}
