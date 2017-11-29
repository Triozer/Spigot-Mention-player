package fr.triozer.mentionplayer.misc;

import fr.triozer.mentionplayer.MentionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

/**
 * @author Cédric / Triozer
 */
public class Settings {

    public static boolean canGUI() {
        return Bukkit.getPluginManager().getPlugin("SmartInvs") != null || MentionPlayer.getInstance().getConfig().getBoolean("option.gui");
    }

    public static long getInterval() {
        if (MentionPlayer.getInstance().getConfig().getBoolean("option.anti-spam.enable"))
            return MentionPlayer.getInstance().getConfig().getLong("option.anti-spam.interval") * 1000;
        else
            return 0L;
    }

    public static boolean canTabComplete() {
        return MentionPlayer.getInstance().getConfig().getBoolean("option.tab-complete");
    }

    public static ChatColor textColor() {
        return ChatColor.valueOf(MentionPlayer.getInstance().getConfig().getString("format.text-color"));
    }

    public static String formatChat(String playerName) {
        return MentionPlayer.getInstance().getConfig().getString("format.chat").replaceAll("&", "§").replace("{player-name}", playerName);
    }

    public static String formatActionBar(String playerName) {
        return MentionPlayer.getInstance().getConfig().getString("format.action-bar").replaceAll("&", "§").replace("{player-name}", playerName);
    }

    public static Sound getSound() {
        Sound sound;

        try {
            sound = Sound.valueOf(MentionPlayer.getInstance().getConfig().getString("option.sound"));
        } catch (IllegalArgumentException var2) {
            String version = Bukkit.getServer().getClass().getPackage().getName();
            version = version.substring(version.lastIndexOf(".") + 1);

            if (version.contains("v1_8")) {
                sound = Sound.valueOf("NOTE_PLING");
            } else {
                sound = Sound.valueOf("BLOCK_NOTE_PLING");
            }

            Bukkit.getConsoleSender().sendMessage("§c\"config.yml\" is configured improperly! Set '" + sound.name() + "' sound.");
        }

        return sound;
    }

}
