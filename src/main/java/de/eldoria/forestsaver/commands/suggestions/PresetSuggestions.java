package de.eldoria.forestsaver.commands.suggestions;

import de.eldoria.eldoutilities.commands.Completion;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Preset;
import de.eldoria.forestsaver.configuration.elements.Presets;
import de.eldoria.forestsaver.configuration.elements.ResourceType;
import dev.chojo.ocular.Configurations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.List;

public record PresetSuggestions(Configurations<Configuration> configurations) {

    @Suggestions("presets")
    public List<String> suggest(CommandContext<CommandSourceStack> ctx, CommandSourceStack stack, CommandInput input) {
        return Completion.complete(input.readString(), configurations.secondary(Presets.KEY).names(ctx.get("type")));
    }

    @Parser(suggestions = "presets")
    public Preset parser(CommandContext<CommandSourceStack> ctx, CommandInput input) {
        return configurations.secondary(Presets.KEY).getPreset(input.readString(), ctx.get("type"))
                             .orElseThrow(() -> new RuntimeException("Preset not found"));
    }
}
