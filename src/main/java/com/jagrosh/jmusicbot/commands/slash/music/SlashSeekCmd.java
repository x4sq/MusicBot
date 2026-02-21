package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Slash command version of SeekCmd.
 */
public class SlashSeekCmd extends SlashMusicCommand
{
    private static final Logger LOG = LoggerFactory.getLogger("Seeking");

    public SlashSeekCmd(Bot bot)
    {
        super(bot);
        this.name = "seek";
        this.help = "seeks the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "time",
                        "Time to seek to: [+|-] HH:MM:SS | MM:SS | SS | 0h0m0s | 0m0s | 0s", true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AudioTrack playingTrack = handler.getPlayer().getPlayingTrack();

        if (!playingTrack.isSeekable())
        {
            event.reply("This track is not seekable.").setEphemeral(true).queue();
            return;
        }

        if (!SlashDJCommand.checkDJPermission(event)
                && playingTrack.getUserData(RequestMetadata.class).getOwner() != event.getUser().getIdLong())
        {
            event.reply("You cannot seek **" + playingTrack.getInfo().title
                    + "** because you didn't add it!").setEphemeral(true).queue();
            return;
        }

        String args = event.optString("time", "");
        TimeUtil.SeekTime seekTime = TimeUtil.parseTime(args);
        if (seekTime == null)
        {
            event.reply("Invalid seek! Expected format: `[+|-] HH:MM:SS | MM:SS | SS | 1h10m | +90s`")
                    .setEphemeral(true).queue();
            return;
        }

        long currentPosition = playingTrack.getPosition();
        long trackDuration = playingTrack.getDuration();
        long seekMilliseconds = seekTime.relative ? currentPosition + seekTime.milliseconds : seekTime.milliseconds;

        if (seekMilliseconds > trackDuration)
        {
            event.reply("Cannot seek to `" + TimeUtil.formatTime(seekMilliseconds)
                    + "` because the current track is `" + TimeUtil.formatTime(trackDuration) + "` long!")
                    .setEphemeral(true).queue();
            return;
        }

        try
        {
            playingTrack.setPosition(seekMilliseconds);
        }
        catch (Exception e)
        {
            event.reply("An error occurred while trying to seek: " + e.getMessage())
                    .setEphemeral(true).queue();
            LOG.warn("Failed to seek track " + playingTrack.getIdentifier(), e);
            return;
        }
        event.reply(event.getClient().getSuccess() + " Successfully seeked to `"
                + TimeUtil.formatTime(playingTrack.getPosition()) + "/"
                + TimeUtil.formatTime(playingTrack.getDuration()) + "`!").queue();
    }
}
