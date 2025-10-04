package de.eldoria.forestsaver.configuration.elements.presets.replacements;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

@JsonTypeName("material")
public final class MaterialReplacement extends Replacement<Material> {
    public MaterialReplacement() {
    }

    public MaterialReplacement(Material replacement) {
        super(replacement);
    }

    @Override
    public BlockData blockData() {
        return replacement.createBlockData();
    }
}
