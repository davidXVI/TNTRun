package dev.david.tntrun.game.components;

import dev.david.tntrun.TNTRunTF;
import dev.david.tntrun.game.TNTRun;
import me.catcoder.sidebar.ProtocolSidebar;
import me.catcoder.sidebar.Sidebar;
import net.tfgames.engine.game.modules.GameScoreboard;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TNTSidebar implements GameScoreboard {

    private final String serverId = TNTRunTF.getInstance().getServerId();
    protected final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    private TNTRun game;
    private Map<UUID, Sidebar<?>> sidebars;

    public TNTSidebar(TNTRun game) {
        this.game = game;
        this.sidebars = new HashMap<>();
    }

    @Override
    public void addSidebar(Player player, String spectated) {
        Sidebar<String> sidebar = ProtocolSidebar.newMiniMessageSidebar("<yellow><bold>TNT-RUN", game.getPlugin());
        sidebar.addLine("  <gray>" + df.format(new Date(System.currentTimeMillis())) + "  <dark_gray>" + serverId + "  ");
        sidebar.addBlankLine();

        if (spectated != null) {
            sidebar.addLine(" ∞ Assistindo: " + spectated);
            sidebar.addBlankLine();
        }

        sidebar.addUpdatableLine(p -> " ⌚ Duração: <green>" + String.format("%02d:%02d", game.getPlayingTask().getSeconds() / 60, game.getPlayingTask().getSeconds() % 60));
        sidebar.addBlankLine();

        sidebar.addUpdatableLine(p -> " \uD83C\uDFF9 Vivos: <green>" + game.getArena().getPlayers().size());
        sidebar.addBlankLine();

        sidebar.addLine(" ⛏ Mapa: <green>" + game.getMap());
        sidebar.addBlankLine();

        sidebar.addLine(" <yellow>jogar.tfgames.com.br ");

        sidebar.getObjective().scoreNumberFormatBlank();
        sidebar.updateLinesPeriodically(0, 20, true);

        sidebar.addViewer(player);
        sidebars.put(player.getUniqueId(), sidebar);
    }

    @Override
    public void removeSidebar(Player player) {
        if (sidebars.containsKey(player.getUniqueId())) {
            sidebars.get(player.getUniqueId()).destroy();
            sidebars.remove(player.getUniqueId());
        }
    }

    @Override
    public void destroy() {
        if (sidebars != null && !sidebars.isEmpty()) {
            for (Sidebar<?> sidebar : sidebars.values()) {
                sidebar.destroy();
            }
            sidebars.clear();
            this.sidebars = null;
        }
        this.game = null;
    }

}
