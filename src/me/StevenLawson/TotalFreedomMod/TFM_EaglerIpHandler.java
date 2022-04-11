package me.StevenLawson.TotalFreedomMod;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class TFM_EaglerIpHandler implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        if (in.readUTF().equals("Login"))
        {
            player.setMetadata("eagler_ip", new FixedMetadataValue(TotalFreedomMod.plugin, in.readUTF()));
        }
    }
}