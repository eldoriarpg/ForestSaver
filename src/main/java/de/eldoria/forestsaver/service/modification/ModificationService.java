package de.eldoria.forestsaver.service.modification;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Preset;
import de.eldoria.forestsaver.data.Worlds;
import de.eldoria.forestsaver.data.dao.Node;
import de.eldoria.forestsaver.data.dao.World;
import de.eldoria.forestsaver.service.restoration.RestoreService;
import de.eldoria.forestsaver.worldguard.ForestFlag;
import dev.chojo.ocular.Configurations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
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
    private final WorldGuard worldGuard;
    private final ForestFlag flag;
    private final RestoreService restoreService;
    private final Configurations<Configuration> configuration;

    private final Plugin plugin;
    private final Worlds worlds;

    public ModificationService(Plugin plugin, Worlds worlds, WorldGuard worldGuard, ForestFlag flag, RestoreService restoreService, Configurations<Configuration> configuration) {
        this.plugin = plugin;
        this.worlds = worlds;
        this.worldGuard = worldGuard;
        this.flag = flag;
        this.restoreService = restoreService;
        this.configuration = configuration;
    }

    // TODO: what about stripping wood

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<String> presetName = getFlagValue(block);

        if (presetName.isEmpty()) return;

        Optional<Preset> preset = configuration.main().presets().getPreset(presetName.get());
        if (preset.isEmpty()) {
            plugin.getLogger().warning("No preset found for " + presetName.get() + " used in region " + getRegionsAtBlock(block));
            return;
        }

        Set<Material> materials = preset.get().combinedMaterials();

        if (!materials.contains(block.getType())) {
            event.setCancelled(true);
            return;
        }

        World world = worlds.getWorld(block.getWorld().getUID());

        Optional<Node> optNode = world.getNode(block.getLocation());
        if (optNode.isPresent()) {
            optNode.get().breakBlock(block);
            return;
        }

        registerNewNode(world, block, materials);
    }

    private void registerNewNode(World world, Block block, Set<Material> materials) {
        Node node = world.createNode();

        List<BlockState> blocks = floodFill(block, materials, 500, 100);

        // TODO: Off main thread theoretically
        node.addBlocks(blocks);
        node.breakBlock(block);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Optional<String> flagValue = getFlagValue(event.getBlock());
        if (flagValue.isPresent()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockStrip(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Optional<String> flagValue = getFlagValue(event.getClickedBlock());
        if (flagValue.isPresent()) {
            if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        Optional<String> presetName = getFlagValue(block);
        if (presetName.isPresent()) {
            if (restoreService.isRestored(block)) {
                event.setCancelled(true);
                return;
            }
            World world = worlds.getWorld(block.getWorld().getUID());

            Optional<Node> optNode = world.getNode(block.getLocation());
            if (optNode.isPresent()) {
                optNode.get().breakBlock(block);
                return;
            }

            Optional<Preset> preset = configuration.main().presets().getPreset(presetName.get());
            if (preset.isEmpty()) {
                plugin.getLogger().warning("No preset found for " + presetName.get() + " used in region " + getRegionsAtBlock(block));
                return;
            }

            Set<Material> materials = preset.get().combinedMaterials();

            registerNewNode(world, block, materials);
        }
    }

    private ApplicableRegionSet getRegionsAtBlock(Block block) {
        return worldGuard.getPlatform().getRegionContainer()
                         .get(BukkitAdapter.adapt(block.getWorld()))
                         .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()), RegionQuery.QueryOption.SORT);
    }

    private Optional<String> getFlagValue(Block block) {
        String queryValue = worldGuard.getPlatform().getRegionContainer()
                                      .get(BukkitAdapter.adapt(block.getWorld()))
                                      .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()), RegionQuery.QueryOption.SORT)
                                      .queryValue(null, flag);
        return Optional.ofNullable(queryValue);
    }

    private List<BlockState> floodFill(Block block, Set<Material> materials, int maxSize, int maxDistance) {
        Queue<Distance> queue = new LinkedList<>();
        Set<BlockVector> visited = new HashSet<>();
        List<BlockState> blocks = new ArrayList<>();

        queue.add(new Distance(block, 0));

        while (!queue.isEmpty() && blocks.size() < maxSize) {
            Distance curBlock = queue.poll();
            if (curBlock.distance() > maxDistance) continue;

            blocks.add(curBlock.block().getState());
            visited.add(curBlock.block().getLocation().toVector().toBlockVector());

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Location loc = curBlock.block().getLocation().clone().add(new Vector(x, y, z));
                        if (visited.contains(loc.toVector().toBlockVector())) continue;
                        Block nextBlock = loc.getBlock();
                        if (materials.contains(nextBlock.getType())) {
                            visited.add(nextBlock.getLocation().toVector().toBlockVector());
                            queue.add(new Distance(nextBlock, curBlock.distance() + 1));
                        }
                    }
                }
            }
        }

        plugin.getLogger().info("Created new node with " + blocks.size() + " blocks");

        return blocks;
    }

}
