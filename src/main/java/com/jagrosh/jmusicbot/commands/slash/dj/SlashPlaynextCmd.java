package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of PlaynextCmd.
 */
public class SlashPlaynextCmd extends SlashDJCommand
{
    private final String loadingEmoji;

    public SlashPlaynextCmd(Bot bot)
    {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "playnext";
        this.help = "plays a single song next";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "query", "Song title or URL to play next", true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String query = event.optString("query", "");
        if (query.startsWith("<") && query.endsWith(">"))
            query = query.substring(1, query.length() - 1);

        final String args = query;
        event.reply(loadingEmoji + " Loading... `[" + args + "]`").queue(hook ->
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), args,
                        new ResultHandler(hook, event, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler
    {
        private final InteractionHook hook;
        private final SlashCommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(InteractionHook hook, SlashCommandEvent event, boolean ytsearch)
        {
            this.hook = hook;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        private void loadSingle(AudioTrack track)
        {
            if (bot.getConfig().isTooLong(track))
            {
                hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                        + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `"
                        + TimeUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrackToFront(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
            hook.editOriginal(FormatUtil.filter(event.getClient().getSuccess()
                    + " Added **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "to begin playing" : "to the queue at position " + pos))).queue();
        }

        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            AudioTrack single;
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
                single = playlist.getSelectedTrack() == null
                        ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            else if (playlist.getSelectedTrack() != null)
                single = playlist.getSelectedTrack();
            else
                single = playlist.getTracks().get(0);
            loadSingle(single);
        }

        @Override
        public void noMatches()
        {
            if (ytsearch)
                hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                        + " No results found for `" + event.optString("query", "") + "`.")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(),
                        "ytsearch:" + event.optString("query", ""),
                        new ResultHandler(hook, event, true));
        }

        @Override
        public void loadFailed(FriendlyException throwable)
        {
            if (throwable.severity == FriendlyException.Severity.COMMON)
                hook.editOriginal(event.getClient().getError() + " Error loading: " + throwable.getMessage()).queue();
            else
                hook.editOriginal(event.getClient().getError() + " Error loading track.").queue();
        }
    }
}
