package de.eldoria.forestsaver.service.modification;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class ModificationService implements Listener {
    private final WorldGuard worldGuard;
    private final ForestFlag flag;
    private final RestoreService restoreService;
    private final Configurations<Configuration> configuration;
    private final Set<UUID> buildAllowed = new HashSet<>();

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

    public void allowBuild(UUID uuid) {
        buildAllowed.add(uuid);
    }

    public void disallowBuild(UUID uuid) {
        buildAllowed.remove(uuid);
    }

    // TODO: what about stripping wood

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (buildAllowed.contains(event.getPlayer().getUniqueId())) return;
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

        handleBlockDestruction(block, preset.get());
    }

    private void registerNewNode(World world, Block block, Preset preset) {
        Node node = world.createNode();

        List<BlockState> blocks = floodFill(block, preset);

        // TODO: Off main thread theoretically
        node.addBlocks(blocks, preset);
        node.breakBlock(block);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (buildAllowed.contains(event.getPlayer().getUniqueId())) return;
        Optional<String> flagValue = getFlagValue(event.getBlock());
        if (flagValue.isPresent()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        Optional<String> presetName = getFlagValue(block);
        if (presetName.isEmpty()) return;

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

        handleBlockDestruction(block, preset.get());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (restoreService.isRestored(event.getBlock())) {
            event.setCancelled(true);
            return;
        }
    }

    private void handleBlockDestruction(Block block, Preset preset) {
        World world = worlds.getWorld(block.getWorld().getUID());

        Optional<Node> optNode = world.getNode(block.getLocation());
        if (optNode.isPresent()) {
            optNode.get().breakBlock(block);
            return;
        }

        registerNewNode(world, block, preset);
    }

    private ApplicableRegionSet getRegionsAtBlock(Block block) {
        ApplicableRegionSet applicableRegions = worldGuard.getPlatform().getRegionContainer()
                                                          .get(BukkitAdapter.adapt(block.getWorld()))
                                                          .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()), RegionQuery.QueryOption.COMPUTE_PARENTS);
        String collect = applicableRegions.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.joining(", "));
        plugin.getLogger().config("Found regions: " + collect);
        return applicableRegions;
    }

    private Optional<String> getFlagValue(Block block) {
        ApplicableRegionSet regionSet = getRegionsAtBlock(block);
        String queryValue = regionSet.queryValue(null, flag);
        if (queryValue == null) {
            plugin.getLogger().config("Found flag value: " + regionSet);
        }
        Optional<String> value = Optional.ofNullable(queryValue);
        if (regionSet.getRegions().isEmpty()) {
            return value.or(() -> configuration.main().worlds().presetFor(block.getWorld()));
        }
        return value;
    }

    private List<BlockState> floodFill(Block block, Preset preset) {
        Queue<Distance> queue = new LinkedList<>();
        Set<BlockVector> visited = new HashSet<>();
        List<BlockState> blocks = new ArrayList<>();
        var maxSize = configuration.main().nodes().maxSize();
        var maxDistance = configuration.main().nodes().maxDistance();
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
                        if (preset.contains(nextBlock.getType())) {
                            visited.add(nextBlock.getLocation().toVector().toBlockVector());
                            Optional<String> flagValue = getFlagValue(nextBlock);
                            if (flagValue.isEmpty()) continue;
                            if (flagValue.get().equals(preset.name())) {
                                queue.add(new Distance(nextBlock, curBlock.distance() + 1));
                            }
                        }
                    }
                }
            }
        }

        plugin.getLogger().info("Created new node with " + blocks.size() + " blocks");

        return blocks;
    }

}
