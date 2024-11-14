package dev.david.tntrun.game.tasks;

import dev.david.tntrun.game.TNTRun;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public class PlayingTask extends BukkitRunnable {

    private final Plugin plugin;
    private final TNTRun game;
    private int seconds;

    private MiniMessage mm = MiniMessage.miniMessage();

    public PlayingTask(Plugin plugin, TNTRun game) {
        this.plugin = plugin;
        this.game = game;
        this.seconds = 0;
    }

    public void start() {
        runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        seconds++;
    }

    @Override
    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.getTaskId());
    }
}
