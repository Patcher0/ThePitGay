package cn.charlotte.pit.perk;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.mizukilab.pit.parm.listener.*;
import net.mizukilab.pit.util.PublicUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 22:26
 */
public class PerkFactory {

    private final List<AbstractPerk> perks;
    private final List<IPlayerDamaged> playerDamageds;
    private final List<IAttackEntity> attackEntities;
    private final List<IItemDamage> iItemDamages;
    private final List<IPlayerBeKilledByEntity> playerBeKilledByEntities;
    private final List<IPlayerKilledEntity> playerKilledEntities;
    private final List<IPlayerRespawn> playerRespawns;
    private final List<IPlayerShootEntity> playerShootEntities;
    private final Map<AbstractPerk, ITickTask> tickTasks;
    private final List<IPlayerAssist> playerAssists;

    private final Map<String, AbstractPerk> perkMap = new Object2ObjectOpenHashMap<>();


    public PerkFactory() {
        this.perks = new ObjectArrayList<>();
        this.playerDamageds = new ObjectArrayList<>();
        this.iItemDamages = new ObjectArrayList<>();
        this.attackEntities = new ObjectArrayList<>();
        this.playerBeKilledByEntities = new ObjectArrayList<>();
        this.playerKilledEntities = new ObjectArrayList<>();
        this.playerRespawns = new ObjectArrayList<>();
        this.tickTasks = new Object2ObjectOpenHashMap<>();
        this.playerShootEntities = new ObjectArrayList<>();
        this.playerAssists = new ObjectArrayList<>();
    }

    @SneakyThrows
    public void init(Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (AbstractPerk.class.isAssignableFrom(clazz)) {
                Object instance = clazz.getConstructor().newInstance();
                perks.add((AbstractPerk) instance);

                final AbstractPerk perk = (AbstractPerk) instance;

                perkMap.put(((AbstractPerk) instance).getInternalPerkName(), (AbstractPerk) instance);

                PublicUtil.register(clazz, instance, playerDamageds, attackEntities, iItemDamages, playerBeKilledByEntities, playerKilledEntities, playerRespawns, playerShootEntities);
                if (ITickTask.class.isAssignableFrom(clazz)) {
                    final ITickTask task = (ITickTask) instance;
                    tickTasks.put(perk, task);
                }
                if (IPlayerAssist.class.isAssignableFrom(clazz)) {
                    playerAssists.add((IPlayerAssist) instance);
                }
            }
        }
    }

    public List<AbstractPerk> getPerks() {
        return perks;
    }

    public List<IPlayerDamaged> getPlayerDamageds() {
        return playerDamageds;
    }

    public List<IAttackEntity> getAttackEntities() {
        return attackEntities;
    }

    public List<IItemDamage> getiItemDamages() {
        return iItemDamages;
    }

    public List<IPlayerBeKilledByEntity> getPlayerBeKilledByEntities() {
        return playerBeKilledByEntities;
    }

    public List<IPlayerKilledEntity> getPlayerKilledEntities() {
        return playerKilledEntities;
    }

    public List<IPlayerRespawn> getPlayerRespawns() {
        return playerRespawns;
    }

    public List<IPlayerShootEntity> getPlayerShootEntities() {
        return playerShootEntities;
    }

    public Map<AbstractPerk, ITickTask> getTickTasks() {
        return tickTasks;
    }

    public List<IPlayerAssist> getPlayerAssists() {
        return playerAssists;
    }

    public Map<String, AbstractPerk> getPerkMap() {
        return perkMap;
    }
}
