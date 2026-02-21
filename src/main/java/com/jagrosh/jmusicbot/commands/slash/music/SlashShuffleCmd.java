package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;

/**
 * Slash command version of ShuffleCmd.
 */
public class SlashShuffleCmd extends SlashMusicCommand
{
    public SlashShuffleCmd(Bot bot)
    {
        super(bot);
        this.name = "shuffle";
        this.help = "shuffles songs you have added";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int s = handler.getQueue().shuffle(event.getUser().getIdLong());
        switch (s)
        {
            case 0:
                event.reply("You don't have any music in the queue to shuffle!").setEphemeral(true).queue();
                break;
            case 1:
                event.reply(event.getClient().getWarning() + " You only have one song in the queue!").queue();
                break;
            default:
                event.reply(event.getClient().getSuccess() + " You successfully shuffled your " + s + " entries.").queue();
                break;
        }
    }
}
