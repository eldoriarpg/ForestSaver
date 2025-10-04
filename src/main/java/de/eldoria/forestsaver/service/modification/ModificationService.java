package de.eldoria.forestsaver.service.modification;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.eldoria.eldoutilities.container.Pair;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Preset;
import de.eldoria.forestsaver.configuration.elements.Presets;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import de.eldoria.forestsaver.data.dao.Node;
import de.eldoria.forestsaver.data.dao.World;
import de.eldoria.forestsaver.service.restoration.RestoreService;
import de.eldoria.forestsaver.worldguard.ResourceFlag;
import dev.chojo.ocular.Configurations;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
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
    private final List<ResourceFlag> resourceFlags;
    private final RestoreService restoreService;
    private final Configurations<Configuration> configuration;
    private final Set<UUID> buildAllowed = new HashSet<>();

    private final Plugin plugin;
    private final de.eldoria.forestsaver.data.Worlds worlds;

    public ModificationService(Plugin plugin, de.eldoria.forestsaver.data.Worlds worlds, WorldGuard worldGuard, List<ResourceFlag> resourceFlags, RestoreService restoreService, Configurations<Configuration> configuration) {
        this.plugin = plugin;
        this.worlds = worlds;
        this.worldGuard = worldGuard;
        this.resourceFlags = resourceFlags;
        this.restoreService = restoreService;
        this.configuration = configuration;
    }

    /**
     * Allows the given player to build.
     *
     * @param uuid player to allow to build
     */
    public void allowBuild(UUID uuid) {
        buildAllowed.add(uuid);
    }

    /**
     * Disallows the given player to build.
     *
     * @param uuid player to disallow to build
     */
    public void disallowBuild(UUID uuid) {
        buildAllowed.remove(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (buildAllowed.contains(event.getPlayer().getUniqueId())) {
            // TOdO: delete fragments if exist
            return;
        }
        BlockState block = event.getBlock().getState();
        List<Pair<Preset, ResourceType>> presets = getFlagPresets(block);

        for (Pair<Preset, ResourceType> pair : presets) {
            Preset preset = pair.first;
            ResourceType type = pair.second;
            if (!preset.contains(block.getBlockData())) {
                event.setCancelled(true);
                continue;
            }
            if (event.isCancelled()) {
                event.setCancelled(true);
            }

            switch (type) {
                case GROWING -> handleGrowingBlockDestruction(event, block, preset);
                case FRAGMENT -> handleFragmentDestruction(event, block, preset);
                case NODE -> handleBlockDestruction(block, preset, type);
            }
            break;
        }
    }

    private void handleFragmentDestruction(BlockBreakEvent event, BlockState block, Preset preset) {
        Optional<BlockData> replacement = preset.replacement(block);
        if (replacement.isEmpty()) {
            worlds.getWorld(block.getWorld().getUID()).breakBlock(ResourceType.FRAGMENT, preset, block);
            return;
        }

        Player player = event.getPlayer();
        Vector direction = player.getEyeLocation().getDirection();
        Location subtract = block.getLocation().subtract(direction);
        for (ItemStack drop : block.getDrops(player.getActiveItem())) {
            block.getWorld().dropItemNaturally(subtract, drop);
        }

        block.setBlockData(replacement.get());
    }

    private void handleGrowingBlockDestruction(BlockBreakEvent event, BlockState block, Preset preset) {
        if (!(block instanceof Ageable ageable)) {
            return;
        }
        if (ageable.getAge() < ageable.getMaximumAge()) {
            event.setCancelled(true);
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            worlds.getWorld(block.getWorld().getUID()).breakBlock(ResourceType.GROWING, preset, block);
        });

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ageable.setAge(0);
            block.getBlock().setBlockData(ageable);
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrowth(BlockGrowEvent event) {
        getFlagPresets(event.getBlock().getState())
                .stream()
                .filter(p -> p.second == ResourceType.GROWING)
                .filter(p -> p.first.contains(event.getBlock().getBlockData()))
                .findFirst()
                .ifPresent(p -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (buildAllowed.contains(event.getPlayer().getUniqueId())) return;
        List<Pair<Preset, ResourceType>> flagValue = getFlagPresets(event.getBlock().getState());
        if (!flagValue.isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveDecay(LeavesDecayEvent event) {
        BlockState block = event.getBlock().getState();
        if (restoreService.isRestored(block)) {
            event.setCancelled(true);
            return;
        }

        List<Pair<Preset, ResourceType>> presetName = getFlagPresets(block);

        World world = this.worlds.getWorld(block.getWorld().getUID());
        for (Pair<Preset, ResourceType> pair : presetName) {
            Preset preset = pair.first;
            ResourceType type = pair.second;

            Optional<Node> optNode = world.getNode(block.getLocation());
            if (optNode.isPresent()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    optNode.get().breakBlock(block, preset);
                });
                return;
            }

            if (preset.contains(block.getBlockData())) {
                handleBlockDestruction(block, preset, type);
                break;
            }
        }


    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (restoreService.isRestored(event.getBlock().getState())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles destruction of the given block.
     * Creates a new node if necessary and logs the block.
     *
     * @param block  block that was destroyed
     * @param preset preset to use for node creation
     */
    private void handleBlockDestruction(BlockState block, Preset preset, ResourceType type) {
        // TODO: ugly af, maybe rework with some non existing threading framework
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = this.worlds.getWorld(block.getWorld().getUID());
            Optional<Node> optNode = world.getNode(block.getLocation());
            if (optNode.isPresent()) {
                optNode.get().breakBlock(block, preset);
                return;
            }

            Node node = world.createNode();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                List<BlockState> blocks = floodFill(block, preset);

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    node.addBlocks(blocks, preset, type);
                    node.breakBlock(block, preset);
                });
            });
        });
    }

    /**
     * Retrieves the set of regions applicable to the provided block.
     * The regions are determined using WorldGuard.
     *
     * @param block the block for which to retrieve applicable regions
     * @return the set of regions applicable to the given block
     */
    private ApplicableRegionSet getRegionsAtBlock(BlockState block) {
        ApplicableRegionSet applicableRegions = worldGuard.getPlatform().getRegionContainer()
                                                          .get(BukkitAdapter.adapt(block.getWorld()))
                                                          .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()), RegionQuery.QueryOption.COMPUTE_PARENTS);
        String collect = applicableRegions.getRegions().stream().map(ProtectedRegion::getId).collect(Collectors.joining(", "));
        plugin.getLogger().config("Found regions: " + collect);
        return applicableRegions;
    }

    /**
     * Retrieves the value of a specific flag for a given block.
     * If no flag value is explicitly set for the block, it falls back to a preset specified for the world.
     *
     * @param block the block for which the flag value is being queried
     * @return an Optional containing the flag value if defined, otherwise an empty Optional
     */
    private List<Pair<Preset, ResourceType>> getFlagPresets(BlockState block) {
        List<Pair<Preset, ResourceType>> result = new ArrayList<>();
        ApplicableRegionSet regionSet = getRegionsAtBlock(block);
        for (ResourceFlag resourceFlag : resourceFlags) {
            String queryValue = regionSet.queryValue(null, resourceFlag);
            if (queryValue != null) {
                plugin.getLogger().config("Found flag value: " + regionSet);
                configuration.secondary(Presets.KEY).getPreset(queryValue, resourceFlag.type())
                             .ifPresentOrElse(
                                     preset -> result.add(new Pair<>(preset, resourceFlag.type())),
                                     () -> plugin.getLogger().warning("No preset found for " + queryValue + " used in region " + regionSet.getRegions()));
                continue;
            }
            if (regionSet.getRegions().isEmpty()) {
                Optional<String> optPreset = configuration.main().worlds().presetFor(block.getWorld(), resourceFlag.type());
                if (optPreset.isEmpty()) continue;
                optPreset.flatMap(preset -> configuration.secondary(Presets.KEY).getPreset(preset, resourceFlag.type()))
                         .ifPresentOrElse(
                                 preset -> result.add(new Pair<>(preset, resourceFlag.type())),
                                 () -> plugin.getLogger().warning("No default preset found for type " + resourceFlag.type() + " in world " + block.getWorld().getName()));
            }
        }
        return result;
    }

    /**
     * Performs a flood-fill operation starting from the given block and including neighboring blocks
     * based on specific criteria such as distance, preset material types, and configuration limits.
     * The method stops adding blocks once the maximum allowed size is reached or if a block exceeds
     * the maximum allowable distance from the starting block.
     *
     * @param block  the starting block for the flood-fill operation
     * @param preset the preset containing materials and rules for determining which blocks to include
     * @return a list of {@link BlockState} objects that were included in the flood-fill
     */
    private List<BlockState> floodFill(BlockState block, Preset preset) {
        Queue<Distance> queue = new LinkedList<>();
        Set<BlockVector> visited = new HashSet<>();
        List<BlockState> blocks = new ArrayList<>();
        var maxSize = configuration.main().nodes().maxSize();
        var maxDistance = configuration.main().nodes().maxDistance();
        queue.add(new Distance(block, 0));

        while (!queue.isEmpty() && blocks.size() < maxSize) {
            Distance curBlock = queue.poll();
            if (curBlock.distance() > maxDistance) continue;

            blocks.add(curBlock.block());
            visited.add(curBlock.block().getLocation().toVector().toBlockVector());

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Location loc = curBlock.block().getLocation().clone().add(new Vector(x, y, z));
                        if (visited.contains(loc.toVector().toBlockVector())) continue;
                        BlockState nextBlock = loc.getBlock().getState();
                        if (preset.contains(nextBlock.getBlockData())) {
                            visited.add(nextBlock.getLocation().toVector().toBlockVector());
                            var nodeFlag = getFlagPresets(nextBlock).stream().filter(p -> p.second == ResourceType.NODE).findFirst();
                            if (nodeFlag.isEmpty()) continue;
                            if (nodeFlag.get().first.name().equals(preset.name())) {
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
