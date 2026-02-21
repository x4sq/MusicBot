package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.queue.AbstractQueue;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;

/**
 * Slash command version of MoveTrackCmd.
 */
public class SlashMoveTrackCmd extends SlashDJCommand
{
    public SlashMoveTrackCmd(Bot bot)
    {
        super(bot);
        this.name = "movetrack";
        this.help = "moves a track in the current queue to a different position";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER, "from",
                        "Current position of the track in the queue", true).setMinValue(1),
                new OptionData(OptionType.INTEGER, "to",
                        "New position for the track in the queue", true).setMinValue(1)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        int from = (int) event.optLong("from", 0);
        int to = (int) event.optLong("to", 0);

        if (from == to)
        {
            event.reply("Can't move a track to the same position.").setEphemeral(true).queue();
            return;
        }

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AbstractQueue<QueuedTrack> queue = handler.getQueue();

        if (from < 1 || from > queue.size())
        {
            event.reply("`" + from + "` is not a valid position in the queue!")
                    .setEphemeral(true).queue();
            return;
        }
        if (to < 1 || to > queue.size())
        {
            event.reply("`" + to + "` is not a valid position in the queue!")
                    .setEphemeral(true).queue();
            return;
        }

        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        event.reply(event.getClient().getSuccess() + " Moved **" + track.getTrack().getInfo().title
                + "** from position `" + from + "` to `" + to + "`.").queue();
    }
}
