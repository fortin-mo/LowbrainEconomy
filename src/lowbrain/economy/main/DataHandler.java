package lowbrain.economy.main;

import org.bukkit.Material;

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

        List<BankData> values = new ArrayList<BankData>(getData().values());

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

        List<BankData> values = new ArrayList<BankData>(getData().values());

        Collections.sort(values, comparator);

        return values;
    }

    public void saveData() {
        getData().values().forEach(bankData -> bankData.save());
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
}
