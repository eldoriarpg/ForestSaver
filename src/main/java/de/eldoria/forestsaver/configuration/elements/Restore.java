package de.eldoria.forestsaver.configuration.elements;

import de.eldoria.forestsaver.service.restoration.RestoreOrder;

public class Restore {
    /**
     * The order in which the blocks are restored.
     */
    RestoreOrder restoreOrder = RestoreOrder.HEIGHT;

    /**
     * The number of ticks between restoration of a block in a node.
     */
    int ticksPerBlock = 5;

    /**
     * The number of ticks in which a node is checked for restoration.
     */
    int checkForRestore = 20 * 60; // 1 minute

    /**
     * The number of seconds in which a node is considered idle when getting checked for restoration.
     */
    int nodeIdleTime = 60 * 10; // 10 minutes

    /**
     * The number of seconds in which a node is deleted after it has been idle for a certain time.
     */
    int nodeDeletionTime = 60 * 60 * 12; // 12 hours

    /**
     * Checked whether a player is within this distance to a node.
     * If a player is within this distance, the block will not be restored.
     */
    int minPlayerDistance = 10;

    /**
     * The order in which the blocks are restored.
     * @return RestoreOrder
     */
    public RestoreOrder restoreOrder() {
        return restoreOrder;
    }

    /**
     * The number of ticks between restoration of a block in a node.
     * @return ticksPerBlock
     */
    public int ticksPerBlock() {
        return ticksPerBlock;
    }

    /**
     * The number of seconds in which a node is considered idle when getting checked for restoration.
     * @return nodeIdleTime
     */
    public int nodeIdleTime() {
        return nodeIdleTime;
    }

    /**
     * Checked whether a player is within this distance to a node.
     * If a player is within this distance, the block will not be restored.
     * @return minPlayerDistance
     */
    public int minPlayerDistance() {
        return minPlayerDistance;
    }

    /**
     * The number of ticks in which a node is checked for restoration.
     * @return checkForRestore
     */
    public int checkForRestore() {
        return checkForRestore;
    }

    /**
     * The number of seconds in which a node is deleted after it has been idle for a certain time.
     * @return nodeDeletionTime
     */
    public int nodeDeletionTime() {
        return nodeDeletionTime;
    }
}
