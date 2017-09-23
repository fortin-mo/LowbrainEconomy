package lowbrain.economy.handlers;

import lowbrain.economy.main.LowbrainEconomy;
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
        long mili = config.getInt("overtime_diff_drop",1440) * 60 * 1000;

        long today = (new Date()).getTime();

        // for internal data
        plugin.getDataHandler().getData().values().forEach(d -> {
            Date date = d.getLastBought();
            long time = date != null ? date.getTime() : Long.MIN_VALUE;

            if (time < today - mili)
                d.decreaseValueBy(d.getOvertimePriceDrop());
        });

        plugin.getDataHandler().save();
    }

    /**
     * runTaskTimer right away (delay set to zero)
     * @param interval interval in minutes
     */
    public void startNow(int interval) {
        int ticks = interval * 60 * 20;
        this.runTaskTimer(plugin, ticks, ticks);
    }
}
