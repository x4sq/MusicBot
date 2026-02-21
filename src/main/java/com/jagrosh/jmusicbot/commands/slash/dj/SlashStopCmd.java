package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;

/**
 * Slash command version of StopCmd.
 */
public class SlashStopCmd extends SlashDJCommand
{
    public SlashStopCmd(Bot bot)
    {
        super(bot);
        this.name = "stop";
        this.help = "stops the current song and clears the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply(event.getClient().getSuccess()
                + " The player has stopped and the queue has been cleared.").queue();
    }
}
