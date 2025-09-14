package spg.lgdev.handler;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by EmptyIrony on 2021/6/20.
 */
public interface MovementHandler {

    void handleUpdateLocation(Player var1, Location var2, Location var3, PacketPlayInFlying var4);
    default void handleUpdateSpeed(Player var1, AtomicDouble speed,Location in,Location out){

    }
    void handleUpdateRotation(Player var1, Location var2, Location var3, PacketPlayInFlying var4);

}
