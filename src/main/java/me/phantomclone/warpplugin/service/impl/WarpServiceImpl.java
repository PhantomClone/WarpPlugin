package me.phantomclone.warpplugin.service.impl;

import lombok.RequiredArgsConstructor;
import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.injection.Bean;
import me.phantomclone.warpplugin.repository.WarpRepository;
import me.phantomclone.warpplugin.service.WarpService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Bean
@RequiredArgsConstructor
public class WarpServiceImpl implements WarpService {

    private final WarpRepository warpRepository;
    private final WarpCache warpCache;

    @Override
    public CompletableFuture<Boolean> createWarp(Warp warp) {
        return warpRepository.createWarp(warp)
                .thenApply(result -> createWarpApply(result, warp));
    }

    @Override
    public CompletableFuture<List<Warp>> getWarpsOf(UUID playerUuid) {
        return warpRepository.getWarpsOf(playerUuid)
                .thenApply(this::createWarpApply);
    }

    @Override
    public CompletableFuture<Optional<Warp>> getWarp(UUID playerUuid, String warpName) {
        return warpRepository.getWarp(playerUuid, warpName);
    }

    @Override
    public CompletableFuture<Boolean> deleteWarp(UUID playerUuid, String warpName) {
        return warpRepository.deleteWarp(playerUuid, warpName)
                .thenApply(result -> deleteWarpApply(result, playerUuid, warpName));
    }

    private boolean createWarpApply(boolean result, Warp warp) {
        if (result) {
            warpCache.cache(warp.getPlayerUuid(), warp.getWarpName());
        }

        return result;
    }

    private List<Warp> createWarpApply(List<Warp> result) {
        if (!result.isEmpty()) {
            warpCache.cache(result.get(0).getPlayerUuid(), result.stream()
                    .map(Warp::getWarpName).toList());
        }

        return result;
    }

    private boolean deleteWarpApply(boolean result, UUID playerUuid, String warpName) {
        if (result) {
            warpCache.remove(playerUuid, warpName);
        }

        return result;
    }
}
