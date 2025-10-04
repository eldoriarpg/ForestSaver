package de.eldoria.forestsaver.configuration.elements.presets.replacements;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bukkit.block.data.BlockData;

@JsonTypeName("block_data")
public final class BlockDataReplacement  extends Replacement<BlockData> {
    @Override
    public BlockData blockData() {
        return replacement;
    }
}
