package lowbrain.economy.main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class TaskHandler extends BukkitRunnable {

    private static LowbrainEconomy plugin;

    /**
     * constructor
     * @param plugin LowbrainEconomy instance
     */
    public TaskHandler(LowbrainEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();

        // minutes to miliseconds
        long mili = config.getInt("time_diff_drop",1440) * 60 * 1000;

        long today = (new Date()).getTime();

        // for internal data
        plugin.getDataHandler().getData().values().forEach(d -> {
            Date date = d.getLastBought();
            long time = date != null ? date.getTime() : Long.MIN_VALUE;

            if (time < today - mili)
                d.decreaseValueBy(d.getDiffPriceDrop());
        });

        // for external data
        plugin.getDataHandler().getExternalData().values().forEach(d -> {
            Date date = d.getBankData().getLastBought();
            long time = date != null ? date.getTime() : Long.MIN_VALUE;

            if (time < today - mili)
                d.getBankData().decreaseValueBy(d.getBankData().getDiffPriceDrop());
        });

        plugin.getDataHandler().save();
    }

    /**
     * runTaskTimer right away (delay set to zero)
     * @param interval interval
     */
    public void startNow(int interval) {
        this.runTaskTimer(plugin, 0, interval);
    }
}
