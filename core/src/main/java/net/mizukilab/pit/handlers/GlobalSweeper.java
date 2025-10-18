package net.mizukilab.pit.handlers;

import cn.charlotte.pit.data.sub.DroppedEntityData;
import cn.charlotte.pit.data.sub.PlacedBlockData;
import io.irina.backports.utils.SWMRHashTable;
import lombok.Getter;
import net.mizukilab.pit.util.RangedStreamLineList;
import net.mizukilab.pit.util.cooldown.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 22:12
 */
@Getter
public class GlobalSweeper extends BukkitRunnable {

    private static GlobalSweeper INSTANCE;
    public final SWMRHashTable<Location, PlacedBlockData> placedBlock;
    private final RangedStreamLineList<DroppedEntityData> entityData;

    public GlobalSweeper() {
        INSTANCE = this;
        this.placedBlock = new SWMRHashTable<>();
        this.entityData = new RangedStreamLineList<>(0,i -> i.getTimer().hasExpired()) {
            @Override
            public void onRecycle(DroppedEntityData droppedEntityData) {
                droppedEntityData.getEntity().remove();
            }
        };
    }

    @Override
    public void run() {
        this.placedBlock.removeIf((i, a) -> {
            if (a.getCooldown().hasExpired()) {
                Location location = a.getLocation();
                location.getBlock().setType(Material.AIR);
                return true;
            }
            return false;
        });
    }

    public void placeBlock(Location location) {
        this.placeBlock(location, new Cooldown(360, TimeUnit.SECONDS));
    }

    public void placeBlock(Location location, Cooldown cooldown) {
        this.placedBlock.put(location, new PlacedBlockData(location, cooldown));
    }

    public static GlobalSweeper get() {
        return INSTANCE;
    }
}
