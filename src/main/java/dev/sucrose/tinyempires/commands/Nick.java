package dev.sucrose.tinyempires.commands;

import dev.sucrose.tinyempires.models.TEPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Nick implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /nick <name>
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "/nick <name>");
            return false;
        }

        final String nickname = args[0];
        final TEPlayer tePlayer = TEPlayer.getTEPlayer(nickname);
        if (tePlayer != null) {
            sender.sendMessage(ChatColor.RED + String.format(
                "%s is an existing player! Nickname your player something else to avoid confusion",
                ChatColor.BOLD + nickname + ChatColor.RED
            ));
            return false;
        }

        final Player player = (Player) sender;
        Bukkit.broadcastMessage(ChatColor.YELLOW + String.format(
            "%s nicknamed themselves to %s",
            ChatColor.BOLD + player.getName() + ChatColor.YELLOW,
            ChatColor.BOLD + nickname
        ));

        player.setDisplayName(nickname);
        return true;
    }

}
