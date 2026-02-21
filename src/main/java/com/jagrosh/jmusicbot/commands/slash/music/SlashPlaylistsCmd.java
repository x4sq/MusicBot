package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;

import java.util.List;

/**
 * Slash command version of PlaylistsCmd.
 */
public class SlashPlaylistsCmd extends SlashMusicCommand
{
    public SlashPlaylistsCmd(Bot bot)
    {
        super(bot);
        this.name = "playlists";
        this.help = "shows the available playlists";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if (!bot.getPlaylistLoader().folderExists())
            bot.getPlaylistLoader().createFolder();
        if (!bot.getPlaylistLoader().folderExists())
        {
            event.reply(event.getClient().getWarning()
                    + " Playlists folder does not exist and could not be created!").setEphemeral(true).queue();
            return;
        }
        List<String> list = bot.getPlaylistLoader().getPlaylistNames();
        if (list == null)
        {
            event.reply(event.getClient().getError() + " Failed to load available playlists!")
                    .setEphemeral(true).queue();
        }
        else if (list.isEmpty())
        {
            event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!").queue();
        }
        else
        {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\nUse `/play playlist <name>` to play a playlist.");
            event.reply(builder.toString()).queue();
        }
    }
}
