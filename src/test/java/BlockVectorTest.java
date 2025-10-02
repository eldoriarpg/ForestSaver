import org.bukkit.util.BlockVector;

import java.util.HashSet;
import java.util.List;

public class BlockVectorTest {
    public static void main(String[] args) {
        List<BlockVector> blockVectors = List.of(
                new BlockVector(0, 0, 0),
                new BlockVector(1, 0, 0),
                new BlockVector(0, 1, 0),
                new BlockVector(0, 0, 1),
                new BlockVector(1, 1, 1)
        );

        HashSet<BlockVector> objects = new HashSet<>();

        for (BlockVector blockVector : blockVectors) {
            System.out.println(blockVector.toString() + " has hash: " + blockVector.hashCode());
            if (objects.contains(blockVector)) continue;
            objects.add(blockVector);
        }
        System.out.println("objects: " + objects.size());
    }
}
