package dev.david.tntrun.config;

import dev.david.tntrun.TNTRunTF;
import net.tfgames.engine.config.GameConfig;

import java.io.IOException;

public class TNTRunConfig extends GameConfig {

    public TNTRunConfig(String mapName, String world) {
        super(TNTRunTF.getInstance(), mapName, world);
    }

    // Y Limit
    public void setYLimit(int y) {
        config.set(mapName + ".y-limit", y);

        try {config.save(arenaFile);}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    public int getYLimit() {
        return config.getInt(mapName + "y-limit");
    }

}
