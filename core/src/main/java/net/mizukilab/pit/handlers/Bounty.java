package net.mizukilab.pit.handlers;

import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import io.irina.backports.utils.SWMRHashTable;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.mizukilab.pit.config.NewConfiguration;
import net.mizukilab.pit.util.Einstein;
import net.mizukilab.pit.util.PlayerUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.cooldown.Cooldown;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 17:33
 */
@Skip
public class Bounty extends BukkitRunnable {
    long tick = 0;
    private final SWMRHashTable<UUID, AnimationData> animationDataMap = new SWMRHashTable<>();

    public void delete(Collection<HologramDisplay> sets) {
        sets.removeIf(holo -> {
            if (!holo.hologram.isSpawned()) {
                return false;
            }
            holo.hologram.deSpawn();
            return true;
        });
    }
    public void removeInvalidHolograms(){
        animationDataMap.removeIf((i, a) -> {
            Player player = Bukkit.getPlayer(i);
            if (player == null || !player.isOnline()) {
                if (a != null) {
                    delete(a.holograms);
                    return a.holograms.isEmpty();
                } else {
                    return true;
                }
            }
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
            boolean b = profile.getBounty() < 500;
            if(b && a != null){
                delete(a.holograms);
            }
            return b;
        });
    }
    @Override
    public void run() {
        tick++;
        tick();
        removeInvalidHolograms();
    }
    public void tick(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uniqueId = player.getUniqueId();
            PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(uniqueId);
            int bounty = profile.getBounty();
            if (bounty >= 500) {
                AnimationData animationData = animationDataMap.computeIfAbsent(uniqueId, i -> new AnimationData());
                String color = profile.bountyColor();
                playAnimation(player,animationData, bounty, color);
            }
        }
    }

    int dist = Bukkit.getViewDistance();
    @SneakyThrows
    private void playAnimation(Player player,AnimationData animationData, int bounty, String color) {
        List<HologramDisplay> holograms = getAndRemoveHologramData(player, animationData);
        if (holograms.size() < 3) {
            Location playerLocation = player.getLocation();
            double x = generatorLocDouble();
            double z = generatorLocDouble();
            Hologram newHologram = HologramAPI.createHologram(playerLocation.add(x, 0.1, z), CC.translate(color + "&l" + bounty + "g"));

            var reviewers = new LinkedList<>(Bukkit.getOnlinePlayers());
            if (!player.hasPermission("pit.admin")) {
                reviewers.remove(player);
            }
            reviewers.removeIf(target -> shouldRemove(player,target));
            newHologram.spawn(reviewers);
            holograms.add(new HologramDisplay(newHologram, x, z));

            animationData.setSpawnCooldown(new Cooldown(650));
        }
    }
    public boolean shouldRemove(Player player, Player in){
        PlayerProfile playerProfileByUuid = PlayerProfile.getPlayerProfileByUuid(in.getUniqueId());
        PlayerProfile playerProfileByUuid2 = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if ((NewConfiguration.INSTANCE.getDynamicInvisible() &&
                (playerProfileByUuid.isInArena() != playerProfileByUuid2.isInArena()))) {
            return true;
        }

        float distance = PlayerUtil.getDistanceSQ(in, player);
        int viewDistance = dist;
        int value = viewDistance - 1;
        int i = Einstein.clampi(value,0,viewDistance) << 4;
        if (distance > (i * i)) {
            return true;
        }

        boolean bountyHiddenWhenNear = playerProfileByUuid
                .getPlayerOption().isBountyHiddenWhenNear();
        return bountyHiddenWhenNear && distance < 64;
    }

    @NotNull
    private List<HologramDisplay> getAndRemoveHologramData(Player player, AnimationData animationData) {
        List<HologramDisplay> holograms = animationData.getHolograms();
        holograms.removeIf(hologram -> {
            if (System.currentTimeMillis() > hologram.endTime) {
                if(hologram.hologram.isSpawned()) {
                    hologram.hologram.deSpawn();
                }
                return hologram.hologram.isFullyDespawned();
            } else if(hologram.hologram.isSpawned()){
                if(tick % 2 ==0) {
                    for (Player member : hologram.hologram.members()) {
                        if (shouldRemove(player, member)) {
                            hologram.hologram.hide(member);
                        }
                    }
                    Location location = player.getLocation().clone();
                    location.setX(location.getX() + hologram.boostX);
                    Hologram hologram1 = hologram.getHologram();
                    Location location1 = hologram1.getLocation();
                    location.setY(location1.getY() + (0.1 * Math.max(1, NewConfiguration.INSTANCE.getBountyTickInterval())));
                    location.setZ(location.getZ() + hologram.boostZ);
                    hologram1.setLocation(location);
                }
                return false;
            }
            return false;
        });
        return holograms;
    }

    private double generatorLocDouble() {
        return (Math.random() * 2) - 1;
    }

    @Getter
    @Setter
    public static class AnimationData {

        private final List<HologramDisplay> holograms;
        private Cooldown spawnCooldown;

        public AnimationData() {
            this.holograms = Collections.synchronizedList(new LinkedList<>());
            this.spawnCooldown = new Cooldown(0);
        }


    }

    @Getter
    public static class HologramDisplay {

        private final Hologram hologram;
        private final double boostX;
        private final double boostZ;
        private final long endTime;

        public HologramDisplay(Hologram hologram, double boostX, double boostZ) {
            this.hologram = hologram;
            this.boostX = boostX;
            this.boostZ = boostZ;
            this.endTime = System.currentTimeMillis() + 2000;
        }
    }
}
