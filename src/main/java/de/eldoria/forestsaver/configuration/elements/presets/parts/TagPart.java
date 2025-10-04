package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.eldoria.forestsaver.configuration.elements.presets.replacements.Replacement;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@JsonTypeName("tag")
public final class TagPart extends Part<Tag<Material>> implements MaterialProvider {
    @JsonIgnore
    Set<Material> materials;

    public TagPart() {
    }

    public TagPart(Tag<Material> original, Replacement<?> replacement, int restoreTokens, boolean doNotRestore, int restoreTokenJitter) {
        super(original, replacement, restoreTokens, doNotRestore, restoreTokenJitter);
    }

    @Override
    public boolean isCovered(BlockData blockData) {
        if (materials == null) {
            materials = new HashSet<>(original.getValues());
        }
        return materials.contains(blockData.getMaterial());
    }

    @Override
    public Set<Material> materials() {
        if (materials == null) {
            materials = new HashSet<>(original.getValues());
        }
        return Collections.unmodifiableSet(materials);
    }
}
