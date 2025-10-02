package de.eldoria.forestsaver.configuration.elements;

import de.eldoria.forestsaver.service.restoration.RestoreOrder;

public class Restore {
    RestoreOrder restoreOrder = RestoreOrder.HEIGHT;
    int ticksPerBlock = 5;
    int checkForRestore = 20 * 60;
    int nodeIdleTime = 20 * 60 * 10;
    int minPlayerDistance = 10;

    public RestoreOrder restoreOrder() {
        return restoreOrder;
    }

    public int ticksPerBlock() {
        return ticksPerBlock;
    }

    public int checks() {
        return checkForRestore;
    }

    public int restoreAfterTicks() {
        return nodeIdleTime;
    }

    public int minPlayerDistance() {
        return minPlayerDistance;
    }
}
