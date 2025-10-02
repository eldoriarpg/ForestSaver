package de.eldoria.forestsaver.configuration.parsing.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import de.eldoria.forestsaver.configuration.parsing.TagDeserializer;
import de.eldoria.forestsaver.configuration.parsing.TagSerializer;
import org.bukkit.Material;
import org.bukkit.Tag;

public class InternalModule extends Module {

    @Override
    public String getModuleName() {
        return "InternalModule";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, null, null, null);
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(Tag.class, new TagDeserializer());

        JavaType type = setupContext.getTypeFactory().constructParametricType(Tag.class, Material.class);
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new TagSerializer(type));

        setupContext.addDeserializers(deserializers);
        setupContext.addSerializers(serializers);
    }
}
