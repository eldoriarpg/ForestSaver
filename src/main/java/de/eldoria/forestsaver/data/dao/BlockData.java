package de.eldoria.forestsaver.data.dao;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public record BlockData(String data) {

    public static BlockData fromBlock(Block block) {
        return new BlockData(block.getBlockData().getAsString());
    }

    public org.bukkit.block.data.BlockData createData() {
        return Bukkit.createBlockData(data);
    }
}
