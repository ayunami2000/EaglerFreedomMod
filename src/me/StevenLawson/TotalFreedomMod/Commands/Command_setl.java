package me.StevenLawson.TotalFreedomMod.Commands;

import me.StevenLawson.TotalFreedomMod.TFM_Util;
import me.StevenLawson.TotalFreedomMod.TFM_WorldEditBridge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = AdminLevel.SUPER, source = SourceType.BOTH)
@CommandParameters(description = "Sets everyone's Worldedit block modification limit to x.", usage = "/<command>")
public class Command_setl extends TFM_Command
{
    @Override
    public boolean run(CommandSender sender, Player sender_p, Command cmd, String commandLabel, String[] args, boolean senderIsConsole)
    {
        int lim = 500;
        try
        {
            lim = args.length == 0 ? 500 : Math.max(1, Integer.parseInt(args[0]));
        } catch (NumberFormatException ignored) {}
        TFM_Util.adminAction(sender.getName(), "Setting everyone's Worldedit block modification limit to " + lim + ".", true);
        TFM_WorldEditBridge web = TFM_WorldEditBridge.getInstance();
        for (final Player player : server.getOnlinePlayers())
        {
            web.setLimit(player, lim);
        }
        return true;
    }
}
