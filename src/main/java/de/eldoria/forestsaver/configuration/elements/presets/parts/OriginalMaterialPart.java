package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Collection;
import java.util.List;

@JsonTypeName("material")
public final class OriginalMaterialPart extends OriginalPart<Material> implements MaterialProvider {
    public OriginalMaterialPart() {
    }

    public OriginalMaterialPart(Material original) {
        super(original);
    }

    @Override
    public boolean isCovered(BlockData blockData) {
        return original == blockData.getMaterial();
    }

    @Override
    public Collection<Material> materials() {
        return List.of(original);
    }
}
