package net.mizukilab.pit.listener;

import net.mizukilab.pit.util.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTrackerUpdateEvent;

public class PandaExtListener implements Listener {
    @EventHandler
    public void onEntryUpdateEvent(EntityTrackerUpdateEvent e){
        e.setCancelled(Utils.isInArena(e.getEntity()) != Utils.isInArena(e.getTrackedEntity()));
    }
}
