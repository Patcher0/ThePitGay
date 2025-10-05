package net.mizukilab.pit.listener;

import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.Utils;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTrackerUpdateEvent;

public class PandaExtListener implements Listener {
    @EventHandler
    public void onEntryUpdateEvent(EntityTrackerUpdateEvent e){
        Entity trackedEntity = e.getTrackedEntity();
        if(trackedEntity instanceof ArmorStand){
            return;
        }
        if(!PlayerUtil.isNPC(e.getTrackedEntity())){
            return;
        }
        e.setCancelled(Utils.isInArena(e.getEntity()) != Utils.isInArena(trackedEntity));
    }
}
