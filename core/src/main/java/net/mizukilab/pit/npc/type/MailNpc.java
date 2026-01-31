package net.mizukilab.pit.npc.type;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerMailData;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.mail.Mail;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.mizukilab.pit.menu.mail.MailMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/3/25 19:03
 */

public class MailNpc extends SkinNPC {

    @Override
    public String getNpcInternalName() {
        return "mail";
    }

    @Override
    public List<String> getNpcDisplayName(Player player) {
        List<String> hologram = new ObjectArrayList<>(3);
        final PlayerProfile profile = PlayerProfile.getPlayerProfileByUuid(player.getUniqueId());
        final PlayerMailData mailData = profile.getMailData();

        int unread = 0;
        for (Mail mail : mailData.getMails()) {
            if (!mail.isClaimed()) {
                unread++;
            }
        }
        hologram.add("&6&l邮件系统");
        if (unread == 0) {
            hologram.add("&e&l右键查看");
        }
        if (unread > 0) {
            hologram.add((System.currentTimeMillis() % 2 == 0 ? "&a" : "&2") + "您有 " + unread + " 封未读邮件");
        }

        return hologram;
    }

    @Override
    public NPCAnimation getAnimation() {
        return null;
    }

    @Override
    public Location getNpcSpawnLocation() {
        return ThePit.getInstance().getPitConfig().getMailNpcLocation();
    }

    @Override
    public Skin getNpcSkin() {
        return new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc2ODU1Nzg4NTMxNiwKICAicHJvZmlsZUlkIiA6ICI3M2ZhZTRjYzA0NjA0MzVmYTg3YTlkNzcxN2JlZGJlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZWNMcDFjN2UiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJmMGE2YzRlOTliNWRlY2QyZTI0ZDc4OWFmMWU4OGVhYzg0Y2FjOGFjNmUxNTZmNDczNGU4N2Q5NzMxZWZlMCIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q5ZDgyYWIxN2ZkOTIwMjJkYmQ0YTg2Y2RlNGMzODJhNzU0MGUxMTdmYWU3YjlhMjg1MzY1ODUwNWE4MDYyNSIKICAgIH0KICB9Cn0=",
                "EEmLk3FkJpVXNH/G/89xjHp1kAkmmZuEpHovrtIIUDGm6Z/99OSqfUrU6NtsY2Hmz0cmLhhSWO6L/ImDC8I/nnx/r8v4QlBWEvbyXzIuQ0MtPveja7Quz5azkurcCTC/7bwTAX3Dk65sMWDzJ4se3WDMkkdNguMzBtCJ3A3rVGy7dfvRw2eBWo3Bu4kABpw0oYH8wQtzmdWCkxYqq4yGW3dhqbM/sCRzR5/HbucwsBHP+Evr4MT/F4Us/Iz0jd8Q+E8O7KalrrYsa0DxVdyszBu4KiHIPLdOiJm/AhBW3WoX+PkPSSUChkcioDIVkV4KHFPTcJa9d1okEM7kcEjw9kOyonXb5eqSpoHUyERVr9ZdmYJLCFignaEsz9WrZmeJlbnqkVTdyTH0nHxI4QPqARhw4DyReqcctRl6RU1kCQHNAhXCYDxpfOBCUsvvo0kNpWIibGFOI5MgXZMEZ1iUaCQj2zlDBcIAE6jFV6WraF4kwZyPrxCCfCOTB60EK9Iy9NZUZYW8aWjsEmgBo2V5TU+YwKuuPHNrEqp7Z2FiNSlA+1KLvMxVmxt2kEOp1crLC39I1rp2aLUFb74VVchZLCF3kQUCBExsHXtJPv/Hx7P7aS959etuNsp+nwb7q4CAhXPYxpwd+FV1xwrVgo0GdGQcUJKQrXAKNPqu8WJmSCY="
        );
    }

    @Override
    public void handlePlayerInteract(Player player) {
        new MailMenu().openMenu(player);
    }

    @Override
    public ItemStack getNpcHeldItem() {
        return new ItemStack(Material.CHEST);
    }
}
