package cn.charlotte.pit.util.hologram.packet

import net.minecraft.server.v1_8_R3.EntityArmorStand
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.concurrent.atomic.AtomicInteger

/**
 * 2024/5/16<br></br>
 * ThePitPlus<br></br>
 *
 * @author huanmeng_qwq
 */
class ArmorStandHelper {
    companion object {
        @JvmStatic
        var ATOM: AtomicInteger = AtomicInteger(60000);
        @JvmStatic
        var FIELD_ACCESS = net.minecraft.server.v1_8_R3.Entity::class.java.getDeclaredField("id");
        @JvmStatic
        var HANDLE_ACCESS: VarHandle? = null;
        init {
            FIELD_ACCESS.trySetAccessible()
            HANDLE_ACCESS = MethodHandles.privateLookupIn(net.minecraft.server.v1_8_R3.Entity::class.java,
                MethodHandles.lookup()).unreflectVarHandle(FIELD_ACCESS);
        }
        @JvmStatic
        fun applyLocation(location: Location, armorStand: PacketArmorStand) {
            armorStand.move(location, true)
        }

        //傻逼幻梦。
        @JvmStatic
        @JvmOverloads
        fun memoryEntity(location: Location,entityId :Int = ATOM.incrementAndGet()): ArmorStand {
            val worldServer = (location.world as CraftWorld).handle
            val entityArmorStand = EntityArmorStand(worldServer, location.x, location.y, location.z)
            HANDLE_ACCESS!!.set(entityArmorStand, entityId)
            return entityArmorStand.bukkitEntity as ArmorStand
        }

        @JvmStatic
        fun setEntityLocation(entity: Entity, to: Location) {
            entity as CraftEntity
            entity.handle.setLocation(to.x, to.y, to.z, to.yaw, to.pitch) //crasher code -->
            //locate to public void entityJoinedWorld(Entity entity, boolean flag)
            //it will not add to Minecraft Server Entity System, but it will be added to the chunk, that is bug from bukkit
        }
    }
}
