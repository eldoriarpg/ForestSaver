package de.eldoria.forestsaver.worldguard;

import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.StringFlag;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Presets;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import dev.chojo.ocular.Configurations;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ResourceFlag extends StringFlag {
    private final ResourceType type;
    private final Configurations<Configuration> configuration;

    public ResourceFlag(String name, ResourceType type, Configurations<Configuration> configuration) {
        super(name);
        this.type = type;
        this.configuration = configuration;
    }

    @Override
    public String parseInput(FlagContext context) throws InvalidFlagFormat {
        String userInput = context.getUserInput();
        if (configuration.secondary(Presets.KEY).getPreset(userInput, type).isEmpty()) {
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

    public ResourceType type() {
        return type;
    }
}
