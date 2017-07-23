package lowbrain.economy.main;

import lowbrain.economy.events.PlayerBuyEvent;
import lowbrain.economy.events.PlayerSellEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EconomyListener implements Listener {
    private  final LowbrainEconomy plugin;

    public EconomyListener(LowbrainEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBought(PlayerBuyEvent e) {
        if (e.isCancelled())
            return;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSold(PlayerSellEvent e) {
        if (e.isCancelled())
            return;
    }
}
