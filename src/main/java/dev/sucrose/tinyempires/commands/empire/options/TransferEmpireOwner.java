package dev.sucrose.tinyempires.commands.empire.options;

import dev.sucrose.tinyempires.discord.DiscordBot;
import dev.sucrose.tinyempires.models.Empire;
import dev.sucrose.tinyempires.models.CommandOption;
import dev.sucrose.tinyempires.models.Permission;
import dev.sucrose.tinyempires.models.TEPlayer;
import dev.sucrose.tinyempires.utils.ErrorUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TransferEmpireOwner implements CommandOption {

    @Override
    public void execute(Player sender, String[] args) {
        // /e owner <player>
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + getUsage());
            return;
        }
        final String playerName = args[0];

        final UUID senderUUID = sender.getUniqueId();
        final TEPlayer tePlayer = TEPlayer.getTEPlayer(senderUUID);
        if (tePlayer == null) {
            sender.sendMessage(ChatColor.RED + ErrorUtils.YOU_DO_NOT_EXIST_IN_THE_DATABASE);
            return;
        }

        if (!tePlayer.isInEmpire()) {
            sender.sendMessage(ChatColor.RED + "You must be in an empire to run this command");
            return;
        }
        final Empire empire = tePlayer.getEmpire();

        if (!tePlayer.isOwner()) {
            sender.sendMessage(ChatColor.RED + "You must be the owner of the empire to transfer (%s is the current " +
                "owner)");
            return;
        }

        final TEPlayer newOwner = TEPlayer.getTEPlayer(playerName);
        if (newOwner == null) {
            sender.sendMessage(ChatColor.RED + String.format(
                "'%s' is not an existing player",
                playerName
            ));
            return;
        }

        DiscordBot.removeEmpireOwnerRoleFromUser(tePlayer);
        DiscordBot.giveUserEmpireOwnerRole(newOwner);
        empire.setOwner(newOwner.getPlayerUUID());
        empire.broadcastText(ChatColor.YELLOW + String.format(
            "%s has transferred ownership of the empire to %s!",
            ChatColor.BOLD + sender.getName() + ChatColor.YELLOW,
            ChatColor.BOLD + playerName + ChatColor.YELLOW
        ));
    }

    @Override
    public String getDescription() {
        return "Transfer ownership onto another member (must be owner)";
    }

    @Override
    public Permission getPermissionRequired() {
        return null;
    }

    @Override
    public String getUsage() {
        return "/e owner <player>";
    }

}
