package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.data.dao.Fragment;
import de.eldoria.forestsaver.data.dao.Node;
import dev.chojo.ocular.Configurations;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RestoreService {
    private final Plugin plugin;
    private final List<RestoreJob> jobs = new ArrayList<>();
    private final Configurations<Configuration> configuration;

    public RestoreService(Plugin plugin, Configurations<Configuration> configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
        // TODO: configuration
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::processRestore, 0, configuration.main().restore().ticksPerBlock());
        // TODO: configuration
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::restoreCheck, 0, configuration.main().restore().checks());
    }

    private void restoreCheck() {

    }

    public void processRestore() {
        for (RestoreJob job : jobs) {
            Fragment next = job.next();
            if (next == null) return;
            next.restore(job.world());
        }
        jobs.removeIf(RestoreJob::isDone);
    }

    public boolean isRestored(Block block) {
        for (RestoreJob job : jobs) {
            if (job.contains(block.getLocation().toVector().toBlockVector())) {
                return true;
            }
        }
        return false;
    }

    // TODO: off main thread
    public void restoreNode(Node node, boolean full) {
        List<Fragment> fragments;
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
    }

    public void addJob(RestoreJob job) {
        if (jobs.stream().anyMatch(j -> j.node().id() == job.node().id())) return;
        jobs.add(job);
    }
}
