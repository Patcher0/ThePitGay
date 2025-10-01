package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import lombok.experimental.UtilityClass;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.IntConsumer;

@UtilityClass
public class LzScheduler {
    public BukkitTask runTaskWithFixedExecutingCount(int dur,int delay,int count, Int2BooleanFunction consumer){
        return new BukkitRunnable() {
            int countLocal = 0;
            @Override
            public void run() {
                boolean b = consumer.get(countLocal);
                if(b && countLocal >= count){
                    this.cancel();
                    return;
                }
                countLocal++;
            }
        }.runTaskTimerAsynchronously(ThePit.getInstance(),delay,dur);
    }
}
