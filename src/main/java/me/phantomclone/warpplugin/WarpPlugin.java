package me.phantomclone.warpplugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.phantomclone.warpplugin.injection.Autowire;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.injection.PhoenixDependencyInjector;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpPlugin extends JavaPlugin {

    private static final String DATASOURCE_PROPERTY = "datasource.properties";
    private static final String PHOENIX_DEPENDENCY_INJECTOR_CONFIG = "IDConfig.yml";

    private PhoenixDependencyInjector phoenixDependencyInjector;

    @Autowire
    private HikariDataSource dataSource;

    @Override
    public void onEnable() {
        saveResource(DATASOURCE_PROPERTY, false);
        saveResource(PHOENIX_DEPENDENCY_INJECTOR_CONFIG, false);

        phoenixDependencyInjector = new PhoenixDependencyInjector(PHOENIX_DEPENDENCY_INJECTOR_CONFIG);

        phoenixDependencyInjector.load(getDataFolder(), this);
    }

    @Override
    public void onDisable() {
        dataSource.close();

        phoenixDependencyInjector.clean();
    }

    @Bean
    public HikariDataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig(String.format("%s/%s", getDataFolder(), DATASOURCE_PROPERTY));

        return new HikariDataSource(hikariConfig);
    }

}
