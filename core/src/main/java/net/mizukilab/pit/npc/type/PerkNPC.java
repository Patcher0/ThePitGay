package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.perk.normal.choose.PerkChooseMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: Misoryan
 * @Created_In: 2021/1/1 13:11
 */

public class PerkNPC extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "perk";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&c&l天赋");
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
        return ThePit.getInstance().getPitConfig().getPerkNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1NzY0MDAwNywKICAicHJvZmlsZUlkIiA6ICI3YTJlMGE4NDVlMmM0N2RmYjQxMDFmNjUyZmIzODhlNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaXNrOHMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI2MWIzYTVhY2VmZGM5ZGQ2ZGMwZDhiZWI4YjFhYWI1ZTdhMDc1MmQwMTE5NjUyZTQyZTI2ZjBlNWFhNTFhYyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "BnJbJb4Z/mVY5PqBg6qoHwwRNf+th671A/TRG9b02EWI0fLUCPoJv0N/YmrLdVFOf3J/o+tX9zgbLTlP5VoTeQp5NZf6X6ZbteNk91UfT/yJFhiPWYpIih0nZw+Cu59jVSLFFWsXgxoF54H/ICKRgFXep+GkEBVa57YjrfbtCl2xIWVZyj0PHOPlT7H7EDltBhDM03BhrR53LvWg2TnNRNlH6rN2lQooxocU3Fi9+a1CBZBZv5r6wQfNaoE8fC53ibkMOBP1cdWJq9XzRUuYs48my623nuBW8UYKtRXcRyC9x61nB88yrTD20bL0TK3nNFsIUtM13ydcQrmlZ0v2vDbmw/7nlDNgEbcOizJLgenVbYOckKhDqKyE/guz6+zMxD+WgAgnBEYsKjlO6wXT2TAsjSOog82fzR+j4bv/9q1SwCC40OmCTKMjeQuQraIbYMMv5+jZo3IMxxyfTfHMJa/ZlRJjP5PGBrAX0d08IeirD744Y+luw0PLPe9FUlDipGCKkcT1vN1xTB17WPrckNgV1Ah+2cbrfCd0yvBtWlA41VP1Ng+au+GyzbG0jhI4gzg3yO4GmzkxXFDiNanoAb/O1NwDvfz0J8PorbRYhdDxBwf+Cmc2IyM0Rd5KhYU3H1OovTob/h0wUysRfsJx0VDfxQkIa+gXaFxVWN9eu2w="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 10) {
            player.sendMessage(CC.translate("&c&l等级不足! &7天赋在 " + LevelUtil.getLevelTag(profile.getPrestige(), 10) + " &7时解锁."));
        } else {
            new PerkChooseMenu().openMenu(player);
        }
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return null;
    }
}
