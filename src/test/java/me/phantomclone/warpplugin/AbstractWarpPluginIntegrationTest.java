package me.phantomclone.warpplugin;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

abstract class AbstractWarpPluginIntegrationTest extends WarpPlugin {

    @Override
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

        config.setUsername("sa");
        config.setPassword("");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(15000);

        return new HikariDataSource(config);
    }
}