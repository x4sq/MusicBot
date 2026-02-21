package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 * Slash command version of NowPlayingCmd.
 */
public class SlashNowPlayingCmd extends SlashMusicCommand
{
    public SlashNowPlayingCmd(Bot bot)
    {
        super(bot);
        this.name = "nowplaying";
        this.help = "shows the song that is currently playing";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        if (handler == null)
        {
            event.reply(event.getClient().getWarning() + " There is no music playing in this server.")
                    .setEphemeral(true).queue();
            return;
        }

        MessageCreateData nowPlayingMsg = handler.getNowPlaying(event.getJDA());
        if (nowPlayingMsg == null)
        {
            MessageCreateData noMusic = handler.getNoMusicPlaying(event.getJDA());
            event.reply(noMusic).queue(msg -> bot.getNowplayingHandler().clearLastNPMessage(event.getGuild()));
        }
        else
        {
            event.reply(nowPlayingMsg).queue(msg -> bot.getNowplayingHandler().setLastNPMessage(msg.retrieveOriginal().complete()));
        }
    }
}
