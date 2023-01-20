package adhdmc.sleepmessages;

import adhdmc.sleepmessages.util.SMMessage;
import adhdmc.sleepmessages.util.SMPerm;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameRule;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.List;

public class SleepListener implements Listener {
    MiniMessage miniMessage = SleepMessages.getMiniMessage();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerSleep(PlayerDeepSleepEvent event) {
        if (SMMessage.PLAYER_SLEEPING.getMessage() == null || SMMessage.PLAYER_SLEEPING.getMessage().isEmpty()) return;
        if (event.getPlayer().hasPermission(SMPerm.BYPASS.getPerm())) return;
        World world = event.getPlayer().getWorld();
        int worldOnlineTotal = world.getPlayerCount();
        Integer worldSleepPercent = world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
        if (worldSleepPercent == null) return;
        Component playerName = event.getPlayer().displayName();
        String worldName = world.getName();
        int currentSleepCount = 0;
        Integer neededSleepers = (int) Math.ceil((worldSleepPercent / 100.0) * worldOnlineTotal);
        boolean purpurAFKSleep = SleepMessages.getInstance().getConfig().getBoolean("purpur-afk-count-as-sleeping");
        boolean worldMessage = SleepMessages.getInstance().getConfig().getBoolean("world-specific-message");
        List<Player> playerList = world.getPlayers();
        if (SleepMessages.getInstance().isPurpurEnabled() && purpurAFKSleep) {
            for (Player player : playerList) {
                if (player.isDeeplySleeping()) currentSleepCount += 1;
                if (player.isAfk()) currentSleepCount += 1;
            }
        } else {
            for (Player player : playerList) {
                if (player.isDeeplySleeping()) currentSleepCount += 1;
            }
        }
        if (worldMessage) {
            sendWorldMessage(world, playerName, currentSleepCount, neededSleepers, worldName, SMMessage.PLAYER_SLEEPING.getMessage());
        } else {
            sendServerMessage(SleepMessages.getInstance().getServer(), playerName, currentSleepCount, neededSleepers, worldName, SMMessage.PLAYER_SLEEPING.getMessage());
        }
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void nightSkip(TimeSkipEvent event) {
        if (!event.getSkipReason().equals(TimeSkipEvent.SkipReason.NIGHT_SKIP)) return;
        if (SMMessage.NIGHT_SKIP.getMessage() == null || SMMessage.NIGHT_SKIP.getMessage().isEmpty()) return;
        boolean worldMessage = SleepMessages.getInstance().getConfig().getBoolean("world-specific-message");
        World world = event.getWorld();
        String worldName = event.getWorld().getName();
        if (worldMessage) {
            sendWorldMessage(world, miniMessage.deserialize(""), 0, 0, worldName, SMMessage.NIGHT_SKIP.getMessage());
        } else {
            sendServerMessage(SleepMessages.getInstance().getServer(), miniMessage.deserialize(""), 0, 0, worldName, SMMessage.NIGHT_SKIP.getMessage());
        }

    }

    private void sendWorldMessage(World world, Component playername, Integer sleepingPlayers, Integer neededSleepers, String worldName, String message) {
        world.sendMessage(miniMessage.deserialize(message,
                Placeholder.component("playername", playername),
                Placeholder.parsed("sleeping", sleepingPlayers.toString()),
                Placeholder.parsed("needed", neededSleepers.toString()),
                Placeholder.parsed("worldname", worldName)));
    }

    private void sendServerMessage(Server server, Component playername, Integer sleepingPlayers, Integer neededSleepers, String worldName, String message) {
        server.sendMessage(miniMessage.deserialize(message,
                Placeholder.component("playername", playername),
                Placeholder.parsed("sleeping", sleepingPlayers.toString()),
                Placeholder.parsed("needed", neededSleepers.toString()),
                Placeholder.parsed("worldname", worldName)));
    }
}

