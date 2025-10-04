package de.eldoria.forestsaver.configuration.elements.presets.replacements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.bukkit.block.data.BlockData;

@JsonTypeInfo(visible = true, property = "type", use = JsonTypeInfo.Id.NAME)
@JsonPropertyOrder(alphabetic = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockDataReplacement.class, name = "blockdata"),
        @JsonSubTypes.Type(value = MaterialReplacement.class, name = "material"),
})
@JsonIgnoreProperties({"type"})
public sealed abstract class Replacement<T> permits BlockDataReplacement, MaterialReplacement {
    T replacement;

    public Replacement() {
    }

    public Replacement(T replacement) {
        this.replacement = replacement;
    }

    public abstract BlockData blockData();
}
