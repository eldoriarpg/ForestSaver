package de.eldoria.forestsaver.service.modification;

import de.eldoria.forestsaver.data.Worlds;
import de.eldoria.forestsaver.data.data.Node;
import de.eldoria.forestsaver.data.data.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class ModificationService implements Listener {
    Set<Tag<Material>> tags = new HashSet<>();
    Set<Material> taggedMaterial = new HashSet<>();

    private final Worlds worlds;

    public ModificationService(Worlds worlds, Set<Tag<Material>> tags) {
        this.worlds = worlds;
        tags.stream().flatMap(tag -> tag.getValues().stream()).forEach(taggedMaterial::add);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getType().isBlock()) return;

        if (!taggedMaterial.contains(block.getType())) return;

        World world = worlds.getWorld(block.getWorld().getUID());

        Optional<Node> optNode = world.getNode(block.getLocation());
        if (optNode.isPresent()) {
            optNode.get().breakBlock(block);
            return;
        }

        Node node = world.createNode();

        List<BlockState> blocks = floodFill(block, 200, 50);

        // TODO: Off main thread theoretically
        node.addBlocks(blocks);
        node.breakBlock(block);
    }

    private List<BlockState> floodFill(Block block, int maxSize, int maxDistance) {
        Queue<Distance> queue = new LinkedList<>();
        Set<BlockVector> visited = new HashSet<>();
        List<BlockState> blocks = new ArrayList<>();

        var world = block.getWorld();

        queue.add(new Distance(block, 0));

        while (!queue.isEmpty() && blocks.size() < maxSize) {
            Distance distance = queue.poll();
            if (distance.distance() > maxDistance) continue;

            blocks.add(distance.block().getState());
            visited.add(distance.block().getLocation().toVector().toBlockVector());

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Location loc = distance.block().getLocation().add(new Vector(x, y, z));
                        if (visited.contains(loc.toVector().toBlockVector())) continue;
                        Block curBlock = world.getBlockAt(loc);
                        if (taggedMaterial.contains(curBlock.getType())) {
                            queue.add(new Distance(curBlock, distance.distance() + 1));
                        }
                    }
                }
            }
        }

        return blocks;
    }

    private record Distance(Block block, int distance) {
    }
}
