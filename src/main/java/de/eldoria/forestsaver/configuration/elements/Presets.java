package de.eldoria.forestsaver.configuration.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.eldoria.forestsaver.ForestSaverPlugin;
import de.eldoria.forestsaver.configuration.elements.presets.parts.BlockDataPart;
import de.eldoria.forestsaver.configuration.elements.presets.parts.MaterialPart;
import de.eldoria.forestsaver.configuration.elements.presets.parts.OriginalMaterialPart;
import de.eldoria.forestsaver.configuration.elements.presets.parts.TagPart;
import de.eldoria.forestsaver.configuration.elements.presets.replacements.MaterialReplacement;
import dev.chojo.ocular.key.Key;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.structure.StructureRotation;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Presets {
    public static final Key<Presets> KEY = Key.builder(Path.of("presets.yaml"), Presets::new).build();
    /**
     * A map representation of the preset list.
     * <p>
     * The map is created on demand.
     */
    @JsonIgnore
    private Map<String, Preset> nodePresetMap = null;
    /**
     * A map representation of the preset list.
     * <p>
     * The map is created on demand.
     */
    @JsonIgnore
    private Map<String, Preset> growingPresetMap = null;
    /**
     * A map representation of the preset list.
     * <p>
     * The map is created on demand.
     */
    @JsonIgnore
    private Map<String, Preset> fragmentPresetMap = null;
    /**
     * A list of presets.
     */
    private List<Preset> nodePresets = List.of(
            new Preset("default",
                    Set.of(new TagPart(Tag.LOGS, null, 100, false, 0),
                            new TagPart(Tag.LEAVES, null, 100, false, 0),
                            new MaterialPart(Material.BEE_NEST, null, 100, false, 0),
                            new BlockDataPart(Material.BIRCH_STAIRS.createBlockData(c -> c.rotate(StructureRotation.CLOCKWISE_90)), null, 100, false, 0)),
                    Set.of(new OriginalMaterialPart(Material.VINE)),
                    100, 20));

    private List<Preset> growingPresets = List.of(
            new Preset("default",
                    Set.of(new TagPart(Tag.CROPS, null, 100, false, 0),
                            new MaterialPart(Material.COCOA, null, 100, false, 0)),
                    Set.of(), 100, 20));

    private List<Preset> fragmentPresets = List.of(
            new Preset("default",
                    Set.of(
                            new MaterialPart(Material.COAL_ORE, new MaterialReplacement(Material.STONE), 20, false, 0),
                            new MaterialPart(Material.IRON_ORE, new MaterialReplacement(Material.STONE), 50, false, 0),
                            new MaterialPart(Material.COPPER_ORE, new MaterialReplacement(Material.STONE), 50, false, 0),
                            new MaterialPart(Material.DIAMOND_ORE, new MaterialReplacement(Material.STONE), 500, false, 0),
                            new MaterialPart(Material.REDSTONE_ORE, new MaterialReplacement(Material.STONE), 100, false, 0),
                            new MaterialPart(Material.LAPIS_ORE, new MaterialReplacement(Material.STONE), 100, false, 0),
                            new MaterialPart(Material.EMERALD_ORE, new MaterialReplacement(Material.STONE), 1000, false, 0),
                            new MaterialPart(Material.GOLD_ORE, new MaterialReplacement(Material.STONE), 200, false, 0),
                            new MaterialPart(Material.DEEPSLATE_COAL_ORE, new MaterialReplacement(Material.DEEPSLATE), 20, false, 0),
                            new MaterialPart(Material.DEEPSLATE_IRON_ORE, new MaterialReplacement(Material.DEEPSLATE), 50, false, 0),
                            new MaterialPart(Material.DEEPSLATE_COPPER_ORE, new MaterialReplacement(Material.DEEPSLATE), 50, false, 0),
                            new MaterialPart(Material.DEEPSLATE_DIAMOND_ORE, new MaterialReplacement(Material.DEEPSLATE), 500, false, 0),
                            new MaterialPart(Material.DEEPSLATE_REDSTONE_ORE, new MaterialReplacement(Material.DEEPSLATE), 100, false, 0),
                            new MaterialPart(Material.DEEPSLATE_LAPIS_ORE, new MaterialReplacement(Material.DEEPSLATE), 100, false, 0),
                            new MaterialPart(Material.DEEPSLATE_EMERALD_ORE, new MaterialReplacement(Material.DEEPSLATE), 1000, false, 0),
                            new MaterialPart(Material.DEEPSLATE_GOLD_ORE, new MaterialReplacement(Material.DEEPSLATE), 200, false, 0)
                    ),
                    Set.of(), 100, 20));

    /**
     * Returns the preset with the given name.
     *
     * @param name Name of the preset.
     * @return Optional of preset, or empty optional if no preset with the given name exists.
     */
    public Optional<Preset> getPreset(String name, ResourceType type) {
        if (name == null) return Optional.empty();
        return switch (type) {
            case GROWING -> getGrowingPreset(name);
            case FRAGMENT -> getFragmentPreset(name);
            case NODE -> getNodePreset(name);
        };
    }

    public Optional<Preset> getNodePreset(String name) {
        return Optional.ofNullable(nodePresetMap().get(name));
    }

    public Optional<Preset> getGrowingPreset(String name) {
        return Optional.ofNullable(growingPresetMap().get(name));
    }

    public Optional<Preset> getFragmentPreset(String name) {
        return Optional.ofNullable(fragmentPresetMap().get(name));
    }

    public Map<String, Preset> nodePresetMap() {
        if (nodePresetMap == null) {
            nodePresetMap = nodePresets.stream().collect(Collectors.toMap(Preset::name, e -> e, (e1, e2) -> {
                ForestSaverPlugin.getInstance().getLogger().warning("Duplicate preset name: " + e1.name());
                return e1;
            }, HashMap::new));
        }
        return nodePresetMap;
    }

    public Map<String, Preset> growingPresetMap() {
        if (growingPresetMap == null) {
            growingPresetMap = growingPresets.stream().collect(Collectors.toMap(Preset::name, e -> e, (e1, e2) -> {
                ForestSaverPlugin.getInstance().getLogger().warning("Duplicate preset name: " + e1.name());
                return e1;
            }, HashMap::new));
        }
        return growingPresetMap;
    }

    public Map<String, Preset> fragmentPresetMap() {
        if (fragmentPresetMap == null) {
            fragmentPresetMap = fragmentPresets.stream().collect(Collectors.toMap(Preset::name, e -> e, (e1, e2) -> {
                ForestSaverPlugin.getInstance().getLogger().warning("Duplicate preset name: " + e1.name());
                return e1;
            }, HashMap::new));
        }
        return fragmentPresetMap;
    }


    /**
     * Returns a collection of all preset names.
     *
     * @return Collection of preset names.
     */
    public Collection<String> names(ResourceType type) {
        return switch (type) {
            case GROWING -> growingPresetMap().keySet();
            case FRAGMENT -> fragmentPresetMap().keySet();
            case NODE -> nodePresetMap().keySet();
        };
    }
}
