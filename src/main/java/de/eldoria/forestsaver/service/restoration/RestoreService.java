package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import de.eldoria.forestsaver.data.Nodes;
import de.eldoria.forestsaver.data.Worlds;
import de.eldoria.forestsaver.data.dao.Fragment;
import de.eldoria.forestsaver.data.dao.Node;
import de.eldoria.forestsaver.data.dao.NodeFragment;
import dev.chojo.ocular.Configurations;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RestoreService {
    private final Plugin plugin;
    private final Nodes nodes;
    private final Worlds worlds;
    private final Queue<RestoreJob> jobs = new LinkedList<>();
    private final Configurations<Configuration> configuration;
    private float tickProgress = 0;

    public RestoreService(Plugin plugin, Nodes nodes, Worlds worlds, Configurations<Configuration> configuration) {
        this.plugin = plugin;
        this.nodes = nodes;
        this.worlds = worlds;
        this.configuration = configuration;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::processRestore, 0, 1);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::restoreCheck, 0, configuration.main().restore().checkForRestore());
    }

    private void restoreCheck() {
        for (World world : plugin.getServer().getWorlds()) {
            var resourceWorld = this.worlds.getWorld(world.getUID());
            List<Node> idleNodes = resourceWorld.idleNodes();
            for (Node node : idleNodes) {
                restoreNode(node, false);
            }
            for (ResourceType type : List.of(ResourceType.GROWING, ResourceType.FRAGMENT)) {
                List<Fragment> fragments = resourceWorld.idleFragments(type);
                addJob(new RestoreJob(world, null, new LinkedList<>(fragments)));
            }
        }

        nodes.deleteUnusedNodes();
    }

    private void processRestore() {
        float jobSize = (float) jobs.size() / configuration.main().restore().ticksPerBlock();
        tickProgress += jobSize;
        long start = System.currentTimeMillis();
        while (tickProgress > 1 && !jobs.isEmpty() && System.currentTimeMillis() - start < 5) {
            tickProgress--;
            RestoreJob job = jobs.poll();
            jobs.add(job);
            Fragment next = job.next();
            if (next == null) {
                job.finish();
                jobs.remove(job);
                continue;
            }
            boolean nearbyPlayer = !job.world().getNearbyEntitiesByType(Player.class, next.position().toLocation(job.world()), configuration.main().restore().minPlayerDistance()).isEmpty();
            if (nearbyPlayer) continue;
            if (!next.position().toLocation(job.world()).isChunkLoaded()) {
                job.world().getChunkAtAsync(next.position().toLocation(job.world()))
                   .thenAccept(chunk -> next.restore(chunk.getWorld()));
                continue;
            }
            next.restore(job.world());
        }
    }

    /**
     * Checks whether the given block is currently being restored.
     *
     * @param block block to check
     * @return true if the block is currently being restored, false otherwise
     */
    public boolean isRestored(BlockState block) {
        for (RestoreJob job : jobs) {
            if (job.contains(block.getLocation().toVector().toBlockVector())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restores the given node.
     *
     * @param node node to restore
     * @param full whether to restore all fragments or only the destroyed fragments
     */
    public void restoreNode(Node node, boolean full) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<NodeFragment> fragments;
            if (full) {
                fragments = node.fragments();
            } else {
                fragments = node.destroyedFragments();
            }
            World world = plugin.getServer().getWorld(node.world());
            if (world == null) return;
            LinkedList<Fragment> restoreOrder = new LinkedList<>(fragments);
            configuration.main().restore().restoreOrder().sort(restoreOrder);
            addJob(new RestoreJob(world, node, restoreOrder));
        });
    }

    private synchronized void addJob(RestoreJob job) {
        if (jobs.stream().anyMatch(j -> j.node().id() == job.node().id())) return;
        jobs.add(job);
    }
}
