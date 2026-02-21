package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of PrefixCmd.
 */
public class SlashPrefixCmd extends SlashAdminCommand
{
    public SlashPrefixCmd(Bot bot)
    {
        this.name = "prefix";
        this.help = "sets a server-specific prefix for text commands";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "prefix",
                        "New prefix to use (omit to clear the custom prefix)", false)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (!event.hasOption("prefix"))
        {
            s.setPrefix(null);
            event.reply(event.getClient().getSuccess() + " Prefix cleared.").queue();
            return;
        }

        String prefix = event.optString("prefix", "");
        if (prefix.isEmpty())
        {
            s.setPrefix(null);
            event.reply(event.getClient().getSuccess() + " Prefix cleared.").queue();
        }
        else
        {
            s.setPrefix(prefix);
            event.reply(event.getClient().getSuccess() + " Custom prefix set to `" + prefix
                    + "` on *" + event.getGuild().getName() + "*").queue();
        }
    }
}
