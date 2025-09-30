package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.data.data.Fragment;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class RestoreService implements Runnable {
    private final Plugin plugin;
    private final List<RestoreJob> jobs = new ArrayList<>();

    public RestoreService(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0, 5);
    }

    @Override
    public void run() {
        for (RestoreJob job : jobs) {
            Fragment next = job.next();
            if (next == null) {
                return;
            }
            next.restore(job.world());
        }
        jobs.removeIf(RestoreJob::isDone);
    }

    public void addJob(RestoreJob job) {
        jobs.add(job);
    }
}
