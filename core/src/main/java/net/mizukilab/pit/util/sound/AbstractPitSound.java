package net.mizukilab.pit.util.sound;

import io.irina.backports.utils.SWMRHashTable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/26 13:32
 */
public abstract class AbstractPitSound {

    private final SWMRHashTable<UUID, Integer> playersTick;

    public AbstractPitSound() {
        this.playersTick = new SWMRHashTable<>();
    }

    public abstract String getMusicInternalName();

    public abstract void onSoundTick(Player player, int tick);

    public void tick() {
        for (Map.Entry<UUID, Integer> entry : playersTick.entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                end(entry.getKey());
                continue;
            }
            Integer tick = entry.getValue();
            this.onSoundTick(player, tick);
            tick++;
            entry.setValue(tick);
        }
    }

    public void play(Player player) {
        this.playersTick.put(player.getUniqueId(), 0);
    }

    public void end(UUID playerUUID) {
        this.playersTick.remove(playerUUID);
    }
    
    public void end(Player player) {
        if (player != null) end(player.getUniqueId());
    }
}
