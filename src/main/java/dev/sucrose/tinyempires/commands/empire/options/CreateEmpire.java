package dev.sucrose.tinyempires.commands.empire.options;

import dev.sucrose.tinyempires.TinyEmpires;
import dev.sucrose.tinyempires.models.*;
import dev.sucrose.tinyempires.utils.BoundUtils;
import dev.sucrose.tinyempires.utils.DrawEmpire;
import dev.sucrose.tinyempires.utils.ErrorUtils;
import dev.sucrose.tinyempires.utils.StringUtils;
import org.bson.types.ObjectId;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CreateEmpire implements CommandOption {

    @Override
    public void execute(Player sender, String[] args) {
        // /e create <name>
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + getUsage());
            return;
        }

        final UUID senderUUID = sender.getUniqueId();
        final TEPlayer tePlayer = TEPlayer.getTEPlayer(senderUUID);
        if (tePlayer == null) {
            sender.sendMessage(ChatColor.RED + ErrorUtils.YOU_DO_NOT_EXIST_IN_THE_DATABASE);
            return;
        }

        if (tePlayer.isInEmpire()) {
            sender.sendMessage(ChatColor.RED + "You must leave your empire before creating one");
            return;
        }

        // no parsing necessary so no try/catch
        final String empireName = StringUtils.buildWordsFromArray(args, 0);
        if (Empire.getEmpire(empireName) != null) {
            sender.sendMessage(ChatColor.RED + String.format(
                "Empire with the name '%s' already exists",
                empireName
            ));
            return;
        }

        if (empireName.length() > 30) {
            sender.sendMessage(ChatColor.RED + "You cannot have an empire name longer than 30 characters");
            return;
        }

        final Location location = sender.getLocation();
        final Chunk chunk = location.getChunk();
        final TEChunk teChunk = TEChunk.getChunk(chunk);
        if (teChunk != null) {
            sender.sendMessage(ChatColor.RED + "This chunk is already owned by another empire");
            return;
        }

        if (BoundUtils.isChunkInBoundsOfSpecialTerritory(chunk)) {
            sender.sendMessage(ChatColor.RED + "You cannot claim this section of the map");
            return;
        }

        // all checks passed, make empire
        try {
            final World world = location.getWorld();
            if (world == null)
                throw new NullPointerException("World found as undefined when fetching player location");
            Empire.createEmpire(
                empireName,
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                tePlayer,
                id -> {
                    // run on main Spigot thread to avoid running asynchronously
                    Bukkit.getScheduler().runTask(
                        TinyEmpires.getInstance(),
                        () -> tePlayer.setEmpireId(id)
                    );

                    // insert initial empire chunk
                    final Empire empire = Empire.getEmpire(id);
                    if (empire == null)
                        throw new NullPointerException("Unable to fetch newly created empire and establish first chunk");
                    TEChunk.createTEChunk(world.getName(), chunk.getX(), chunk.getZ(), empire);
                    DrawEmpire.drawChunk(empire, world.getName(), chunk.getX(), chunk.getZ());
                    Bukkit.broadcastMessage(ChatColor.GREEN + String.format(
                        "%s created the empire of %s",
                        ChatColor.BOLD + sender.getDisplayName() + ChatColor.GREEN,
                        ChatColor.BOLD + empireName
                    ));
                }
            );

        } catch(Exception err) {
            err.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Failed to create new empire, please notify a developer");
        }
    }

    @Override
    public String getDescription() {
        return "Create empire";
    }

    @Override
    public Permission getPermissionRequired() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/e create <name>";
    }

}
