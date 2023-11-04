package me.phantomclone.warpplugin.repository;

import me.phantomclone.warpplugin.domain.Warp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarpRepository {

    CompletableFuture<Boolean> createWarp(Warp warp);
    CompletableFuture<List<Warp>> getWarpsOf(UUID playerUuid);
    CompletableFuture<Optional<Warp>> getWarp(UUID playerUuid, String warpName);
    CompletableFuture<Boolean> deleteWarp(UUID playerUuid, String warpName);

}
