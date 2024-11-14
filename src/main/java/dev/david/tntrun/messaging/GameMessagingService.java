package dev.david.tntrun.messaging;

import dev.david.tntrun.TNTRunTF;
import dev.david.tntrun.game.TNTRun;
import lombok.Getter;
import lombok.Setter;
import net.tfgames.common.api.game.modes.PackedTNTRun;
import net.tfgames.common.api.game.settings.GameType;
import net.tfgames.common.api.messaging.DefaultChannels;
import net.tfgames.common.api.messaging.MessagingChannel;
import net.tfgames.common.api.messaging.MessagingPacket;
import net.tfgames.common.api.messaging.TargetedPacket;
import net.tfgames.common.api.messaging.packets.game.GameCreateRequestPacket;
import net.tfgames.common.api.messaging.packets.game.GameCreateResponsePacket;
import net.tfgames.common.messaging.MessagingCommonManager;
import net.tfgames.engine.EngineTF;
import net.tfgames.engine.arena.Arena;
import net.tfgames.engine.arena.ArenaManager;
import net.tfgames.engine.arena.ArenaState;
import net.tfgames.engine.config.EngineConfig;
import org.bukkit.Bukkit;

import java.util.UUID;

@Setter
@Getter
public class GameMessagingService {

    @Getter
    private static GameMessagingService singleton;
    private String identity;

    private GameMessagingService(String identity) {
        this.identity = identity;
    }

    public static void init() {
        if (singleton != null) {
            return;
        }

        String sender = EngineTF.getSettings().getProperty(EngineConfig.ENGINE_SERVER_ID);
        singleton = new GameMessagingService(sender);
        singleton.registerChannels();
    }

    private void registerChannels() {
        var createGame = MessagingCommonManager.getInstance().getMessagingHandler().registerIncomingPacketChannel(
                new MessagingChannel<GameCreateRequestPacket>() {
                    @Override
                    public void read(GameCreateRequestPacket gameCreatePacket) {
                        if (isNotTargetedHere(gameCreatePacket)) return;
                        if (gameCreatePacket.getGame() == null) return;

                        if (gameCreatePacket.getGame() instanceof PackedTNTRun packet) {
                            Arena arena = ArenaManager.loadArena(new TNTRun(EngineTF.getInstance(), packet));
                            UUID uuid = arena.getGameId();

                            Bukkit.getScheduler().runTaskTimer(arena.getPlugin(), e -> {
                                if (arena.getCurrentState().equals(ArenaState.WAITING)) {
                                    MessagingCommonManager.getInstance().getMessagingHandler().sendPacket(
                                            DefaultChannels.GAME_CREATE_RESPONSE.toString(),
                                            new GameCreateResponsePacket(
                                                    getIdentity(),
                                                    null,
                                                    gameCreatePacket,
                                                    uuid
                                            ),
                                            false
                                    );
                                    e.cancel();
                                }
                            }, 0L, 20L);
                        }
                    }

                    @Override
                    public Class<GameCreateRequestPacket> getType() {
                        return GameCreateRequestPacket.class;
                    }

                    @Override
                    public String getName() {
                        return GameType.TNT_RUN.getDisplayTag() + "_" + DefaultChannels.GAME_CREATE_REQUEST;
                    }
                }
        );

        if (!createGame) {
            TNTRunTF.getInstance().getLogger().severe("Não foi possível registrar o canal GAMECREATE!");
        }

        /*var sessionCreate = MessagingCommonManager.getInstance().getMessagingHandler().registerIncomingPacketChannel(
                new MessagingChannel<SessionCreateRequestPacket>() {
                    @Override
                    public void read(SessionCreateRequestPacket sessionCreatePacket) {
                        System.out.println("RECEBIDO PACOTE DE SESSÃO");
                        PackedSession session = sessionCreatePacket.getSession();
                        if (session.getMap() == null) return;

                        if (sessionCreatePacket.getSession() instanceof PackedDuelsSetup duelsSetup) {
                            DuelSetup setup = new DuelSetup(duelsSetup.getMap(), duelsSetup.getTeamAmount());
                            setup.getEditors().addAll(sessionCreatePacket.getPlayers());

                            Bukkit.getScheduler().runTaskTimer(TFDuels.getInstance(), e -> {
                                TFRedisson.getRedisManager().sendPacket(
                                        DefaultChannels.SESSION_CREATE_RESPONSE.toString(),
                                        new SessionCreateResponsePacket(
                                                getIdentity(),
                                                sessionCreatePacket.getSender(),
                                                sessionCreatePacket
                                        ),
                                        false
                                );
                                e.cancel();
                            }, 0L, 20L);
                        }
                    }

                    @Override
                    public Class<SessionCreateRequestPacket> getType() {
                        return SessionCreateRequestPacket.class;
                    }

                    @Override
                    public String getName() {
                        return DefaultChannels.SESSION_CREATE_REQUEST.getName();
                    }
                }
        );

        if (!sessionCreate) {
            TFDuels.getInstance().getLogger().severe("Não foi possível registrar o canal SESSIONCREATE!");
        }*/
    }

    private boolean isNotTargetedHere(MessagingPacket packet) {
        if (!(packet instanceof TargetedPacket)) {
            return false;
        }
        if (null == ((TargetedPacket)packet).getTarget()) {
            return false;
        }
        return !((TargetedPacket) packet).getTarget().equals(getIdentity());
    }

}
