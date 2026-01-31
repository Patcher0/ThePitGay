package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.status.StatusMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/2 21:31
 */

public class StatusNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "status";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&9&l统计信息");
        if (profile.getLevel() >= 50 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 50) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getStatusNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1NzEwODMyMSwKICAicHJvZmlsZUlkIiA6ICJiZWNiMGVhYmM3N2I0ZjJiYTM1OGNkOGM0MWM0MWE4ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJad2VpZmFjaEd1c3RhZiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNTdjY2YyNGNkYjE5MDEwNjI0ZTQxM2Q1ZjI1MmJjNzM5YmZhZDM5MWU2NDg4MzBhNTNmOGM5YjM5ZjE5Mzg3IgogICAgfQogIH0KfQ==",
                "GqcfJWdP4pfH7jMzFrtWcrlSosp8kKJ4kb4kvL54LeHFA5/lvzW6mUYxQUFg/Xv3BadM5G+R/R10lY1UYLpzsqAnGBtlm53W0g0P5FDy8aZLHivqWS813tnho5VeFurpWQGcK0YdRC1V75aaoQpkyf6PRdkWLm+HMQgjZ0bJs3n1Pv2QSB6m+GIqP1NmuhDpiK2HJLZdke6IxEsxsz9dof6aAHt6uR+f0VxaDZlcCaQ9ZP3WxtIO+19pqNttwmovC2pjjhh3uleXpuVgwut6DBnkiWaT75yRt2AskRdxVQOQiIpeQZ0q4qV5sBo5v2VawIwLJZwq3rC3OB2/btQ9+PBqC/cRIiplJwpqjJsRedTYfc3YysZ5wZ7XaX0VXt3lC9wS8oKTqZEl6Dh6f+kLmIbHVe1X5wJYZHUb+DQQYr5028mAkFoFogA5BQ/5AJg0k7XgLbNijNOC0O1uFPXW+mVjoS2AE27Hd6uxua9uUH4NGhVI9LMYb5WZOauB6N7fnQtbpwDEaw59vEF3OG4ZExnv9uolaAKWHuGwLBdk9iR/8Tx0TT5lkUPpTZiF3/wkK6AwZF54Txg4yZwiv3gqHB7OAxgNhcsKEg8QEmNzUbMTa36w09rMF00AwpMv90QV7LHzwHpbFepyOBnAtNnHXPnLf6V4KktLkFQ1mn8bmGA="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 50 && profile.getPrestige() == 0) {
            player.sendMessage(CC.translate("&c&l等级不足! &7统计在 " + LevelUtil.getLevelTag(profile.getPrestige(), 50) + " &7时解锁."));
        } else {
            new StatusMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
