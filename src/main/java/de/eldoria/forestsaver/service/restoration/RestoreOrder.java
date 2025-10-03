package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.data.dao.Fragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Comparator.comparing;

public enum RestoreOrder {
    /**
     * Restores blocks in order of their destruction time.
     */
    TIME(e -> e.sort(comparing(fragment -> Objects.requireNonNullElse(fragment.destroyed(), Instant.now())))),

    /**
     * Restores blocks in order of their height.
     */
    HEIGHT(e -> e.sort(comparing(x -> x.position().getBlockY()))),

    /**
     * Restores blocks in a random order.
     */
    RANDOM(Collections::shuffle);

    private final Consumer<List<Fragment>> sorter;

    RestoreOrder(Consumer<List<Fragment>> sorter) {
        this.sorter = sorter;
    }

    /**
     * Sorts the given collection according to this restore order.
     * @param collection collection to sort
     */
    public void sort(List<Fragment> collection) {
        sorter.accept(collection);
    }
}
