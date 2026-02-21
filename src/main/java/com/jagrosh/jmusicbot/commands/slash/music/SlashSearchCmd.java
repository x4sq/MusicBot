package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SearchCmd.
 * Returns the top 5 YouTube search results as a text list.
 */
public class SlashSearchCmd extends SlashMusicCommand
{
    protected String searchPrefix = "ytsearch:";
    private final String searchingEmoji;

    public SlashSearchCmd(Bot bot)
    {
        super(bot);
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "search";
        this.help = "searches YouTube for a provided query and shows top results";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "query", "Search query", true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String query = event.optString("query", "");
        if (query.isEmpty())
        {
            event.reply("Please include a query.").setEphemeral(true).queue();
            return;
        }

        event.reply(searchingEmoji + " Searching... `[" + query + "]`").queue(hook ->
                bot.getPlayerManager().loadItemOrdered(event.getGuild(),
                        searchPrefix + query,
                        new ResultHandler(hook, event, query)));
    }

    private class ResultHandler implements AudioLoadResultHandler
    {
        private final net.dv8tion.jda.api.interactions.InteractionHook hook;
        private final SlashCommandEvent event;
        private final String query;

        private ResultHandler(net.dv8tion.jda.api.interactions.InteractionHook hook,
                              SlashCommandEvent event, String query)
        {
            this.hook = hook;
            this.event = event;
            this.query = query;
        }

        @Override
        public void trackLoaded(AudioTrack track)
        {
            if (bot.getConfig().isTooLong(track))
            {
                hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                        + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `"
                        + bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
            hook.editOriginal(FormatUtil.filter(event.getClient().getSuccess()
                    + " Added **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "to begin playing" : "to the queue at position " + pos))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            if (playlist.getTracks().isEmpty())
            {
                hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                        + " No results found for `" + query + "`.")).queue();
                return;
            }

            StringBuilder sb = new StringBuilder(FormatUtil.filter(
                    event.getClient().getSuccess() + " Search results for `" + query + "`:\n"));
            int count = Math.min(5, playlist.getTracks().size());
            for (int i = 0; i < count; i++)
            {
                AudioTrack track = playlist.getTracks().get(i);
                sb.append("`").append(i + 1).append(".` [`")
                        .append(TimeUtil.formatTime(track.getDuration())).append("`] **")
                        .append(FormatUtil.filter(track.getInfo().title)).append("** — <")
                        .append(track.getInfo().uri).append(">\n");
            }
            sb.append("\nUse `/play <URL>` to play one of these tracks.");
            hook.editOriginal(sb.toString()).queue();
        }

        @Override
        public void noMatches()
        {
            hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                    + " No results found for `" + query + "`.")).queue();
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
