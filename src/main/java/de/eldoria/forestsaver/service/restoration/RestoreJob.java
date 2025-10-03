package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.data.dao.Fragment;
import de.eldoria.forestsaver.data.dao.Node;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public final class RestoreJob {
    private final World world;
    private final Node node;
    private final Queue<Fragment> fragments;
    private final Set<BlockVector> restoration;
    private final List<Fragment> once;

    public RestoreJob(World world, Node node, Queue<Fragment> fragments) {
        this.world = world;
        this.node = node;
        this.once = fragments.stream().filter(e -> {
            BlockData blockData = e.blockData();
            return !blockData.getMaterial().hasGravity() && !blockData.getMaterial().isSolid();
        }).toList();
        this.fragments = fragments;
        this.fragments.removeIf(e -> {
            BlockData blockData = e.blockData();
            return !blockData.getMaterial().hasGravity() && !blockData.getMaterial().isSolid();
        });
        restoration = fragments.stream().map(Fragment::position).collect(Collectors.toSet());
    }

    public boolean contains(BlockVector position) {
        return restoration.contains(position);
    }

    @Nullable
    public Fragment next() {
        return fragments.poll();
    }

    public boolean isDone() {
        return fragments.isEmpty();
    }

    public World world() {
        return world;
    }

    public Node node() {
        return node;
    }

    public Queue<Fragment> fragments() {
        return fragments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RestoreJob) obj;
        return Objects.equals(this.world, that.world) &&
               Objects.equals(this.node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, node);
    }

    @Override
    public String toString() {
        return "RestoreJob[" +
               "world=" + world + ", " +
               "node=" + node + ", " +
               "fragments=" + fragments + ']';
    }

    public void finish() {
        for (Fragment fragment : once) {
            fragment.restore(world);
        }
    }
}
