package me.phantomclone.warpplugin.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class Warp {

    private UUID playerUuid;
    private String warpName;
    private Location location;

}
