package net.mizukilab.pit.util;

import cn.charlotte.pit.ThePit;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@UtilityClass
public class Einstein {
    Vector vector = new Vector(0,0,0);
    public double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
    public float clampf(float value, float lowerBound, float upperBound) {
        if (value < lowerBound) {
            return lowerBound;
        } else {
            return Math.min(value, upperBound);
        }
    }
    public int clampi(int value, int lowerBound, int upperBound) {
        if (value < lowerBound) {
            return lowerBound;
        } else {
            return Math.min(value, upperBound);
        }
    }
    public void noVelocity(Player player){
        if(player instanceof CraftPlayer h) {
            EntityPlayer handle = h.getHandle();
            handle.velocityChanged = false;
            handle.motX = 0;
            handle.motY = 0;
            handle.motZ = 0;
            return;
        }
        player.setVelocity(vector);
    }
    public void flushPos(Player player) {
        if (Bukkit.isPrimaryThread()) {
            player.teleport(player.getLocation());
        } else {
            Bukkit.getScheduler().runTask(ThePit.getInstance(),() -> flushPos(player));
        }
    }
}
