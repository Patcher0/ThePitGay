package net.mizukilab.pit.hologram.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.LeaderBoardEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.hologram.AbstractHologram;
import net.mizukilab.pit.util.chat.StringUtil;
import net.mizukilab.pit.util.level.LevelUtil;
import net.mizukilab.pit.util.rank.RankUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/4 13:04
 */
public class LeaderBoardHologram extends AbstractHologram {

    @Override
    public String getInternalName() {
        return "leader_board_hologram";
    }

    @Override
    public List<String> getText(Player player) {
        List<String> hologramText = new LinkedList<>();
        hologramText.add("&b&l顶级活跃玩家");
        hologramText.add("&7天坑乱斗等级排名");
        hologramText.add("");


        Object2ObjectLinkedOpenHashMap<UUID, LeaderBoardEntry> leaderBoardEntries = LeaderBoardEntry.getLeaderBoardEntries();
        Iterator<LeaderBoardEntry> iterator = leaderBoardEntries.values().iterator();
        for (int b = 0; b < 10; b++) {
            if (!iterator.hasNext()) {
                hologramText.add("&e" + (b + 1) + "&7. &7暂无");
            } else {
                LeaderBoardEntry entry = iterator.next();

                int prestige = entry.getPrestige();
                double experience = entry.getExperience();
                for (int i = 0; i < prestige; i++) {
                    experience = experience + LevelUtil.getLevelTotalExperience(i, 120);
                }
                String levelTag = LevelUtil.getLevelTagWithRoman(entry.getPrestige(), entry.getExperience());
                String formattedExp = StringUtil.getFormatLong((long) experience);

                hologramText.add("&e" + entry.getRank() + "&7." + " " + levelTag + " " + entry.getName() + " &7- &b" + formattedExp + " 经验值");
            }
        }

        LeaderBoardEntry entry = leaderBoardEntries
                .get(player.getUniqueId());
        if (entry != null) {
            double top = 100D * entry.getRank() / leaderBoardEntries.size();
            int prestige = entry.getPrestige();
            double experience = entry.getExperience();
            for (int i = 0; i < prestige; i++) {
                experience = experience + LevelUtil.getLevelTotalExperience(i, 120);
            }
            String formattedExp = StringUtil.getFormatLong((long) experience);

            hologramText.add("");
            hologramText.add("&7你的经验值: &b" + formattedExp + " 经验值");
            hologramText.add("&7排名: " + "&e#" + entry.getRank() + " &7(前&e" + new DecimalFormat("0.0").format(top) + "%&7)");
        } else {
            hologramText.add("");
            hologramText.add("&7&o还没有你的排行数据,请等会再来...");
        }
        return hologramText;
    }

    @Override
    public boolean shouldLoop() {
        return true;
    }

    @Override
    public int loopTicks() {
        return 20;
    }

    @Override
    public double getHologramHighInterval() {
        return 0.3;
    }

    @Override
    public Location getLocation() {
        return ThePit.getInstance().getPitConfig().getLeaderBoardHologram();
    }
}
