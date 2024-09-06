package me.phantomclone.warpplugin.cache.impl;

import me.phantomclone.warpplugin.cache.WarpCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WarpCacheImplTest {

    private WarpCache warpCache;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        warpCache = new WarpCacheImpl();
        playerUUID = UUID.randomUUID();
    }

    @Test
    void testCacheWithList_ShouldStoreWarpNames() {
        List<String> warpNames = List.of("warp1", "warp2", "warp3");

        warpCache.cache(playerUUID, warpNames);

        assertEquals(warpNames, warpCache.getWarpNameCache(playerUUID));
    }

    @Test
    void testCacheWithString_ShouldAddWarpNameToList() {
        String warpName1 = "warp1";
        String warpName2 = "warp2";

        warpCache.cache(playerUUID, warpName1);

        warpCache.cache(playerUUID, warpName2);

        List<String> expectedWarpNames = List.of(warpName1, warpName2);
        assertEquals(expectedWarpNames, warpCache.getWarpNameCache(playerUUID));
    }

    @Test
    void testRemove_ShouldRemoveWarpNameFromList() {
        List<String> warpNames = List.of("warp1", "warp2", "warp3");
        warpCache.cache(playerUUID, warpNames);

        warpCache.remove(playerUUID, "warp2");

        List<String> expectedWarpNames = List.of("warp1", "warp3");
        assertEquals(expectedWarpNames, warpCache.getWarpNameCache(playerUUID));
    }

    @Test
    void testGetWarpNameCache_ShouldReturnEmptyListIfNotCached() {
        List<String> warpNames = warpCache.getWarpNameCache(playerUUID);

        assertTrue(warpNames.isEmpty());
    }

    @Test
    void testClear_ShouldRemoveAllWarpNamesForUUID() {
        List<String> warpNames = List.of("warp1", "warp2");
        warpCache.cache(playerUUID, warpNames);

        warpCache.clear(playerUUID);

        assertTrue(warpCache.getWarpNameCache(playerUUID).isEmpty());
    }

    @Test
    void testCacheWithEmptyList_ShouldStoreEmptyList() {
        warpCache.cache(playerUUID, List.of());

        assertTrue(warpCache.getWarpNameCache(playerUUID).isEmpty());
    }

    @Test
    void testRemoveFromEmptyCache_ShouldHandleGracefully() {
        warpCache.remove(playerUUID, "nonexistentWarp");

        assertTrue(warpCache.getWarpNameCache(playerUUID).isEmpty());
    }
}
