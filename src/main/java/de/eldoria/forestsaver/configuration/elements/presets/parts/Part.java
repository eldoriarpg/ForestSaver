package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.eldoria.forestsaver.configuration.elements.presets.replacements.Replacement;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

@JsonTypeInfo(visible = true, property = "type", use = JsonTypeInfo.Id.NAME)
@JsonPropertyOrder(alphabetic = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockDataPart.class, name = "blockdata"),
        @JsonSubTypes.Type(value = MaterialPart.class, name = "material"),
        @JsonSubTypes.Type(value = TagPart.class, name = "tag"),
})
@JsonIgnoreProperties({"type"})
public sealed abstract class Part<T> extends OriginalPart<T> permits BlockDataPart, MaterialPart, TagPart {
    /**
     * Possible replacement for the destroyed block.
     */
    @Nullable
    Replacement<?> replacement;
    /**
     * Number of tokens to be generated for the part to be restored.
     * Only values above 0 have an effect
     */
    int restoreTokens = 0;
    /**
     * A number of tokens to be added or subtracted from the total number of tokens.
     * Only values above 0 have an effect
     */
    int restoreTokenJitter = 0;

    /**
     * If true, the block will not be restored or recorded, but allowed to be broken.
     */
    boolean doNotRestore;

    public Part() {
    }

    public Part(T original, Replacement<?> replacement, int restoreTokens, boolean doNotRestore, int restoreTokenJitter) {
        super(original);
        this.replacement = replacement;
        this.restoreTokens = restoreTokens;
        this.doNotRestore = doNotRestore;
        this.restoreTokenJitter = restoreTokenJitter;
    }

    @Nullable
    public BlockData replacementData() {
        if (replacement == null) return null;
        return replacement.blockData();
    }

    public void replacement(Replacement<?> replacement) {
        this.replacement = replacement;
    }

    public T original() {
        return original;
    }

    public int restoreTokens() {
        return restoreTokens;
    }

    public int getRestoreTokenJitter() {
        return restoreTokenJitter;
    }
}
