package me.phantomclone.warpplugin.flyway;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.injection.PostConstructor;
import org.flywaydb.core.Flyway;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

@Bean
@RequiredArgsConstructor
public class FlywayHandler {

    private static final String MIGRATION_FOLDER = "filesystem:%s/db/migration";

    private final WarpPlugin plugin;
    private final HikariDataSource dataSource;

    @PostConstructor
    @SneakyThrows
    public void migrate() {
        // Flyway can not find sql files in resource because of spigot plugin loader.
        copySQLFileToPluginFolder();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(getFlywayLocation())
                .load();

        flyway.migrate();
    }

    private void copySQLFileToPluginFolder() throws URISyntaxException, IOException {
        URI uri = getUriOfSQLResourceFolder();
        FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
        try (Stream<Path> walk = Files.walk(fileSystem.getPath("db/migration/"))) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .forEach(this::copyUrl);
        }
        fileSystem.close();
    }

    private URI getUriOfSQLResourceFolder() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("db/migration/");

        if (resource == null) {
            throw new RuntimeException("db/migration resource uri could not be found.");
        }

        return resource.toURI();
    }

    @SneakyThrows
    private void copyUrl(Path path) {
        File outputFile = new File(plugin.getDataFolder(), path.toString());

        if (!outputFile.exists()) {
            createFolderIfNotExist(outputFile);
            copySqlFile(path, outputFile);
        }
    }

    private static void copySqlFile(Path path, File outputFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFile);
             InputStream inputStream = path.toUri().toURL().openConnection().getInputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        }
    }

    private static void createFolderIfNotExist(File outputFile) {
        boolean ignore = outputFile.getParentFile().mkdirs();
    }

    private String getFlywayLocation() {
        return String.format(MIGRATION_FOLDER, plugin.getDataFolder().toString().replace('\\', '/'));
    }

}
