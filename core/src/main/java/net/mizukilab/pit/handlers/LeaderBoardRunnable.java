package net.mizukilab.pit.handlers;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.LeaderBoardEntry;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.SneakyThrows;
import net.mizukilab.pit.util.rank.RankUtil;
import nya.Skip;
import org.bson.Document;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/3 12:57
 */
@Skip
public class LeaderBoardRunnable extends BukkitRunnable {

    @SneakyThrows
    @Override
    public void run() {

        updateLeaderboardData();
    }

    public static void updateLeaderboardData() {
        FindIterable<Document> documents = ThePit.getInstance()
                .getMongoDB()
                .getCollection()
                .find()
                .projection(Projections.include("totalExp", "uuid", "experience", "prestige", "lastLogoutTime"))
                .sort(Sorts.descending("totalExp"))
                .filter(Filters.gte("lastLogoutTime", System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));

        Object2ObjectLinkedOpenHashMap<UUID,LeaderBoardEntry> entries = new Object2ObjectLinkedOpenHashMap<>();
        int i = 1;
        for (Document document : documents) {
            try {
                String uuid = document.getString("uuid");
                final Object expObj = document.get("experience");
                Double experience;
                try {
                    experience = (Double) expObj;
                } catch (Exception e) {
                    experience = Double.valueOf(((Integer) expObj));
                }
                int prestige = document.getInteger("prestige");
                int rank = i;
                UUID uuid1 = UUID.fromString(uuid);
                String playerRealColoredName = RankUtil.getPlayerRealColoredName(uuid1);
                entries.putAndMoveToLast(uuid1,new LeaderBoardEntry(playerRealColoredName, uuid1, rank, experience, prestige));
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LeaderBoardEntry.setLeaderBoardEntries(entries);
    }
}
