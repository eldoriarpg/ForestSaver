package de.eldoria.forestsaver.configuration.elements.presets.parts;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.eldoria.forestsaver.configuration.elements.presets.replacements.Replacement;
import org.bukkit.block.data.BlockData;

@JsonTypeName("block_data")
public final class BlockDataPart extends Part<BlockData> {

    public BlockDataPart() {
    }

    public BlockDataPart(BlockData original, Replacement<?> replacement, int restoreTokens, boolean doNotRestore, int restoreTokenJitter) {
        super(original, replacement, restoreTokens, doNotRestore, restoreTokenJitter);
    }

    @Override
    public boolean isCovered(BlockData blockData) {
        return blockData == original;
    }
}
