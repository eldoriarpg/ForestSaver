package de.eldoria.forestsaver.data.data;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public record BlockData(Material material, String data) {

    public static BlockData fromBlock(Block block) {
        Material type = block.getType();
        var blockData = block.getBlockData().getAsString();
        return new BlockData(type, blockData);
    }

    public org.bukkit.block.data.BlockData createData() {
        return Bukkit.createBlockData(material, data);
    }
}
