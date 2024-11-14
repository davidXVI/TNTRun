package dev.david.tntrun.game;

import dev.david.tntrun.TNTRunTF;
import dev.david.tntrun.config.TNTRunConfig;
import dev.david.tntrun.game.components.TNTSidebar;
import dev.david.tntrun.game.tasks.PlayingTask;
import dev.david.tntrun.utils.PlayerPosition;
import lombok.Getter;
import net.kyori.adventure.title.Title;
import net.tfgames.common.api.game.PackedGame;
import net.tfgames.engine.arena.ArenaState;
import net.tfgames.engine.game.Game;
import net.tfgames.engine.game.modules.GameChat;
import net.tfgames.engine.team.ArenaTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class TNTRun extends Game {

    private TNTRun gameInstance;
    private TNTRunConfig config;

    private PlayingTask playingTask;
    private BukkitRunnable blockBreakingTask;

    private Map<UUID, Long> timeOfDeath;
    private HashSet<Block> blocksToDestroy;

    public TNTRun(Plugin plugin, PackedGame packedGame) {
        super(plugin, packedGame);
        this.gameInstance = this;

        this.timeOfDeath = new HashMap<>();
        this.blocksToDestroy = new HashSet<>();

        this.playingTask = new PlayingTask(plugin, this);
        this.blockBreakingTask = new BukkitRunnable() {
            @Override
            public void run() {
                handleMovements();
            }
        };
    }

    @Override
    public void init(String s, String s1) {
        this.config = new TNTRunConfig(s, s1);
    }

    @Override
    public void start() {
        playingTask.start();
        blockBreakingTask.runTaskTimer(plugin, 20 * 5, 1L);

        setupComponents();
        teleportPlayers();
    }

    @Override
    public void setupComponents() {
        this.scoreboard = new TNTSidebar(this);
        this.gameChat = new GameChat();

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                addComponents(player, null);
            }
        }
    }

    @Override
    public Game clone() {
        return new TNTRun(plugin, packedGame);
    }

    //Game Management
    public void checkWinner() {
        // Arena is Empty, end the game with a winner
        if (arena.getPlayers().isEmpty()) {
            endGame(null);
            return;
        }

        if (arena.getPlayers().size() == 1) {
            Player winner = Bukkit.getPlayer(arena.getPlayers().getFirst());
            endGame(winner);
        }
    }

    public void endGame(Player winner) {
        if (winner != null) {
            setTimeOfDeath(winner);
            endMessage(winner);
            arena.changeState(ArenaState.RESTARTING);
        }
        else {
            arena.changeState(ArenaState.RESTARTING);
        }
    }

    public void endMessage(Player winner) {
        arena.sendMessage("<gold><bold><st>                                                                 ");
        arena.sendCenteredMessage("<yellow><bold>ᴛɴᴛ ʀᴜɴ");
        arena.sendMessage(" ");
        arena.sendCenteredMessage("<gold>Vencedor - " + winner.getName());
        arena.sendMessage(" ");
        sendRankMessages(3);
        arena.sendMessage(" ");
        arena.sendMessage("<gold><bold><st>                                                                 ");

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            if(player != null) {
                if (player.equals(winner)) {
                    player.showTitle(Title.title(mm.deserialize("<green><bold>VITÓRIA!"), mm.deserialize("Você venceu a Partida!")));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0F , 1.0F);
                }
                else {
                    player.showTitle(Title.title(mm.deserialize("<red><bold>DERROTA!"), mm.deserialize("Não foi dessa vez :(")));
                    player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1.0F , 1.0F);
                }
            }
        }
    }

    // Ranking
    public Optional<Player> getPlayerByRank(int rank) {
        List<UUID> rankedPlayers = timeOfDeath.entrySet().stream()
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        if (rank > 0 && rank <= rankedPlayers.size()) {
            UUID playerUUID = rankedPlayers.get(rank - 1);
            Player player = Bukkit.getPlayer(playerUUID);

            if (player != null && player.isOnline()) {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }

    // Teleport Players
    public void teleportPlayers() {
        arena.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                player.clearTitle();
                player.teleportAsync(config.getWaitingSpawn());
            }
        });
    }

    // Block Breaking Mechanic
    public void handleMovements() {
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Location playerLocation = player.getLocation();
                Location blockLocation = playerLocation.clone().add(0, -1, 0);

                destroyBlock(blockLocation);
            }
        }
    }

    public void destroyBlock(Location loc) {
        int y = loc.getBlockY() + 1;
        Block block = null;

        for (int i = 0; i <= 1; i++) {
            block = getBlockUnderPlayer(y, loc);
            y--;
            if (block != null) {
                break;
            }
        }
        if (block != null) {
            Block fblock = block;

            if (!blocksToDestroy.contains(fblock)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        blocksToDestroy.remove(fblock);
                        removeBlock(fblock);
                    }
                }.runTaskLater(plugin, 8L);
            }
        }
    }

    private Block getBlockUnderPlayer(int y, Location location) {
        PlayerPosition loc = new PlayerPosition(location.getX(), y, location.getZ());
        double playerBoundingBox = 0.3;

        Block b11 = loc.getBlock(location.getWorld(), +playerBoundingBox, -playerBoundingBox);
        if (b11.getType() != Material.AIR && b11.getType() != Material.LIGHT) {
            return b11;
        }
        Block b12 = loc.getBlock(location.getWorld(), -playerBoundingBox, +playerBoundingBox);
        if (b12.getType() != Material.AIR && b12.getType() != Material.LIGHT) {
            return b12;
        }
        Block b21 = loc.getBlock(location.getWorld(), +playerBoundingBox, +playerBoundingBox);
        if (b21.getType() != Material.AIR && b21.getType() != Material.LIGHT) {
            return b21;
        }
        Block b22 = loc.getBlock(location.getWorld(), -playerBoundingBox, -playerBoundingBox);
        if (b22.getType() != Material.AIR && b22.getType() != Material.LIGHT) {
            return b22;
        }
        return null;
    }

    public void removeBlock(Block block) {
        Block underBlock = block.getRelative(BlockFace.DOWN);
        block.setType(Material.AIR);
        underBlock.setType(Material.AIR);
    }

    // Time Of Death
    public void setTimeOfDeath(Player player) {
        timeOfDeath.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void sendRankMessages(int maxRanks) {
        String[] colors = {"<green>", "<yellow>", "<red>"};
        for (int i = 1; i <= maxRanks; i++) {
            int rank = i;
            getPlayerByRank(rank).ifPresent(player -> arena.sendCenteredMessage(colors[rank - 1] + rank + "º Lugar <gray>- " + player.getName()));
        }
    }

    @Override
    public void destroyData() {
        if (playingTask != null) {
            playingTask.cancel();
            playingTask = null;
        }

        if (blockBreakingTask != null) {
            blockBreakingTask.cancel();
            blockBreakingTask = null;
        }

        if (timeOfDeath != null) {
            timeOfDeath.clear();
            timeOfDeath = null;
        }

        if (blocksToDestroy != null) {
            blocksToDestroy.clear();
            blocksToDestroy = null;
        }

        gameInstance = null;
        config = null;
    }

}
