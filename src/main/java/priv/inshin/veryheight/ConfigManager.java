/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package priv.inshin.veryheight;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器，文件采用YMAL格式
 */
public class ConfigManager {
    public final Map<String, List<HeightRangeEffects>> heightRangeEffects = new HashMap<>();

    public ConfigManager(@NotNull JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection worlds = config.getConfigurationSection("world");
        if (worlds != null) {
            for (String world : worlds.getKeys(false)) { // 遍历提出的世界
                ConfigurationSection height = worlds.getConfigurationSection(world + ".height");
                if (height != null) {
                    for (String key : height.getKeys(false)) { // 遍历该世界的所有范围
                        String[] range = key.split("-", 2);
                        int minHeight = Integer.parseInt(range[0]);
                        int maxHeight = Integer.parseInt(range[1]);
                        List<HeightRangeEffects> heightRangeEffects = this.heightRangeEffects.computeIfAbsent(world, k -> new ArrayList<>());
                        heightRangeEffects.add(new HeightRangeEffects(minHeight, maxHeight, height.getStringList(key + ".effects"), height.getInt(key + ".tick", 0)));
                    }
                }
            }
        }
    }


}
