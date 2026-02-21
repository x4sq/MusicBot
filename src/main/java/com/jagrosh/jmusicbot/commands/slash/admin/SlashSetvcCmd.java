package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SetvcCmd.
 * Uses Discord's native CHANNEL option type.
 */
public class SlashSetvcCmd extends SlashAdminCommand
{
    public SlashSetvcCmd(Bot bot)
    {
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.CHANNEL, "channel",
                        "Voice channel to restrict music to (omit to clear restriction)", false)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (!event.hasOption("channel"))
        {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + " Music can now be played in any channel").queue();
            return;
        }

        var channel = event.getOption("channel").getAsChannel();
        if (!(channel instanceof VoiceChannel))
        {
            event.reply(event.getClient().getError() + " Please select a voice channel!")
                    .setEphemeral(true).queue();
            return;
        }

        s.setVoiceChannel((VoiceChannel) channel);
        event.reply(event.getClient().getSuccess() + " Music can now only be played in "
                + ((VoiceChannel) channel).getAsMention()).queue();
    }
}
