package de.eldoria.forestsaver.configuration.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.eldoria.forestsaver.configuration.elements.presets.parts.BlockDataPart;
import de.eldoria.forestsaver.configuration.elements.presets.parts.MaterialProvider;
import de.eldoria.forestsaver.configuration.elements.presets.parts.OriginalPart;
import de.eldoria.forestsaver.configuration.elements.presets.parts.Part;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
    Set<Part<?>> parts = new HashSet<>();

    /**
     * Materials that should always be restored no matter whether they were destroyed or not.
     * This is especially useful for blocks that are not destroyed via a BlockBreakEvent like {@link Material#VINE}
     */
    Set<OriginalPart<?>> alwaysRestore = new HashSet<>();

    /**
     * Number of tokens to be generated for a fragment of the preset to be restored.
     */
    int restoreTokens = 100;

    int restoreTokenJitter = 20;


    /**
     * Cached combined materials of the preset.
     * <p>
     * The sum of materials from {@link #parts} and {@link #alwaysRestore}.
     */
    @JsonIgnore
    Set<Material> coveredMaterials = null;
    @JsonIgnore
    Set<BlockData> coveredBlockData = null;

    /**
     * Cached combined materials of the preset.
     * <p>
     * The sum of materials from {@link #parts} and {@link #alwaysRestore}.
     */
    @JsonIgnore
    Set<Material> alwaysRestoreMaterials = null;
    @JsonIgnore
    Set<BlockData> alwaysRestoreBlockData = null;

    public Preset() {
    }

    public Preset(String name, Set<Part<?>> parts, Set<OriginalPart<?>> alwaysRestore, int restoreTokens, int restoreTokenJitter) {
        this.name = name;
        this.parts = parts;
        this.alwaysRestore = alwaysRestore;
        this.restoreTokens = restoreTokens;
        this.restoreTokenJitter = restoreTokenJitter;
    }

    /**
     * Returns the combined materials of the preset.
     *
     * @return combined materials
     */
    public Set<Material> coveredMaterials() {
        if (coveredMaterials == null) {
            coveredMaterials = new HashSet<>();
            for (Part<?> fragment : parts) {
                if (fragment instanceof MaterialProvider data) {
                    coveredMaterials.addAll(data.materials());
                }
            }

            for (OriginalPart<?> fragment : alwaysRestore) {
                if (fragment instanceof MaterialProvider data) {
                    coveredMaterials.addAll(data.materials());
                }
            }
        }
        return coveredMaterials;
    }

    /**
     * Returns the combined materials of the preset.
     *
     * @return combined materials
     */
    public Set<BlockData> combinedBlockData() {
        if (coveredBlockData == null) {
            coveredBlockData = new HashSet<>();
            for (Part<?> fragment : parts) {
                if (fragment instanceof BlockDataPart data) {
                    coveredBlockData.add(data.original());
                }
            }
            for (OriginalPart<?> fragment : alwaysRestore) {
                if (fragment instanceof BlockDataPart data) {
                    coveredBlockData.add(data.original());
                }
            }
        }
        return coveredBlockData;
    }

    /**
     * Returns the combined materials of the preset.
     *
     * @return combined materials
     */
    public Set<Material> alwaysRestoreMaterials() {
        if (alwaysRestoreMaterials == null) {
            alwaysRestoreMaterials = new HashSet<>();
            for (OriginalPart<?> fragment : alwaysRestore) {
                if (fragment instanceof MaterialProvider data) {
                    alwaysRestoreMaterials.addAll(data.materials());
                }
            }
        }
        return alwaysRestoreMaterials;
    }

    /**
     * Returns the combined materials of the preset.
     *
     * @return combined materials
     */
    public Set<BlockData> alwaysRestoreBlockData() {
        if (alwaysRestoreBlockData == null) {
            alwaysRestoreBlockData = new HashSet<>();
            for (OriginalPart<?> fragment : alwaysRestore) {
                if (fragment instanceof BlockDataPart data) {
                    alwaysRestoreBlockData.add(data.original());
                }
            }
        }
        return alwaysRestoreBlockData;
    }

    /**
     * Returns the name of the preset.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Checks whether the preset contains the given material.
     *
     * @param material material to check
     * @return true if the preset contains the material, false otherwise
     */
    public boolean contains(BlockData material) {
        return coveredMaterials().contains(material.getMaterial()) || combinedBlockData().contains(material);
    }

    /**
     * Checks whether the given material should always be restored.
     *
     * @param material material to check
     * @return true if the material should always be restored, false otherwise
     */
    public boolean alwaysRestore(BlockData material) {
        return alwaysRestore.contains(material);
    }

    public int restoreToken(@NotNull BlockState block) {
        for (Part<?> part : parts) {
            if (part.isCovered(block.getBlockData())) {
                int restoreTokens = part.restoreTokens();
                int restoreTokenJitter = part.getRestoreTokenJitter();
                if (restoreTokens == 0) restoreTokens = this.restoreTokens;
                if (restoreTokenJitter == 0) restoreTokenJitter = this.restoreTokenJitter;
                if (restoreTokenJitter == 0) return restoreTokens;
                return restoreTokens + ThreadLocalRandom.current().nextInt(-restoreTokenJitter, restoreTokenJitter);
            }
        }
        // shouldn't happen
        return restoreTokens + ThreadLocalRandom.current().nextInt(-restoreTokenJitter, restoreTokenJitter);
    }

    public Optional<BlockData> replacement(BlockState block) {
        for (Part<?> part : parts) {
            if (part.isCovered(block.getBlockData())) return Optional.ofNullable(part.replacementData());
        }
        // shouldn't happen
        return Optional.empty();
    }
}
