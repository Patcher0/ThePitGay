package net.mizukilab.pit.impl;

import cn.charlotte.pit.api.PointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlayerPointsAPI {
    public static PointsAPI API;
    public static void init(){
        Plugin playerPoints = Bukkit.getPluginManager()
                .getPlugin("PlayerPoints");
        try {
            if (playerPoints != null) {
                API = PlayerPointsAPIImpl.INSTANCE;
            } else {
                API = new NullPlayerPointsAPIImpl();
            }
        } catch (Throwable e) {
            //fallback
            API = new NullPlayerPointsAPIImpl();
        }
    }
}
