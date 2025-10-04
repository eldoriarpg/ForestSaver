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
public final class OriginalTagPart extends OriginalPart<Tag<Material>> implements MaterialProvider {
    @JsonIgnore
    Set<Material> materials;

    public OriginalTagPart() {
    }

    public OriginalTagPart(Tag<Material> original) {
        super(original);
    }

    @Override
    public boolean isCovered(BlockData blockData) {
        if (materials == null) {
            materials = new HashSet<>(original.getValues());
        }
        return false;
    }

    @Override
    public Set<Material> materials() {
        return Collections.unmodifiableSet(materials);
    }
}
