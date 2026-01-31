package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.shop.ShopMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 11:16
 */

public class ShopNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "shop";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&b&l商店");
        if (profile.getLevel() >= 10) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getShopNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1NzUwNTgxOSwKICAicHJvZmlsZUlkIiA6ICI5MDkwNWNmMzQ3YmU0ODNkYmFjNDQwYTZlNjMxYmM5MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJjYW53ZWNhbGwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkwZDQzZDI1YjA1MjliODBkNTEwZmE0OWZmMDhkYTI5ZDgyNDhkMzY5OWFmNWE0ZTQ1ZDE5ZmI1NDFjZDE0NSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "d5rm00wSUJ3LDF9wRLqNXuJpJp6DSdjQ0cNBnIqv25EvWuv8dTW2qkuCHcnCuTvGk2yw2zVZaK6I19f/g47GiwSxBbT1/VYwq5yp4bApvcXzhUyawY+9fX1S1dYlnbyApAtIK/TLVpWXAv+4f/V+V+ftLXH5TxTP8YOBIprtdQcwiRGQsvPg6b8SXJnRImmptm856u8BMRqR61QEHOCXs3YU8Dzic4Y7iArZ2li7ae59z8eTxx4nMmoWfi4QPfaD7hBeabB9B1GICTg29d1qjiDj+hcynSoUGzDdrVjLOG5mkVEkuvAPVBZoaloCHMm11257gk/3d4d/mP64JsZruXqD5o1D2NRSk6BtYcD402uxTb3+lkk3z3SG0RK53LdzSW/nqhhgbFkakM/L8RIEAr5zsaHDm39ca8kcT6bjzc4dt23bR2F3pqKAL3+2XgeZvTcA+MqHEohrzxOuYrGTaEMYMvMb2rC/ZqzHkYYjxLyk+oVTUuzPbc4Xt2hlpsLxRRWgXQvyCWPeDRoOpoimTUiabMRnYOe4q9D9PSuJEV4OB1R3lP34qMIuS5aFHWrUYs9U6uwAKvNwL6VqteXy8yZhrdDwUMcLv75XdOPZDbPblwdfmjJqQLgQLjr/WGyDSqJ5gaG38qHpz/8oxW0mwgWOkP2vKVuQlXgyK2NQSfM=");
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 10) {
            player.sendMessage(CC.translate("&c&l等级不足! &7商店在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &7时解锁."));
        } else {
            new ShopMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
