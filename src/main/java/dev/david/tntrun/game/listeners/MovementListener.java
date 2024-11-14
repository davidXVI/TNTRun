package dev.david.tntrun.game.listeners;

import dev.david.tntrun.game.TNTRun;
import net.tfgames.engine.api.PlayerKillEvent;
import net.tfgames.engine.arena.Arena;
import net.tfgames.engine.arena.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasChangedPosition()) return;

        Player player = e.getPlayer();
        Arena arena = ArenaManager.getArena(player);

        if (arena == null) return;
        if (arena.getGame() instanceof TNTRun game) {
            int playerY = e.getPlayer().getLocation().getBlockY();
            if (playerY < game.getConfig().getYLimit()) {
                PlayerKillEvent killEvent = new PlayerKillEvent(arena, null, player, PlayerKillEvent.KillCause.VOID);
                Bukkit.getPluginManager().callEvent(killEvent);

                arena.addSpectator(player, null, true);
                game.setTimeOfDeath(player);
                player.playSound(player, Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);

                // Handle teleportation and winner check
                player.teleportAsync(game.getConfig().getWaitingSpawn()).thenRun(game::checkWinner);
                arena.resetPlayer(player);
            }
        }
    }

}
