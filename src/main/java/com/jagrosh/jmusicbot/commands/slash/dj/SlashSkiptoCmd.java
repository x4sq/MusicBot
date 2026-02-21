package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SkiptoCmd.
 */
public class SlashSkiptoCmd extends SlashDJCommand
{
    public SlashSkiptoCmd(Bot bot)
    {
        super(bot);
        this.name = "skipto";
        this.help = "skips to the specified song in the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "position",
                        "Queue position to skip to", true).setMinValue(1)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        int index = (int) event.optLong("position", 0);
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        if (index < 1 || index > handler.getQueue().size())
        {
            event.reply(event.getClient().getError()
                    + " Position must be a valid integer between 1 and "
                    + handler.getQueue().size() + "!").setEphemeral(true).queue();
            return;
        }

        handler.getQueue().skip(index - 1);
        event.reply(event.getClient().getSuccess() + " Skipped to **"
                + handler.getQueue().get(0).getTrack().getInfo().title + "**").queue();
        handler.getPlayer().stopTrack();
    }
}
