package me.phantomclone.warpplugin.cache;

import java.util.List;
import java.util.UUID;

public interface WarpCache {

    void cache(UUID uuid, List<String> warpNameList);
    void cache(UUID uuid, String warpName);
    void remove(UUID uuid, String warpName);

    List<String> getWarpNameCache(UUID uuid);

    void clear(UUID uuid);

}
