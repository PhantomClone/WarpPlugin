package me.phantomclone.warpplugin.service.impl;

import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.domain.Warp;
import me.phantomclone.warpplugin.repository.WarpRepository;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarpServiceImplTest {

    @Mock
    WarpRepository warpRepository;

    @Mock
    WarpCache warpCache;

    @InjectMocks
    WarpServiceImpl warpService;

    @Test
    void testCreateWarp() throws ExecutionException, InterruptedException {
        Warp warp = createWarp();
        when(warpRepository.createWarp(warp))
                .thenReturn(CompletableFuture.completedFuture(true));

        CompletableFuture<Boolean> result = warpService.createWarp(warp);

        assertTrue(result.get());
        verify(warpCache)
                .cache(warp.getPlayerUuid(), warp.getWarpName());
    }

    @Test
    void testGetWarpsOf() throws ExecutionException, InterruptedException {
        Warp warp = createWarp();
        UUID playerUuid = warp.getPlayerUuid();
        Warp secondWarp = createWarp(playerUuid, "Second", mock(Location.class));
        List<Warp> warps = List.of(warp, secondWarp);
        when(warpRepository.getWarpsOf(warp.getPlayerUuid()))
                .thenReturn(CompletableFuture.completedFuture(warps));

        CompletableFuture<List<Warp>> result = warpService.getWarpsOf(playerUuid);

        assertEquals(2, result.get().size());
        verify(warpCache).cache(eq(playerUuid), anyList());
    }

    @Test
    void testGetWarp() throws ExecutionException, InterruptedException {
        Warp warp = createWarp();
        when(warpRepository.getWarp(warp.getPlayerUuid(), warp.getWarpName()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(warp)));

        CompletableFuture<Optional<Warp>> result = warpService.getWarp(warp.getPlayerUuid(), warp.getWarpName());

        assertTrue(result.get().isPresent());
        assertEquals(warp, result.get().get());
    }

    @Test
    void testDeleteWarp() throws ExecutionException, InterruptedException {
        Warp warp = createWarp();
        when(warpRepository.deleteWarp(warp.getPlayerUuid(), warp.getWarpName()))
                .thenReturn(CompletableFuture.completedFuture(true));

        CompletableFuture<Boolean> result = warpService.deleteWarp(warp.getPlayerUuid(), warp.getWarpName());

        assertTrue(result.get());
        verify(warpCache).remove(warp.getPlayerUuid(), warp.getWarpName());
    }

    Warp createWarp() {
        return createWarp(UUID.randomUUID(), UUID.randomUUID().toString(), mock(Location.class));
    }

    Warp createWarp(UUID uuid, String warpName, Location location) {
        return new Warp().setPlayerUuid(uuid)
                .setWarpName(warpName)
                .setLocation(location);
    }
}