package de.eldoria.forestsaver.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.incendo.cloud.annotations.Command;

public class Debug {

    @Command("resourcesaver|rs debug blockdata")
    public void getBlockData(CommandSourceStack stack) {
        if (!(stack.getSender() instanceof Player player)) return;

        RayTraceResult rayTraceResult = player.rayTraceBlocks(10);
        if (rayTraceResult.getHitBlock() == null) return;
        player.sendRichMessage(rayTraceResult.getHitBlock().getBlockData().getAsString());
    }
}
