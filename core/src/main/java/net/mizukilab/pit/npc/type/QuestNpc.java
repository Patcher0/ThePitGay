package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.quest.main.QuestMenu;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.level.LevelUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/19 18:38
 */

public class QuestNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "quest";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> lines = new ObjectArrayList<>(3);
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        lines.add("&3&l任务");
        if (profile.getLevel() >= 30 || profile.getPrestige() > 0) {
            lines.add("&e&l右键查看");
        } else {
            lines.add("&c在 " + LevelUtil.getLevelTag(profile.getPrestige(), 30) + " &c时解锁");
        }
        return lines;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getQuestNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin("ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1ODA5ODg5OSwKICAicHJvZmlsZUlkIiA6ICI1YjM3NWRiNTMyNTY0NmUyOGIyZjZkMjFlMDRiODUxMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQdWd6VGh1Z3oiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZjZDk2MDJlNjFjNTJhZWFmZWQzMjIxZTZjZTM5NDY1ZjBlOTA0YWYyNGE5NGI1ZjNmNGZhODA4YmFmYjhhZCIKICAgIH0KICB9Cn0=",
                "g38z2yqB1z/ZzgP6pB8MWKQF2TtVRbIquLTt0gXpKKeKfzeRzFykCH5PF20xT8aTks8mWwkMubfieo63tHDCXHkcQ17gTP+UWkcst7iorMxMmM8dT9oDsrD+5TGlfjVgvmfKvgzryDtElQ6GY3jRl9kI+rh5ebmsbud7e+JYZP4MvFe71MTTvzbePVMThhjVDZ8wrCYbQ17qfhztAvqm7ai7qPo6ceEr1PzeMy8Sl7aRwM+EAZ3EUhTPYP9U6jyqQ08qSWmKgh5GuzeNtY1z75FuKioboCG6b2oyAPdlFmbP1+jvLySPNJz9iQddu8BfqASGMsBoRdLdaH6TPzPgqK6VoNITS+tvdGX4yyxT3ARwzK4Wo1vtXGuT/8zU85TUoh7TPKMxQdUuyky4dNqIuoxC6rexO9eaDJ1VNjk6omGQOjpmt+Y0pPGqa7+3xzJRf7L70oS4scuDubgRo1UDKTUOUwSR7eL6fPkOCd3hzB4NMueUzE63Xnehd/9bNdGT3M3KRVMse18FOfhZ88zuURGB9GksxQUmpImbdscxMxJRHx/rcX46/7Wl99MKp7SHD85VtTPzRnzX6QsliITOjoxv5jYVAmW6HLACqYbo3tWvXHnV0pIsUsrzaCu0eO748bbTVqH+JycbYfP7B8AwCC8dClZHU2iNamQKvT568YE="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        if (profile.getLevel() < 30) {
            player.sendMessage(CC.translate("&c&l等级不足! &7任务在 " + LevelUtil.getLevelTag(profile.getPrestige(), 30) + " &7时解锁."));
            return;
        }
        new QuestMenu().openMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return new ItemStack(Material.BOOK);
    }
}
