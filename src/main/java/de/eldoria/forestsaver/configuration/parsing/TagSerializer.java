package de.eldoria.forestsaver.configuration.parsing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.io.IOException;

public class TagSerializer extends StdSerializer<Tag<Material>> {
    public TagSerializer(JavaType type) {
        super(type);
    }

    @Override
    public void serialize(Tag<Material> materialTag, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        var key = materialTag.getKey();
        jsonGenerator.writeObject(key);
    }
}
