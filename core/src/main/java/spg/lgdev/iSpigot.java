package spg.lgdev;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import nya.Skip;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import spg.lgdev.handler.MovementHandler;

import java.util.List;

/**
 * Created by EmptyIrony on 2021/6/20.
 */
@Skip
public class iSpigot implements Listener {
    public static iSpigot INSTANCE;
    private final List<MovementHandler> movementHandlers = new ObjectArrayList<>();

    public iSpigot() {
        INSTANCE = this;
    }

    private static void handleMove(Location from2, Location eventTo, Player player, List<MovementHandler> movementHandlers, PlayerMoveEvent event) {
        boolean shouldUpdateRot = from2.getPitch() != eventTo.getPitch() || from2.getYaw() != eventTo.getYaw();
        boolean shouldUpdatePos = from2.getX() != eventTo.getX() || from2.getY() != eventTo.getY() || from2.getZ() != eventTo.getZ();
        float walkSpeed = event.getPlayer().getWalkSpeed();
        AtomicDouble atomicDouble = new AtomicDouble(walkSpeed);
        for (MovementHandler move : movementHandlers) {
            if (shouldUpdateRot) {
                move.handleUpdateRotation(player, from2, eventTo, null);
            }
            if (shouldUpdatePos) {
                move.handleUpdateLocation(player, from2, eventTo, null);
                move.handleUpdateSpeed(player, atomicDouble, from2, eventTo);
            }
        }
        float newSpeed = (float) atomicDouble.get();
        if (newSpeed == walkSpeed) {
            return;
        }
        player.setWalkSpeed(newSpeed);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        handleMove(event.getFrom(), event.getTo(), event.getPlayer(), movementHandlers, event);
    }

    @EventHandler
    public void onMove(PlayerTeleportEvent event) {
        handleMove(event.getFrom(), event.getTo(), event.getPlayer(), movementHandlers, event);
    }

    public void addMovementHandler(MovementHandler var1) {
        this.movementHandlers.add(var1);
    }


}
