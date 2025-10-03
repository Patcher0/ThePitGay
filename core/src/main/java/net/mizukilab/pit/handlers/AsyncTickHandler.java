package net.mizukilab.pit.handlers;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.util.hologram.Hologram;
import cn.charlotte.pit.util.hologram.HologramAPI;
import net.mizukilab.pit.PitHook;
import net.mizukilab.pit.actionbar.ActionBarManager;
import net.mizukilab.pit.data.operator.PackedOperator;
import net.mizukilab.pit.data.operator.ProfileOperator;
import net.mizukilab.pit.enchantment.IActionDisplayEnchant;
import net.mizukilab.pit.hologram.AbstractHologram;
import net.mizukilab.pit.hologram.HologramMarco;
import net.mizukilab.pit.item.AbstractPitItem;
import net.mizukilab.pit.item.factory.ItemFactory;
import net.mizukilab.pit.util.PublicUtil;
import net.mizukilab.pit.util.chat.ActionBarUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.time.TimeUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Async tick handler
 */
@Skip
public class AsyncTickHandler extends BukkitRunnable {


    ThePit instance = ThePit.getInstance();
    ConcurrentLinkedDeque<Runnable> taskList = new ConcurrentLinkedDeque<>();
    private long tick = 0;
    public void enqueueTask(Runnable task){
        taskList.addLast(task);
    }
    public void flushIds() {
        PublicUtil.itemVersion = PitHook.getItemVersion();
        PublicUtil.signVer = PitHook.getGitVersion();
    }

    public AsyncTickHandler() {
        flushIds();
    }

    @Override
    public void run() {
        //trade
        ActionBarManager actionBarManager = (ActionBarManager) instance.getActionBarManager();
        if (actionBarManager != null) {
            actionBarManager.tick();
        }
        if (tick % 10 == 0) {
            //Async Lru Detector
            ItemFactory itemFactory = (ItemFactory) instance.getItemFactory();
            itemFactory.lru();
            flushIds();
        }
        if (++tick == Long.MIN_VALUE) {
            tick = 0; //从头开始
        }
        if (tick > 1200 && tick % 6000 == 0) {
            //AutoSave
            doAutoSave();
            return;
        }
        //Async Io Tracker
        onHologramTick();
        if(tick % 10 == 0) {
            onActionBarTick();
        }
        if(tick % 3600 == 0){
            long minecraftTick = TimeUtil.getRealTimeToMCTick();
            Bukkit.getWorlds().forEach(world -> world.setTime(minecraftTick));
        }
        ((ProfileOperator) instance.getProfileOperator()).tick();
        while(true) {
            Runnable runnable = taskList.pollFirst();
            if (runnable == null) {
                break;
            }
            try {
                runnable.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    public void onActionBarTick(){
        StringBuilder stringBuilder = new StringBuilder();
        ThePit.getInstance().getProfileOperator().forEach(i -> {
            PlayerProfile profile = i.profile();
            if(!profile.isLoaded()){
                return;
            }
            if(profile.code == ProfileOperator.OPCODE_BUSY){
                return;
            }
            Player entity;
            if(i instanceof PackedOperator e){
                entity = e.getLastBoundPlayer();
            } else {
                entity = null;
            }
            if(entity == null){
                return;
            }
            AbstractPitItem leggings = profile.leggings;
            AbstractPitItem heldItem = profile.heldItem;
            if(leggings != null){
                handleActionBar(stringBuilder,entity,leggings);
            }
            if(heldItem != null){
                handleActionBar(stringBuilder,entity,heldItem);
            }
            if(!stringBuilder.isEmpty()){
                ActionBarUtil.sendActionBar1(entity,"sk",stringBuilder.toString(),10);
                stringBuilder.setLength(0);
            }
        });
    }
    public void handleActionBar(StringBuilder stringBuilder,Player entity,AbstractPitItem e){
        e.getEnchantments().forEach((z,level) -> {
            if(z instanceof IActionDisplayEnchant w){
                stringBuilder.append("&b&l").append(z.getEnchantName()).append(" ").append(w.getText(level,entity)).append(" ");
            }
        });
    }
    public void onHologramTick(){
        ThePit.getInstance().getHologramMarco().getHolograms().forEach((playerUuid,hologram) -> {
            Player player = Bukkit.getPlayer(playerUuid);
            if(player == null){
                return;
            }
            Map<String, HologramMarco.HologramData> data = hologram.getHologramData();
            for (AbstractHologram abstractHologram : ThePit.getInstance().getHologramFactory().getLoopHologram()) {
                HologramMarco.HologramData hologramData = data.get(abstractHologram.getInternalName());
                if (hologramData == null) {
                    continue;
                }

                int i1 = Math.max(1, abstractHologram.loopTicks());

                if (tick % i1 != 0) {
                    continue;
                }

                List<String> text = abstractHologram.getText(player);
                List<Hologram> holograms1 = hologramData.getHolograms();
                if (text.size() != holograms1.size()) {
                    holograms1.forEach(Hologram::deSpawn);
                    holograms1.clear();
                    List<Hologram> holograms = new ArrayList<>();
                    for (int i = 0; i < text.size(); i++) {
                        String line = text.get(i);
                        Hologram holo = HologramAPI.createHologram(abstractHologram.getLocation().clone().add(0, -i * abstractHologram.getHologramHighInterval(), 0), CC.translate(line));
                        holo.spawn(Collections.singletonList(player));
                        holograms.add(holo);
                    }
                    holograms1.addAll(holograms);
                }
                for (int i = 0; i < text.size(); i++) {
                    holograms1.get(i).setText(CC.translate(text.get(i)));
                }
            }
        });
    }
    public void doAutoSave() {
        final long last = System.currentTimeMillis();
        instance.getProfileOperator().doSaveProfiles();


        final long now = System.currentTimeMillis();
        Bukkit.getLogger().info("Auto saved player backups, time: " + (now - last) + "ms");
        Bukkit.getOnlinePlayers().forEach(player -> {

            if (player.hasPermission("pit.admin")) return;
            ((ProfileOperator) instance.getProfileOperator()).operatorStrict(player).ifPresent(operator -> {
                PlayerProfile playerProfileByUuid = operator.profile();
                if (playerProfileByUuid.getCombatTimer().hasExpired()) {
                    if (player.getLastDamageCause() != null) {
                        player.setLastDamageCause(null); //fix memory leak
                    }
                }
                final long lastActionTimestamp = playerProfileByUuid
                        .getLastActionTimestamp();
                //AntiAFK
                if (now - lastActionTimestamp >= 10 * 60 * 1000) {
                    // 意义不明
                    // player.sendMessage("...", true);
                    operator.pending(i -> {
                        playerProfileByUuid.setLastActionTimestamp(now);
                    });
                }
            });

        });
    }
}
