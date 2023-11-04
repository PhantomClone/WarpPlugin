package me.phantomclone.warpplugin.repository.impl;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.QueryBuilderConfig;
import de.chojo.sadu.wrapper.util.ParamBuilder;
import de.chojo.sadu.wrapper.util.Row;
import de.chojo.sadu.wrapper.util.UpdateResult;
import me.phantomclone.warpplugin.WarpPlugin;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.repository.WarpRepository;
import org.bukkit.Location;
import org.bukkit.World;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Bean
public class WarpRepositoryImpl extends QueryFactory implements WarpRepository {

    private static final String CREATE_WARP_SQL = "INSERT INTO warp(playeruuid, warpname, worldname, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_WARP_OF_UUID = "SELECT warpname, worldname, x, y, z, pitch, yaw FROM warp WHERE playeruuid = ?";
    private static final String SELECT_WARP_OF_UUID_AND_WARP_NAME = "SELECT warpname, worldname, x, y, z, yaw, pitch FROM warp WHERE playeruuid = ? AND warpname = ?";
    private static final String DELETE_WARP = "DELETE FROM warp WHERE playeruuid = ? AND warpname = ?";

    private final WarpPlugin plugin;

    public WarpRepositoryImpl(DataSource dataSource, WarpPlugin plugin) {
        super(dataSource, QueryBuilderConfig.builder()
                .withExceptionHandler(WarpRepositoryImpl::handleSQLException)
                .build());
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Boolean> createWarp(Warp warp) {
        return builder().query(CREATE_WARP_SQL)
                .parameter(paramBuilder -> parameterForCreateWarp(paramBuilder, warp))
                .insert()
                .send()
                .thenApply(UpdateResult::changed);
    }

    @Override
    public CompletableFuture<List<Warp>> getWarpsOf(UUID playerUuid) {
        return builder(Warp.class)
                .query(SELECT_WARP_OF_UUID)
                .parameter(paramBuilder -> paramBuilder.setObject(playerUuid))
                .readRow(row -> readWarpOfRow(row, playerUuid))
                .all();
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(UUID playerUuid, String warpName) {
        return builder(Warp.class)
                .query(SELECT_WARP_OF_UUID_AND_WARP_NAME)
                .parameter(paramBuilder -> parameterForGetWarp(playerUuid, warpName, paramBuilder))
                .readRow(row -> readWarpOfRow(row, playerUuid))
                .first();
    }

    @Override
    public CompletableFuture<Boolean> deleteWarp(UUID playerUuid, String warpName) {
        return builder()
                .query(DELETE_WARP)
                .parameter(paramBuilder -> parameterForGetWarp(playerUuid, warpName, paramBuilder))
                .delete()
                .send()
                .thenApply(UpdateResult::changed);
    }

    private static void parameterForCreateWarp(ParamBuilder paramBuilder, Warp warp) throws SQLException {
        Location location = warp.getLocation();
        paramBuilder.setObject(warp.getPlayerUuid())
                .setString(warp.getWarpName())
                .setString(location.getWorld().getName())
                .setDouble(location.getX())
                .setDouble(location.getY())
                .setDouble(location.getZ())
                .setBigDecimal(BigDecimal.valueOf(location.getYaw()))
                .setBigDecimal(BigDecimal.valueOf(location.getPitch()));
    }

    private Warp readWarpOfRow(Row row, UUID playerUuid) throws SQLException {
        return new Warp()
                .setPlayerUuid(playerUuid)
                .setWarpName(row.getString("warpname"))
                .setLocation(readLocationOfRow(row));
    }

    private Location readLocationOfRow(Row row) throws SQLException {
        World world = plugin.getServer().getWorld(row.getString("worldname"));
        double x = row.getDouble("x");
        double y = row.getDouble("y");
        double z = row.getDouble("z");
        float yaw = row.getBigDecimal("yaw").floatValue();
        float pitch = row.getBigDecimal("pitch").floatValue();

        return new Location(world, x, y, z, yaw, pitch);
    }

    private static void parameterForGetWarp(UUID playerUuid, String warpName, ParamBuilder paramBuilder) throws SQLException {
        paramBuilder.setObject(playerUuid).setString(warpName);
    }

    private static void handleSQLException(SQLException sqlException) {
       if (sqlException.getMessage().compareTo("ERROR: duplicate key value violates unique constraint \"warp_pkey\"") == 0) {
            sqlException.printStackTrace();
        }
    }

}
