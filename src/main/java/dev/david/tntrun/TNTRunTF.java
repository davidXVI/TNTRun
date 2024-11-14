package dev.david.tntrun;

import dev.david.tntrun.game.listeners.MovementListener;
import dev.david.tntrun.messaging.GameMessagingService;
import lombok.Getter;
import net.tfgames.common.api.game.settings.GameType;
import net.tfgames.engine.EngineTF;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TNTRunTF extends JavaPlugin {

    @Getter
    private static TNTRunTF instance;
    private String serverId;

    @Override
    public void onEnable() {
        instance = this;

        GameMessagingService.init();
        EngineTF.registerGame(GameType.TNT_RUN);

        registerListeners(
                new MovementListener()
        );
    }

    @Override
    public void onDisable() {
        EngineTF.unRegisterGame(GameType.TNT_RUN);
    }

    public void registerListeners(Listener... listeners) {
        for (Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, this);
        }
    }

    public String getServerId() {
        return EngineTF.getServerId();
    }
}
