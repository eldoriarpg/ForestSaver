package de.eldoria.forestsaver.configuration.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.HashSet;
import java.util.Set;

public class Preset {
    String name;
    Set<Tag<Material>> tags = new HashSet<>();
    Set<String> materials = new HashSet<>();
    Set<Material> alwaysRestore = new HashSet<>();
    @JsonIgnore
    Set<Material> combinedMaterials = null;

    public Preset() {
    }

    public Preset(String name, Set<Tag<Material>> tags, Set<String> materials) {
        this.name = name;
        this.tags = tags;
        this.materials = materials;
    }

    public Set<Material> combinedMaterials() {
        if (combinedMaterials == null) {
            combinedMaterials = new HashSet<>();
            combinedMaterials.addAll(materials.stream().map(Material::getMaterial).toList());
            combinedMaterials.addAll(alwaysRestore);
            combinedMaterials.addAll(tags.stream().flatMap(tag -> tag.getValues().stream()).toList());
        }
        return combinedMaterials;
    }

    public String name() {
        return name;
    }

    public boolean contains(Material material) {
        return combinedMaterials().contains(material);
    }

    public boolean alwaysRestore(Material material){
        return alwaysRestore.contains(material);
    }
}
