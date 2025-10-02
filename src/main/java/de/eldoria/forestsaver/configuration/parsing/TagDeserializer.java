package de.eldoria.forestsaver.configuration.parsing;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

import java.io.IOException;

public class TagDeserializer extends JsonDeserializer<Tag<Material>> {

    @Override
    public Tag<Material> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return Bukkit.getTag(Tag.REGISTRY_BLOCKS, jsonParser.readValueAs(NamespacedKey.class), Material.class);
    }
}
