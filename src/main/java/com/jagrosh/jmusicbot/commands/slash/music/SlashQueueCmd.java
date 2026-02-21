package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

/**
 * Slash command version of QueueCmd.
 * Shows the current queue as an embed (first page, up to 10 entries).
 */
public class SlashQueueCmd extends SlashMusicCommand
{
    private static final int ITEMS_PER_PAGE = 10;

    public SlashQueueCmd(Bot bot)
    {
        super(bot);
        this.name = "queue";
        this.help = "shows the current queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "page", "Page number to display", false)
                        .setMinValue(1)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        int pagenum = (int) event.optLong("page", 1);
        AudioHandler ah = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        List<QueuedTrack> list = ah.getQueue().getList();

        if (list.isEmpty())
        {
            event.reply(event.getClient().getWarning() + " There is no music in the queue!").queue();
            return;
        }

        int totalPages = (int) Math.ceil((double) list.size() / ITEMS_PER_PAGE);
        if (pagenum > totalPages) pagenum = totalPages;

        long total = 0;
        for (QueuedTrack qt : list)
            total += qt.getTrack().getDuration();

        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        RepeatMode repeatMode = settings.getRepeatMode();
        QueueType queueType = settings.getQueueType();

        StringBuilder title = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null)
        {
            title.append(ah.getStatusEmoji()).append(" **")
                    .append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        }
        title.append(event.getClient().getSuccess())
                .append(" Current Queue | ").append(list.size()).append(" entries | `")
                .append(TimeUtil.formatTime(total)).append("` | ")
                .append(queueType.getEmoji()).append(" `").append(queueType.getUserFriendlyName()).append("`");
        if (repeatMode.getEmoji() != null)
            title.append(" | ").append(repeatMode.getEmoji());

        int start = (pagenum - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, list.size());

        StringBuilder desc = new StringBuilder();
        for (int i = start; i < end; i++)
        {
            desc.append("`").append(i + 1).append(".` ").append(list.get(i).toString()).append("\n");
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(event.getMember().getColors().getPrimary())
                .setDescription(desc.toString())
                .setFooter("Page " + pagenum + "/" + totalPages, null);

        event.replyEmbeds(eb.build())
                .addContent(FormatUtil.filter(title.toString()))
                .queue();
    }
}
