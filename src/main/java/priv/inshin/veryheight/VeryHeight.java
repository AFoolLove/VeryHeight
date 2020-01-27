/*
 * Copyright (c) 2020. InShin. All rights reserved.
 */

package priv.inshin.veryheight;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VeryHeight extends JavaPlugin {
    private ConfigManager mConfigManager;
    private BukkitTask mMainTask;
    private BukkitTask mTickTask;

    @Override
    public void onEnable() {
        this.mConfigManager = new ConfigManager(this);
        this.mMainTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (mConfigManager != null && !mConfigManager.heightRangeEffects.isEmpty()) {
                for (Player player : Bukkit.getOnlinePlayers()) { // 所有在线玩家，包括 OP
                    List<HeightRangeEffects> heightRangeEffects = mConfigManager.heightRangeEffects.get(player.getWorld().getName());
                    if (heightRangeEffects != null) {
                        for (HeightRangeEffects heightRangeEffect : heightRangeEffects) {
                            // 给予相应 BUFF
                            for (PotionEffect effect : heightRangeEffect.effects(player)) {
                                player.removePotionEffect(effect.getType());
                                if (effect.getDuration() != 0) {
                                    player.addPotionEffect(effect, false);
                                }
                            }
                        }
                    }
                }
            }
        }, 1, 1);
        mTickTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (List<HeightRangeEffects> value : mConfigManager.heightRangeEffects.values()) {
                for (HeightRangeEffects effects : value) {
                    for (Map.Entry<UUID, Long> entry : effects.ticks.entrySet()) {
                        entry.setValue(entry.getValue() + 1);
                    }
                }
            }
        }, 1, 1);
    }

    @Override
    public void onDisable() {
        if (this.mMainTask != null && !this.mMainTask.isCancelled()) {
            this.mMainTask.cancel();
        }
        if (this.mTickTask != null && !this.mTickTask.isCancelled()) {
            this.mTickTask.cancel();
        }
        this.mConfigManager.heightRangeEffects.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equals("reconfig") && sender.hasPermission("inshin.veryheight.reconfig")) {
            reloadConfig();
            this.mConfigManager.heightRangeEffects.clear();
            this.mConfigManager = new ConfigManager(this);
            sender.sendMessage("[VeryHeight] §2Reload config.");
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return "reconfig".startsWith(args[0].toLowerCase()) ? Collections.singletonList("reconfig") : null;
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
