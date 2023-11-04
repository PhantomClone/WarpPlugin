package me.phantomclone.warpplugin.cache.impl;

import me.phantomclone.warpplugin.cache.WarpCache;
import me.phantomclone.warpplugin.injection.Bean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Bean
public class WarpCacheImpl implements WarpCache {

    private final Map<UUID, List<String>> cache = new ConcurrentHashMap<>();

    @Override
    public void cache(UUID uuid, List<String> warpNameList) {
        cache.put(uuid, warpNameList);
    }

    @Override
    public void cache(UUID uuid, String warpName) {
        List<String> list = cache.getOrDefault(uuid, List.of());

        ArrayList<String> warpNameList = new ArrayList<>(list);
        warpNameList.add(warpName);

        cache.put(uuid, Collections.unmodifiableList(warpNameList));
    }

    @Override
    public void remove(UUID uuid, String warpName) {
        List<String> list = cache.getOrDefault(uuid, List.of());

        ArrayList<String> warpNameList = new ArrayList<>(list);
        warpNameList.remove(warpName);

        cache.put(uuid, Collections.unmodifiableList(warpNameList));
    }

    @Override
    public List<String> getWarpNameCache(UUID uuid) {
        return cache.getOrDefault(uuid, List.of());
    }

    @Override
    public void clear(UUID uuid) {
        cache.remove(uuid);
    }
}
