package net.mizukilab.pit.nametag;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.PlayerProfile;
import cn.charlotte.pit.data.sub.PerkData;
import cn.charlotte.pit.events.EventFactory;
import cn.charlotte.pit.events.genesis.GenesisTeam;
import cn.charlotte.pit.events.trigger.type.IEpicEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mizukilab.pit.events.impl.major.HamburgerEvent;
import net.mizukilab.pit.events.impl.major.RedVSBlueEvent;
import net.mizukilab.pit.events.impl.major.SpireEvent;
import net.mizukilab.pit.util.SpecialUtil;
import net.mizukilab.pit.util.chat.CC;
import net.mizukilab.pit.util.nametag.BufferedNametag;
import net.mizukilab.pit.util.nametag.NametagAdapter;
import net.mizukilab.pit.util.rank.RankUtil;
import nya.Skip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/1 15:56
 */
@Skip
public class NameTagImpl implements NametagAdapter {

    @Override
    public List<BufferedNametag> getPlate(Player player) {

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        int size = onlinePlayers.size();
        List<BufferedNametag> tags = new ObjectArrayList<>(size);
        List<PlayerProfile> profiles = new ObjectArrayList<>(size);

        for (Player target : onlinePlayers) {
            profiles.add(PlayerProfile.getPlayerProfileByUuid(target.getUniqueId()));
        }

        ThePit instance = ThePit.getInstance();
        EventFactory eventFactory = instance.getEventFactory();
        if (eventFactory == null) {
            return tags;
        }


        IEpicEvent activeEpicEvent1 = eventFactory.getActiveEpicEvent();
        sort(profiles, activeEpicEvent1);

        int i = 200;
        for (PlayerProfile profile : profiles) {
            String displayName;
            Player target = Bukkit.getPlayer(profile.getPlayerUuid());
            if (activeEpicEvent1 instanceof SpireEvent event) {
                final int rank = event.getRank(target);
                if (rank == -1) {
                    displayName = CC.translate("&7[???&7] " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                } else {
                    displayName = CC.translate("&7[&e#" + rank + "&7] " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                }
            } else if (activeEpicEvent1 instanceof RedVSBlueEvent activeEpicEvent) {
                displayName = activeEpicEvent.isRedTeam(profile.getPlayerUuid()) ? CC.translate(profile.getFormattedLevelTagTabSpec() + " &c") : CC.translate(profile.getFormattedLevelTagTabSpec() + " &9");
            } else {
                displayName = CC.translate(profile.getFormattedLevelTagTabSpec() + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                PerkData perkData = profile.getChosePerk().get(5);
                if (perkData != null) {
                    if (perkData.getPerkInternalName().equalsIgnoreCase("despot_streak") && profile.getStreakKills() >= 200) {
                        displayName = CC.translate("&c&l暴君" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("over_drive") && profile.getStreakKills() >= 50) {
                        displayName = CC.translate("&e&l超速传动" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("high_lander") && profile.getStreakKills() >= 50) {
                        displayName = CC.translate("&6&l尊贵血统" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("beast_mode_mega_streak") && profile.getStreakKills() >= 50) {
                        displayName = CC.translate("&a&l野兽模式" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("hermit") && profile.getStreakKills() >= 50) {
                        displayName = CC.translate("&9&l隐士" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("uber_streak") && profile.getStreakKills() >= 100) {
                        final int level = Math.min(400, (((int) profile.getStreakKills()) / 100) * 100);
                        displayName = CC.translate("&d&l登峰造极" + " " + level + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (perkData.getPerkInternalName().equalsIgnoreCase("to_the_moon") && profile.getStreakKills() >= 100) {
                        displayName = CC.translate("&b&l月球之旅" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }
                    if (SpecialUtil.isPrivate(profile)) {
                        displayName = CC.translate("&b&lPRIVATE" + " " + RankUtil.getPlayerRankColor(profile.getPlayerUuid()));
                    }

                }
            }
            StringBuilder suffix = new StringBuilder();
            if (activeEpicEvent1 instanceof SpireEvent event) {
                final String displayFloor = event.getDisplayFloor(profile.getPlayerUuid());
                suffix.append(" ").append(displayFloor);

            } else if (activeEpicEvent1 instanceof HamburgerEvent event) {
                final HamburgerEvent.PizzaData data = event.getPizzaDataMap().get(profile.getPlayerUuid());
                if (data != null) {
                    suffix.append(" &c")
                            .append(data.getHamburger())
                            .append("ஐ &6")
                            .append(data.getMoney())
                            .append("$");
                }
            } else {
                if (profile.isSupporter() && profile.getPlayerOption().isSupporterStarDisplay() && !profile.isNicked()) {
                    suffix.append(" &e✬");
                }
                if (instance.getPitConfig().isGenesisEnable()) {
                    suffix.append(" ").append(profile.bountyColor());
                    if (profile.getBounty() != 0) {
                        suffix.append("&l").append(profile.getBounty()).append("g");
                    } else {
                        if (profile.getGenesisData().getTeam() == GenesisTeam.ANGEL) {
                            suffix.append("♆");
                        }
                        if (profile.getGenesisData().getTeam() == GenesisTeam.DEMON) {
                            suffix.append("♨");
                        }
                    }
                } else if (profile.getBounty() != 0) {
                    suffix.append("&6&l ").append(profile.getBounty()).append("g");
                } else {
                    suffix.append(" ");
                }
            }
            if (displayName.length() > 16) {
                displayName = displayName.substring(0, 15);

            }

            tags.add(new BufferedNametag(
                    i + "",
                    //&7 refers to Prefix
                    displayName,
                    CC.translate(suffix.toString()),
                    false,
                    player));
            i--;
        }

        return tags;
    }

    private static void sort(List<PlayerProfile> profiles, IEpicEvent activeEpicEventName) {
        profiles.sort((profile1, profile2) -> {
            int priority1 = profile1.isNicked() ? profile1.getNickPrestige() + 1000 * profile1.getNickLevel() : profile1.getPrestige() + 1000 * profile1.getLevel();
            int priority2 = profile2.isNicked() ? profile2.getNickPrestige() + 1000 * profile2.getNickLevel() : profile2.getPrestige() + 1000 * profile2.getLevel();

            if (activeEpicEventName instanceof RedVSBlueEvent activeEpicEvent) {
                if (activeEpicEvent.isRedTeam(profile1.getPlayerUuid())) {
                    priority1 += 10000000;
                }
                if (activeEpicEvent.isRedTeam(profile2.getPlayerUuid())) {
                    priority2 += 10000000;
                }
                return Integer.compare(priority1, priority2);
            }

            if (activeEpicEventName instanceof SpireEvent event) {
                priority1 += event.getRank(Bukkit.getPlayer(profile1.getPlayerUuid())) * 10000000;
                priority2 += event.getRank(Bukkit.getPlayer(profile2.getPlayerUuid())) * 10000000;
            }

            return Integer.compare(priority1, priority2);
        });
    }

    @Override
    public boolean showHealthBelowName(Player player) {
        return ThePit.getInstance().getEventFactory() != null && "spire".equals(ThePit.getInstance().getEventFactory().getActiveEpicEventName());
    }
}
