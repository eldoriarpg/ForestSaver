package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.eldoria.forestsaver.configuration.elements.presets.replacements.Replacement;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Collection;
import java.util.List;

@JsonTypeName("material")
public final class MaterialPart extends Part<Material> implements MaterialProvider{
    public MaterialPart() {
    }

    public MaterialPart(Material original, Replacement<?> replacement, int restoreTokens, boolean doNotRestore, int restoreTokenJitter) {
        super(original, replacement, restoreTokens, doNotRestore, restoreTokenJitter);
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
