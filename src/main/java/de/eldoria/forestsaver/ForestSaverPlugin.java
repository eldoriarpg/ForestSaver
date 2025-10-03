package de.eldoria.forestsaver;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.chojo.sadu.datasource.DataSourceCreator;
import de.chojo.sadu.mapper.RowMapperRegistry;
import de.chojo.sadu.mariadb.databases.MariaDb;
import de.chojo.sadu.mysql.databases.MySql;
import de.chojo.sadu.postgresql.databases.PostgreSql;
import de.chojo.sadu.queries.api.configuration.QueryConfiguration;
import de.chojo.sadu.sqlite.databases.SqLite;
import de.chojo.sadu.updater.QueryReplacement;
import de.chojo.sadu.updater.SqlUpdater;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import de.eldoria.forestsaver.commands.Node;
import de.eldoria.forestsaver.commands.Settings;
import de.eldoria.forestsaver.commands.suggestions.Presets;
import de.eldoria.forestsaver.configuration.Configuration;
import de.eldoria.forestsaver.configuration.elements.Database;
import de.eldoria.forestsaver.configuration.parsing.module.InternalModule;
import de.eldoria.forestsaver.data.Nodes;
import de.eldoria.forestsaver.data.Worlds;
import de.eldoria.forestsaver.data.dao.Fragment;
import de.eldoria.forestsaver.service.modification.ModificationService;
import de.eldoria.forestsaver.service.restoration.RestoreService;
import de.eldoria.forestsaver.worldguard.ForestFlag;
import de.eldoria.jacksonbukkit.JacksonPaper;
import de.eldoria.jacksonbukkit.serializer.NamespacedKeySerializer;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Level;

public class ForestSaverPlugin extends EldoPlugin {
    private ForestFlag forestFlag;
    private Configurations<Configuration> conf;

    @Override
    public Level getLogLevel() {
        return Level.INFO;
    }

    @Override
    public void onPluginLoad() throws Throwable {
        Key<Configuration> mainConfig = Key.builder(Path.of("config.yaml"), Configuration::new).build();
        conf = Configurations.builder(mainConfig,
                                     new YamlDataFormat())
                             .setBase(getDataPath().resolve("configurations"))
                             .addModule(JacksonPaper.builder()
                                                    .withMiniMessages()
                                                    .withNamespacedKeyFormat(NamespacedKeySerializer.Format.SHORT)
                                                    .build())
                             .addModule(new InternalModule())
                             .build();
        conf.main();
        conf.save();
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        forestFlag = new ForestFlag(conf);
        flagRegistry.register(forestFlag);

    }

    @Override
    public void onPluginEnable() throws Throwable {
        var commandManager = PaperCommandManager.builder()
                                                .executionCoordinator(ExecutionCoordinator.<CommandSourceStack>builder().build())
                                                .buildOnEnable(this);

        Nodes nodes = new Nodes(conf);
        Worlds worlds = new Worlds(nodes);
        RestoreService restoreService = new RestoreService(this, nodes, conf);
        ModificationService modificationService = new ModificationService(this, worlds, WorldGuard.getInstance(), forestFlag, restoreService, conf);
        registerListener(modificationService);

        // Parser without a CommandMeta mapper.
        var annotationParser = new AnnotationParser<>(commandManager, CommandSourceStack.class);
        Node node = new Node(nodes, restoreService, modificationService);
        annotationParser.parse(node);
        annotationParser.parse(new Presets(conf));
        annotationParser.parse(new Settings(conf));
        setupDb();
    }

    @Override
    public void onPostStart() throws Throwable {
        conf.main().bootstrap(this, conf);
        conf.save();
    }

    private void setupDb() {
        Database database = conf.main().database();
        DataSource dataSource = switch (database.type()) {
            case SQLITE -> DataSourceCreator.create(SqLite.get())
                                            .configure(c -> c.path(getDataPath().resolve(database.path())))
                                            .create()
                                            .build();
            case POSTGRESQL -> DataSourceCreator.create(PostgreSql.get())
                                                .configure(c -> c.host(database.host())
                                                                 .port(database.port())
                                                                 .database(database.database())
                                                                 .user(database.username())
                                                                 .password(database.password())
                                                                 .currentSchema(database.schema())
                                                                 .applicationName("ForestSaver"))
                                                .create()
                                                .withMaximumPoolSize(database.poolSize())
                                                .build();
            case MARIADB -> DataSourceCreator.create(MariaDb.get())
                                             .configure(c -> c.host(database.host())
                                                              .port(database.port())
                                                              .database(database.database())
                                                              .user(database.username())
                                                              .password(database.password()))
                                             .create()
                                             .withMaximumPoolSize(database.poolSize())
                                             .build();
            case MYSQL -> DataSourceCreator.create(MySql.get())
                                           .configure(c -> c.host(database.host())
                                                            .port(database.port())
                                                            .database(database.database())
                                                            .user(database.username())
                                                            .password(database.password()))
                                           .create()
                                           .withMaximumPoolSize(database.poolSize())
                                           .build();
        };

        try {
            updateDb(dataSource, database);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RowMapperRegistry rowMapperRegistry = new RowMapperRegistry();
        rowMapperRegistry.register(de.eldoria.forestsaver.data.dao.Node.class);
        rowMapperRegistry.register(Fragment.class);
        QueryConfiguration.setDefault(QueryConfiguration.builder(dataSource)
                                                        .setRowMapperRegistry(rowMapperRegistry)
                                                        .build());
    }

    private void updateDb(DataSource dataSource, Database database) throws SQLException, IOException {
        switch (database.type()) {
            case SQLITE -> {
                SqlUpdater.builder(dataSource, SqLite.get())
                          .withClassLoader(getClassLoader())
                          .execute();
            }
            case POSTGRESQL -> {
                SqlUpdater.builder(dataSource, PostgreSql.get())
                          .withClassLoader(getClassLoader())
                          .setSchemas(database.schema())
                          .setReplacements(new QueryReplacement("forest_schema", database.schema()))
                          .execute();
            }
            case MARIADB -> {
                SqlUpdater.builder(dataSource, MariaDb.get())
                          .withClassLoader(getClassLoader())
                          .setReplacements(new QueryReplacement("prefix", database.tablePrefix()))
                          .setVersionTable(database.tablePrefix() + "_version")
                          .execute();
            }
            case MYSQL -> {
                SqlUpdater.builder(dataSource, MySql.get())
                          .withClassLoader(getClassLoader())
                          .setReplacements(new QueryReplacement("prefix", database.tablePrefix()))
                          .setVersionTable(database.tablePrefix() + "_version")
                          .execute();
            }
        }

    }
}
