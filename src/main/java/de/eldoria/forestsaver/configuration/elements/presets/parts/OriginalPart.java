package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bukkit.block.data.BlockData;

@JsonTypeInfo(visible = true, property = "type", use = JsonTypeInfo.Id.NAME)
@JsonPropertyOrder(alphabetic = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OriginalBlockDataPart.class, name = "blockdata"),
        @JsonSubTypes.Type(value = OriginalMaterialPart.class, name = "material"),
        @JsonSubTypes.Type(value = OriginalTagPart.class, name = "tag"),
})
@JsonIgnoreProperties({"type"})
public sealed abstract class OriginalPart<T> permits OriginalBlockDataPart, OriginalTagPart, OriginalMaterialPart, Part {
    /**
     * The destroyed block.
     */
    T original;

    public OriginalPart(T original) {
        this.original = original;
    }

    public OriginalPart() {
    }

    public abstract boolean isCovered(BlockData blockData);

}
