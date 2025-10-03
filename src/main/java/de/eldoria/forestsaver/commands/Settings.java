package de.eldoria.forestsaver.commands;

import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Preset;
import dev.chojo.ocular.Configurations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;

import java.util.List;

public class Settings {
    private final Configurations<Configuration> configurations;

    public Settings(Configurations<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Command("resourcesaver|rs settings <world> defaultpreset <preset>")
    public void setDefaultPreset(CommandSourceStack stack, @Argument("world") World world, @Argument("preset") Preset preset) {
        configurations.main().worlds().setDefaultPreset(world, preset);
        configurations.save();
    }

    @Command("resourcesaver|rs settings <world> resetpreset")
    public void removeDefaultPreset(CommandSourceStack stack, @Argument("world") World world) {
        configurations.main().worlds().setDefaultPreset(world, null);
        configurations.save();
    }
}
