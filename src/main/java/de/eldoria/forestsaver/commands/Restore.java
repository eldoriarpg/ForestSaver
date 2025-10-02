package de.eldoria.forestsaver.commands;

import de.eldoria.forestsaver.data.Nodes;
import de.eldoria.forestsaver.data.dao.Node;
import de.eldoria.forestsaver.service.restoration.RestoreService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Default;
import org.incendo.cloud.annotations.Flag;

import java.util.Optional;

public class Restore {
    private final Nodes nodes;
    private final RestoreService restoreService;

    public Restore(Nodes nodes, RestoreService restoreService) {
        this.nodes = nodes;
        this.restoreService = restoreService;
    }

    @Command(value = "node restore <node> [full]")
    public void restore(CommandSourceStack stack, @Argument("node") int nodeId, @Argument("full") @Default("false") boolean full) {
        nodes.getNode(nodeId).ifPresentOrElse(node -> restoreService.restoreNode(node, full), () -> stack.getSender().sendRichMessage("Node not found"));
    }

    @Command(value = "node restoreall [world]")
    public void restore(CommandSourceStack stack, @Argument("world") World world, @Flag("full") boolean full) {
        if (stack.getSender() instanceof Player player) {
            if (world == null) world = player.getWorld();
            nodes.all(world).forEach(node -> restoreService.restoreNode(node, full));
        } else {
            nodes.all(world).forEach(node -> restoreService.restoreNode(node, full));

        }
    }

    @Command(value = "node identify")
    public void identify(CommandSourceStack stack) {
        if (!(stack.getSender() instanceof Player player)) return;
        RayTraceResult rayTraceResult = player.rayTraceBlocks(100);
        if (rayTraceResult.getHitBlock() == null) {
            player.sendRichMessage("No block found");
            return;
        }
        Optional<Node> node = nodes.getNode(rayTraceResult.getHitBlock().getLocation());
        node.ifPresentOrElse(n -> player.sendRichMessage("Node found: " + n.id()),
                () -> player.sendRichMessage("No node found"));
    }
}
