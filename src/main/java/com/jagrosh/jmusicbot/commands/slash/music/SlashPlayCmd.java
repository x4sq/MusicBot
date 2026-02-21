package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
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
 * Slash command version of PlayCmd.
 * Supports /play <query|URL> and /play playlist <name> as a subcommand.
 */
public class SlashPlayCmd extends SlashMusicCommand
{
    private final String loadingEmoji;

    public SlashPlayCmd(Bot bot)
    {
        super(bot);
        this.loadingEmoji = bot.getConfig().getLoading();
        this.name = "play";
        this.help = "plays the provided song or resumes if paused";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "query", "Song title, URL, or playlist name to play", false)
        );
        this.children = new SlashMusicCommand[]{new PlaylistSubCmd(bot)};
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        String query = event.optString("query", "");

        // No query: try to resume if paused
        if (query.isEmpty())
        {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler.getPlayer().getPlayingTrack() != null && handler.getPlayer().isPaused())
            {
                if (SlashDJCommand.checkDJPermission(event))
                {
                    handler.getPlayer().setPaused(false);
                    event.reply(event.getClient().getSuccess() + " Resumed **"
                            + handler.getPlayer().getPlayingTrack().getInfo().title + "**.").queue();
                }
                else
                {
                    event.reply("Only DJs can unpause the player!").setEphemeral(true).queue();
                }
                return;
            }
            event.reply(event.getClient().getWarning()
                    + " Please provide a song title or URL, or use `/play playlist <name>` to play a playlist.")
                    .setEphemeral(true).queue();
            return;
        }

        // Strip angle brackets if present
        if (query.startsWith("<") && query.endsWith(">"))
            query = query.substring(1, query.length() - 1);

        final String args = query;
        event.reply(loadingEmoji + " Loading... `[" + args + "]`").queue(hook ->
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), args,
                        new ResultHandler(hook, event, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler
    {
        private final net.dv8tion.jda.api.interactions.InteractionHook hook;
        private final SlashCommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(net.dv8tion.jda.api.interactions.InteractionHook hook,
                              SlashCommandEvent event, boolean ytsearch)
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
            int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
            hook.editOriginal(FormatUtil.filter(event.getClient().getSuccess()
                    + " Added **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "to begin playing" : "to the queue at position " + pos))).queue();
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude)
        {
            int[] count = {0};
            playlist.getTracks().forEach(track -> {
                if (!bot.getConfig().isTooLong(track) && !track.equals(exclude))
                {
                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event)));
                    count[0]++;
                }
            });
            return count[0];
        }

        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult())
            {
                AudioTrack single = playlist.getSelectedTrack() == null
                        ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                loadSingle(single);
            }
            else if (playlist.getSelectedTrack() != null)
            {
                loadSingle(playlist.getSelectedTrack());
            }
            else
            {
                int count = loadPlaylist(playlist, null);
                if (playlist.getTracks().isEmpty())
                {
                    hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                            + " The playlist " + (playlist.getName() == null ? "" : "(**" + playlist.getName() + "**) ")
                            + "could not be loaded or contained 0 entries")).queue();
                }
                else if (count == 0)
                {
                    hook.editOriginal(FormatUtil.filter(event.getClient().getWarning()
                            + " All entries in this playlist "
                            + (playlist.getName() == null ? "" : "(**" + playlist.getName() + "**) ")
                            + "were longer than the allowed maximum (`" + bot.getConfig().getMaxTime() + "`)")).queue();
                }
                else
                {
                    hook.editOriginal(FormatUtil.filter(event.getClient().getSuccess()
                            + " Found " + (playlist.getName() == null ? "a playlist" : "playlist **" + playlist.getName() + "**")
                            + " with `" + playlist.getTracks().size() + "` entries; added to the queue!"
                            + (count < playlist.getTracks().size()
                                    ? "\n" + event.getClient().getWarning()
                                            + " Tracks longer than the allowed maximum (`"
                                            + bot.getConfig().getMaxTime() + "`) have been omitted."
                                    : ""))).queue();
                }
            }
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

    /**
     * Subcommand: /play playlist <name>
     */
    public class PlaylistSubCmd extends SlashMusicCommand
    {
        public PlaylistSubCmd(Bot bot)
        {
            super(bot);
            this.name = "playlist";
            this.help = "plays the provided playlist";
            this.beListening = true;
            this.bePlaying = false;
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "name", "Name of the playlist to play", true)
            );
        }

        @Override
        public void doCommand(SlashCommandEvent event)
        {
            String name = event.optString("name", "");
            if (name.isEmpty())
            {
                event.reply(event.getClient().getError() + " Please include a playlist name.")
                        .setEphemeral(true).queue();
                return;
            }
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(name);
            if (playlist == null)
            {
                event.reply("I could not find `" + name + ".txt` in the Playlists folder.")
                        .setEphemeral(true).queue();
                return;
            }
            event.reply(loadingEmoji + " Loading playlist **" + name + "**... ("
                    + playlist.getItems().size() + " items)").queue(hook ->
            {
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(),
                        at -> handler.addTrack(new QueuedTrack(at, RequestMetadata.fromResultHandler(at, event))),
                        () -> {
                            StringBuilder builder = new StringBuilder(
                                    playlist.getTracks().isEmpty()
                                            ? event.getClient().getWarning() + " No tracks were loaded!"
                                            : event.getClient().getSuccess() + " Loaded **"
                                                    + playlist.getTracks().size() + "** tracks!");
                            if (!playlist.getErrors().isEmpty())
                                builder.append("\nThe following tracks failed to load:");
                            playlist.getErrors().forEach(err -> builder.append("\n`[")
                                    .append(err.getIndex() + 1).append("]` **")
                                    .append(err.getItem()).append("**: ").append(err.getReason()));
                            String str = builder.toString();
                            if (str.length() > 2000)
                                str = str.substring(0, 1994) + " (...)";
                            hook.editOriginal(FormatUtil.filter(str)).queue();
                        });
            });
        }
    }
}
