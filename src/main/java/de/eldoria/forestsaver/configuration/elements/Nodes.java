package de.eldoria.forestsaver.configuration.elements;

public class Nodes {
    /**
     * Max distance of a block to the root block to be considered part or the node.
     */
    int maxDistance = 100;

    /**
     * Max number of blocks in a node.
     */
    int maxSize = 1000;

    /**
     * Max distance of a block to the root block to be considered part or the node.
     * @return maxDistance
     */
    public int maxDistance() {
        return maxDistance;
    }

    /**
     * Max number of blocks in a node.
     * @return maxSize
     */
    public int maxSize() {
        return maxSize;
    }
}
