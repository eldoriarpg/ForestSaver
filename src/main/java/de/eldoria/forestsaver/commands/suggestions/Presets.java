package de.eldoria.forestsaver.commands.suggestions;

import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Preset;
import dev.chojo.ocular.Configurations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandInput;

import java.util.List;

public class Presets {
    private final Configurations<Configuration> configurations;

    public Presets(Configurations<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Suggestions("presets")
    public List<String> suggest(CommandSourceStack stack, String input) {
        return Completion.complete(input, configurations.main().presets().names());
    }

    @Parser(suggestions = "presets")
    public Preset parser(CommandInput value) {
        return configurations.main().presets().getPreset(value.readString()).orElseThrow(() -> new RuntimeException("Preset not found"));
    }
}
