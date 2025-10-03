package net.mizukilab.pit.hologram;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.event.PitProfileLoadedEvent;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import lombok.Getter;
import net.mizukilab.pit.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/4 13:09
 */
//Being fix
//@AutoRegister
public class HologramMarco implements Listener {
    @Getter
    protected final ConcurrentHashMap<UUID, PlayerHologram> holograms = new ConcurrentHashMap<>();

    @EventHandler
    public void onLoad(PitProfileLoadedEvent event) {
        ThePit.getInstance().getSingleAsyncScheduler().enqueueTask(() -> {
            Player player = Bukkit.getPlayer(event.getPlayerProfile().getPlayerUuid());
            if (player != null && player.isOnline()) {
                PlayerHologram playerHologram = holograms.computeIfAbsent(player.getUniqueId(), i ->  new PlayerHologram(new LinkedList<>()));

                HologramFactory hologramFactory = ThePit.getInstance().getHologramFactory();
                for (AbstractHologram hologram : hologramFactory.loopHologram) {
                    handleHologramCreate(player, playerHologram, hologram);
                }

                for (AbstractHologram hologram : hologramFactory.normalHologram) {
                    handleHologramCreate(player, playerHologram, hologram);
                }
            }
        });
    }

    private void handleHologramCreate(Player player, PlayerHologram playerHologram, AbstractHologram hologram) {
        LinkedList<Hologram> holograms = new LinkedList<>();
        for (int i = 0; i < hologram.getText(player).size(); i++) {
            String text = hologram.getText(player).get(i);
            Hologram holo = HologramAPI.createHologram(hologram.getLocation().clone().add(0, -i * hologram.getHologramHighInterval(), 0), CC.translate(text));
            holo.spawn(Collections.singletonList(player));
            holograms.add(holo);
        }
        playerHologram.hologramData.put(hologram.getInternalName(), new HologramData(holograms, hologram.getInternalName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ThePit.getInstance().getSingleAsyncScheduler().enqueueTask(() -> {
            PlayerHologram playerHologram = holograms.remove(event.getPlayer().getUniqueId());
            if (playerHologram == null) {
                return;
            }
            for (HologramData datum : playerHologram.hologramData.values()) {
                for (Hologram hologram : datum.holograms) {
                    hologram.deSpawn();
                }
            }
        });
    }

    @Data
    public static class PlayerHologram {

        private final Map<String, HologramData> hologramData;

        public PlayerHologram(List<HologramData> hologramData) {
            this.hologramData = new Object2ObjectOpenHashMap<>();
            for (HologramData data : hologramData) {
                this.hologramData.put(data.internalName, data);
            }
        }
    }
    @Data
    public static class HologramData {

        private List<Hologram> holograms;
        private String internalName;

        public HologramData(List<Hologram> holograms, String internalName) {
            this.holograms = holograms;
            this.internalName = internalName;
        }
    }
}
