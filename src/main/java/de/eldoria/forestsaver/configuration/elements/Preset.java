package de.eldoria.forestsaver.configuration.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.HashSet;
import java.util.Set;

public class Preset {
    /**
     * Name/id of the preset.
     */
    String name;
    /**
     * Tags that provide materials associated with the preset.
     *
     * @see Tag
     */
    Set<Tag<Material>> tags = new HashSet<>();
    /**
     * Materials associated with the preset.
     */
    Set<String> materials = new HashSet<>();
    /**
     * Materials that should always be restored no matter whether they were destroyed or not.
     * This is especially useful for blocks that are not destroyed via a BlockBreakEvent like {@link Material#VINE}
     */
    Set<Material> alwaysRestore = new HashSet<>();
    /**
     * Cached combined materials of the preset.
     * <p>
     * The sum of materials from {@link #tags}, {@link #materials} and {@link #alwaysRestore}.
     */
    @JsonIgnore
    Set<Material> combinedMaterials = null;

    public Preset() {
    }

    public Preset(String name, Set<Tag<Material>> tags, Set<String> materials) {
        this.name = name;
        this.tags = tags;
        this.materials = materials;
    }

    /**
     * Returns the combined materials of the preset.
     * @return combined materials
     */
    public Set<Material> combinedMaterials() {
        if (combinedMaterials == null) {
            combinedMaterials = new HashSet<>();
            combinedMaterials.addAll(materials.stream().map(Material::getMaterial).toList());
            combinedMaterials.addAll(alwaysRestore);
            combinedMaterials.addAll(tags.stream().flatMap(tag -> tag.getValues().stream()).toList());
        }
        return combinedMaterials;
    }

    /**
     * Returns the name of the preset.
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Checks whether the preset contains the given material.
     * @param material material to check
     * @return true if the preset contains the material, false otherwise
     */
    public boolean contains(Material material) {
        return combinedMaterials().contains(material);
    }

    /**
     * Checks whether the given material should always be restored.
     * @param material material to check
     * @return true if the material should always be restored, false otherwise
     */
    public boolean alwaysRestore(Material material) {
        return alwaysRestore.contains(material);
    }
}
