package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SettcCmd.
 * Uses Discord's native CHANNEL option type.
 */
public class SlashSettcCmd extends SlashAdminCommand
{
    public SlashSettcCmd(Bot bot)
    {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.CHANNEL, "channel",
                        "Text channel to restrict music commands to (omit to clear restriction)", false)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (!event.hasOption("channel"))
        {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + " Music commands can now be used in any channel").queue();
            return;
        }

        var channel = event.getOption("channel").getAsChannel();
        if (!(channel instanceof TextChannel))
        {
            event.reply(event.getClient().getError() + " Please select a text channel!")
                    .setEphemeral(true).queue();
            return;
        }

        s.setTextChannel((TextChannel) channel);
        event.reply(event.getClient().getSuccess() + " Music commands can now only be used in "
                + ((TextChannel) channel).getAsMention()).queue();
    }
}
