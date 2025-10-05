package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bukkit.block.data.BlockData;

@JsonTypeName("block_data")
public final class OriginalBlockDataPart extends OriginalPart<BlockData> {

    public OriginalBlockDataPart() {
    }

    public OriginalBlockDataPart(BlockData original) {
        super(original);
    }

    @Override
    public boolean isCovered(BlockData blockData) {
        return blockData == original;
    }
}
