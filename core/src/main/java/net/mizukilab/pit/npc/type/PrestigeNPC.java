package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.prestige.PrestigeMainMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 13:44
 */

public class PrestigeNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "prestige";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&6&l精通");
        if (profile.getLevel() >= 120 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 120) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getPrestigeNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1NjIxNTMzNSwKICAicHJvZmlsZUlkIiA6ICIzNTExZmEwMzdhNWY0ZjUwODBiMjE5ZjRiMmFhNTc3OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2l1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY4YjEyOWNkZDhkNTgxYzIzNDA5YzUyZjBmOWIzNWMxODY4MDcyNzI3Njc3ODNjNjUwZjk3ODk4MTFiNDlkNjQiCiAgICB9CiAgfQp9",
                "WZgx4KVfu+8KFLvW8YLygiEt4V2xO9fe2s5fFW5uKwrHc49ul+VYvt+znthejzjNVKcBKZkSomHqj2qOnfylVuw164MTAzEpG8ZDUlpKQ9IFP/DtmubkoI5EX8tYrvyugWfYyWlkuFXlBGYyUAFbeprBZfhEgjugafWcfBwFv7CZrT4qgVcXfik5X/45E3GxjJrgCOsyfb7fSSA8q94R3KKm/6W8X6+l5WWXG9l7YfGiAEd1gV5du/yEI7YmsQEjRZsygJdy8AAcEnmYoCYBn0vvs6OLhzjdEf4PRNUt0tJWbOWIRi0WVp8YUN+0Jfd50S9VgS1IbO2SJ/SnsG1PkG13xHkJOOBWFXNP+SOSRX4A5cwanFp+XLquRi3GAUdVFJMmGgHsyUccirJILqQwJ9bRvtEAASkTyRTeZK+IFWvL+Xi8z7fYfLSd1ICPlhf6a2Wvffm72zCN/SAev/blB+Vo+6SBSmsXWeKQhhyp5kmXvzXhvaNsA7z4jnXdn6tEQ2BDW34jXQ5Eo+jEQaZOg7dI+lIDMdBdeWkMlgOPxCL1vWVsaIuRVM+JoBjdK/9IN/NepNnsjSMuuAttmg+MxBTO3/yaYJmYTMFP3c4lUUAr9loCmXni2TozDT7iBZzd8krEQd4Sp4Bhm6ATr0eKUd1fU5f9fBjfayVZgxzaCQI=");
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 120 && profile.getPrestige() == 0) {
            player.sendMessage(CC.translate("&c&l等级不足! &7精通在 " + LevelUtil.getLevelTag(profile.getPrestige(), 120) + " &7时解锁."));
        } else {
            new PrestigeMainMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
