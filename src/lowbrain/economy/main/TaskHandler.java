package lowbrain.economy.main;

import org.bukkit.scheduler.BukkitRunnable;

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

    }

    /**
     * runTaskTimer right away (delay set to zero)
     * @param interval interval
     */
    public void startNow(int interval) {
        this.runTaskTimer(plugin, 0, interval);
    }
}
