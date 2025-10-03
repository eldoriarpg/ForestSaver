package de.eldoria.forestsaver.service.restoration;

import de.eldoria.forestsaver.data.dao.Fragment;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Comparator.comparing;

public enum RestoreOrder {
    TIME(e -> e.sort(comparing(fragment -> Objects.requireNonNullElse(fragment.destroyed(), Instant.now())))),
    HEIGHT(e -> e.sort(comparing(x -> x.position().getBlockY()))),
    RANDOM(Collections::shuffle);

    private final Consumer<List<Fragment>> sorter;

    RestoreOrder(Consumer<List<Fragment>> sorter) {
        this.sorter = sorter;
    }

    public void sort(List<Fragment> collection) {
        sorter.accept(collection);
    }
}
