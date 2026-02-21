package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;

/**
 * Slash command version of PauseCmd.
 */
public class SlashPauseCmd extends SlashDJCommand
{
    public SlashPauseCmd(Bot bot)
    {
        super(bot);
        this.name = "pause";
        this.help = "pauses the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused())
        {
            event.reply(event.getClient().getWarning()
                    + " The player is already paused! Use `/play` to unpause!").queue();
            return;
        }
        handler.getPlayer().setPaused(true);
        event.reply(event.getClient().getSuccess() + " Paused **"
                + handler.getPlayer().getPlayingTrack().getInfo().title
                + "**. Use `/play` to unpause!").queue();
    }
}
