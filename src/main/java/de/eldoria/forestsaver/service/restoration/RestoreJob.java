package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.data.data.Fragment;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class RestoreJob {
    private final World world;
    private final Queue<Fragment> fragments;

    public RestoreJob(World world, Queue<Fragment> fragments) {
        this.world = world;
        this.fragments = fragments;
    }

    @Nullable
    public Fragment next() {
        return fragments.poll();
    }

    public boolean isDone() {
        return fragments.isEmpty();
    }

    public World world() {
        return null;
    }
}
