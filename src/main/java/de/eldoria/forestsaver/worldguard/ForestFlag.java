package de.eldoria.forestsaver.worldguard;

import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StringFlag;
import de.eldoria.forestsaver.configuration.Configuration;
import dev.chojo.ocular.Configurations;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ForestFlag extends StringFlag {
    private final Configurations<Configuration> configuration;

    public ForestFlag(Configurations<Configuration> configuration) {
        super("forest-type");
        this.configuration = configuration;
    }

    @Override
    public String parseInput(FlagContext context) throws InvalidFlagFormat {
        String userInput = context.getUserInput();
        if (configuration.main().presets().getPreset(userInput).isEmpty()) {
            throw new InvalidFlagFormat("Preset not found");
        }
        return super.parseInput(context);
    }

    @Override
    public boolean hasConflictStrategy() {
        return true;
    }

    @Override
    public @Nullable String chooseValue(Collection<String> values) {
        return values.stream().findFirst().orElse(null);
    }
}
